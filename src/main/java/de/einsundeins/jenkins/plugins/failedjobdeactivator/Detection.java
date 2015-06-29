/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Jochen A. Fuerbacher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.einsundeins.jenkins.plugins.failedjobdeactivator;

import java.util.ArrayList;
import java.util.List;

import de.einsundeins.jenkins.plugins.failedjobdeactivator.FailedJobDeactivator.DescriptorImpl;

import com.sonyericsson.jenkins.plugins.bfa.model.FoundFailureCause;
import com.sonyericsson.jenkins.plugins.bfa.model.FailureCauseBuildAction;

import jenkins.model.Jenkins;
import hudson.model.Item;
import hudson.model.TopLevelItem;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.Cause.UserIdCause;

/**
 * This class contains all detection logic.
 * 
 * @author Jochen A. Fuerbacher
 */
public class Detection {

    /**
     * List containing detected jobs.
     */
    private List<DetectedJob> detectedJobs;

    /**
     * Information, if the detected failure cause is a deletion failure cause.
     */
    private boolean isDeletionFailureCause;
    
    /**
     * Contains all failure causes and addional info in a simple string.
     */
    private String nameOfFailureCause;
    
    /**
     * The actual analyzed job.
     */
    private AbstractProject<?, ?> aProject;
    
    /**
     * The property of the actual analyzed job.
     */
    private FailedJobDeactivator property;
    
    /**
     * The current system time.
     */
    private long systemtime;
    
    /**
     * The global configuration descriptor.
     */
    private FailedJobDeactivator.DescriptorImpl descriptor;
    

    /**
     * Default constructor
     */
    public Detection() {

        detectedJobs = new ArrayList<DetectedJob>();
        descriptor = (DescriptorImpl) Jenkins.getInstance().getDescriptor(
                FailedJobDeactivator.class);
        systemtime = System.currentTimeMillis();
    }

    /**
     * Start detection of inactive projects.
     */
    public void startDetection() {

        boolean jobDetected = false;

        // Get all jobs
        for (Item project : Jenkins.getInstance().getAllItems()) {
            
            aProject = (AbstractProject<?, ?>) project;
            property = (FailedJobDeactivator) aProject
                    .getProperty(FailedJobDeactivator.class);
            jobDetected = false;

            // Check if detection is active for job and job is not disabled
            if ((aProject != null) && (aProject instanceof TopLevelItem)
                    && (!aProject.isDisabled())
                    && ((property == null) || ((property != null) && (property
                            .getActive())))) {
                
                // Check if Job has never been runned.
                jobDetected = checkHasNeverRunned(aProject, property);

                // Check if Job has never been runned successfully.
                if (!jobDetected) {
                    jobDetected = checkHasNeverRunnedSuccessfully(aProject,
                            property);
                }

                // Check if lastSuccessfulBuild too long ago
                if (!jobDetected) {
                    jobDetected = checkLastSuccessfulBuildTooLongAgo(aProject, property);
                }

            }
        }
        
    }

    /**
     * Starts analyzation of the failure causes of the last build.
     * 
     * @param aProject
     *            is the project to be analysed.
     */
    private void analyseFailureCauses(AbstractProject<?, ?> aProject) {

        List<FoundFailureCause> failureCauses = new ArrayList<FoundFailureCause>();
        AbstractBuild<?, ?> abstractBuild = (AbstractBuild<?, ?>) aProject
                .getLastBuild();
        FailureCauseBuildAction subAction = abstractBuild
                .getAction(FailureCauseBuildAction.class);
        if (subAction != null) {
            failureCauses = subAction.getFailureCauseDisplayData() != null ? subAction
                    .getFailureCauseDisplayData().getFoundFailureCauses()
                    : null;
        }
        isDeletionFailureCause = false;
        
        //Neccessary to not show "null" on frontend
        nameOfFailureCause = new String();
        
        loopOverFailureCauses(failureCauses);

    }
    
    /**
     * Analyzes failure causes.
     * @param failureCauses are the detected failure causes of the last build.
     */
    private void loopOverFailureCauses(List<FoundFailureCause> failureCauses){
        
        if(failureCauses.size() == 0){
            isDeletionFailureCause = descriptor.getDeleteJobsWithoutFailureCauses();
        }
        
        for (int i = 0; (i < failureCauses.size()); i++) {

            nameOfFailureCause += (failureCauses.get(i).getName() + "; ");

            for (int j = 0; (descriptor.getJobHandling() != null) && 
                    (j < descriptor.getJobHandling().size()); j++) {

                if (failureCauses.get(i).getId()
                        .equals(descriptor.getJobHandling()
                                .get(j)[Constants.FAILURE_CAUSE_ID])
                        && descriptor.getJobHandling().get(j)[Constants.HOW_TO_HANDLE_JOB]
                                .equals("Delete")) {

                    isDeletionFailureCause = true;
                }
            }
        }
    }

    /**
     * Analyzes the time when the last build was triggered manually.
     * 
     * @param job
     *            The job to get analyzed
     * @param deadlineIn64bitUnixtime
     *            is the deadline time in 64 bit Unixtime
     * @return the boolean value, if a build was triggered manually within the
     *         deadline.
     */
    private boolean isManuallyTriggeredInDeadline(AbstractProject<?, ?> job,
            long deadlineIn64bitUnixtime) {

        int numberOfBuilds = job.getLastBuild().getNumber();
        int numberOfCauses = 0;
        List<Cause> causes = new ArrayList<Cause>();

            // Iterate over all builds within the deadline
            while ((numberOfBuilds >= job.getFirstBuild().getNumber())
                    && (job.getBuildByNumber(numberOfBuilds) != null)
                    && (isInDeadline(job.getBuildByNumber(numberOfBuilds)
                            .getTimeInMillis(), deadlineIn64bitUnixtime))){
                                    
                if (job.getBuildByNumber(numberOfBuilds).getCauses() != null) {

                    causes = job.getBuildByNumber(numberOfBuilds).getCauses();
                    numberOfCauses = causes.size();
                }

                // Iterate over all causes
                for (int i = 0; i < numberOfCauses; i++) {

                    // Check if cause is UserIdCause
                    if ((causes.get(i) != null)
                            && (causes.get(i) instanceof UserIdCause)
                            && (((UserIdCause) causes.get(i)).getUserName() != null)) {

                        numberOfBuilds = -1;
                        return true;
                    }
                }
                numberOfCauses = 0;
                numberOfBuilds--;
            }
            
        return false;
    }

    /**
     * Checks if the job has never been built.
     * 
     * @param job
     * @param property
     */
    private boolean checkHasNeverRunned(AbstractProject<?, ?> job,
            FailedJobDeactivator property) {

        long deadline = 0;

        if (job.getLastBuild() == null) {
            deadline = getDeadlineLastSuccessfulBuild(property);

            if (!isInDeadline(job.getBuildDir().lastModified(), deadline)) {
                if (this.descriptor != null) {
                    setDetectedJob(job, "Job has never been built.",
                            this.descriptor.getDeleteNeverBuiltJobs(),
                            0, null);
                } else {
                    setDetectedJob(job, "Job has never been built.", true,
                            0, null);
                }

                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the job has never runned successfully.
     * 
     * @param job
     * @param property
     */
    private boolean checkHasNeverRunnedSuccessfully(AbstractProject<?, ?> job,
            FailedJobDeactivator property) {

        long deadlineLastManuallyTriggered = 0;
        long deadlineLastSuccessfulBuild = 0;

        if ((job.getLastBuild() != null)
                && (job.getLastSuccessfulBuild() == null)) {

            deadlineLastManuallyTriggered = getDeadlineLastManuallyTriggered(property);
            deadlineLastSuccessfulBuild = getDeadlineLastSuccessfulBuild(property);

            if((!isInDeadline(job.getFirstBuild()
                    .getTimeInMillis(), deadlineLastSuccessfulBuild))
                            && !(isManuallyTriggeredInDeadline(job,
                                    deadlineLastManuallyTriggered))) {

                analyseFailureCauses(job);

                setDetectedJob(job,
                        "Job has never been built successfully. Last failure cause was: "
                                + this.nameOfFailureCause,
                        this.isDeletionFailureCause,
                        calculateDayDifference(job.getLastBuild().getTimeInMillis()),
                        job.getLastBuild().getResult().toString());

                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the last successful build was too long ago
     * 
     * @param job
     *            The job to get analyzed
     * @param property
     */
    private boolean checkLastSuccessfulBuildTooLongAgo(AbstractProject<?, ?> job,
            FailedJobDeactivator property) {

        long deadlineLastManuallyTriggered = 0;
        long deadlineLastSuccessfulBuild = 0;

        if ((job.getLastBuild() != null)
                && (job.getLastSuccessfulBuild() != null)) {

            deadlineLastManuallyTriggered = getDeadlineLastManuallyTriggered(property);
            deadlineLastSuccessfulBuild = getDeadlineLastSuccessfulBuild(property);

            if ((!isInDeadline(job.getLastSuccessfulBuild()
          .getTimeInMillis(), deadlineLastSuccessfulBuild))
                            && !(isManuallyTriggeredInDeadline(job,
                                    deadlineLastManuallyTriggered))) {
                
                analyseFailureCauses(job);

                setDetectedJob(job,
                        "Last successful build is " 
                        + calculateDayDifference(job.getLastSuccessfulBuild().getTimeInMillis()) 
                        + " days long ago. Last failure cause was: "
                                + this.nameOfFailureCause,
                        this.isDeletionFailureCause,
                        calculateDayDifference(job.getLastBuild().getTimeInMillis()),
                        job.getLastBuild().getResult().toString());
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the detected job in a list.
     * 
     * @param job
     * @param failureCause
     * @param deleteJob
     * @param timeOfLastBuild
     * @param resultOfLastBuild
     */
    private void setDetectedJob(AbstractProject<?, ?> job, String failureCause,
            boolean deleteJob, long timeOfLastBuild, String resultOfLastBuild) {

        DetectedJob detectedJob = new DetectedJob();
        detectedJob.setaProject(job);
        detectedJob.setFailureCause(failureCause);
        detectedJob.setDeleteJob(deleteJob);
        detectedJob.setTimeOfLastBuild(timeOfLastBuild);
        detectedJob.setResultOfLastBuild(resultOfLastBuild);

        this.detectedJobs.add(detectedJob);
    }

    /**
     * Getter for the deadline of the last successful build in 64 bit unix time.
     * 
     * @param property
     * @return
     */
    private long getDeadlineLastSuccessfulBuild(FailedJobDeactivator property) {

        if (property == null) {
            
            return this.descriptor.getGlobalLastSuccessfulBuild()
                    * Constants.DAYS_TO_64BIT_UNIXTIME;

        } else {
            return property.getLastSuccessfulBuild()
                    * Constants.DAYS_TO_64BIT_UNIXTIME;
        }
    }

    /**
     * Getter for the deadline of the last manually triggered build in 64 bit unix time.
     * 
     * @param property property of the job.
     * @return Deadline in 64 bit unix time
     */
    private long getDeadlineLastManuallyTriggered(FailedJobDeactivator property) {

        if (property == null) {
            
            return this.descriptor.getGlobalLastManuallyTriggered()
                    * Constants.DAYS_TO_64BIT_UNIXTIME;
        } else {
            return property.getLastManuallyTriggered()
                    * Constants.DAYS_TO_64BIT_UNIXTIME;
        }
    }
    
    /**
     * Checks if the timestamp lays within the deadline, starting from today.
     * The timestamp has to be in the past.
     * @param timestamp of the job
     * @param deadline in 64 bit unix time
     * @return true, if within the deadline; else false.
     */
    private boolean isInDeadline(long timestamp, long deadline){
        
        if((this.systemtime - timestamp) < deadline){
            return true;
        }
        
        return false;
        
    }
    
    /**
     * Calculates the difference between the actual time and a timestamp.
     * @param timestamp in 64 bit unix time
     * @return number of days
     */
    private long calculateDayDifference(long timestamp){
        
        return ((systemtime - timestamp)/Constants.DAYS_TO_64BIT_UNIXTIME);
    }

    /**
     * 
     * @return Returns the list of all detected jobs, their failure causes and
     *         information whether the job should get deleted.
     */
    public List<DetectedJob> getDetectedJobs() {
        return this.detectedJobs;
    }

    /**
     * Clears all lists.
     */
    public void clearLists() {

        this.detectedJobs.clear();
    }
    
    /**
     * Used for testing only.
     * @param job
     * @param failureCause
     * @param deleteJob
     */
    protected void testSetDetectedJob(AbstractProject<?, ?> job, String failureCause,
            boolean deleteJob, long timeOfLastBuild, String resultOfLastBuild){
        
        setDetectedJob(job, failureCause, deleteJob, timeOfLastBuild, resultOfLastBuild);
    }
    
    /**
     * Used for testing only.
     * @param property
     * @return
     */
    protected long testGetDeadlineLastManuallyTriggered(FailedJobDeactivator property){
        return getDeadlineLastManuallyTriggered(property);
    }
    
    /**
     * Used for testing only.
     * @param property
     * @return
     */
    protected long testGetDeadlineLastSuccessfulBuild(FailedJobDeactivator property){
        return getDeadlineLastSuccessfulBuild(property);
    }
    
    /**
     * Used for testing only.
     * @param job
     * @param property
     */
    protected void testCheckHasNeverRunnedSuccessfully(AbstractProject<?, ?> job,
            FailedJobDeactivator property){
        checkHasNeverRunnedSuccessfully(job,property);
    }
    
    /**
     * Used for testing only.
     * @param job
     * @param property
     */
    protected void testCheckLastSuccessfulBuildTooLongAgo(AbstractProject<?, ?> job,
            FailedJobDeactivator property){
        checkLastSuccessfulBuildTooLongAgo(job,property);
    }
}
