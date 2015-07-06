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

import hudson.model.AbstractProject;

/**
 * Contains all detected jobs and their failure causes.
 * 
 * @author jfuerbacher
 *
 */
public class DetectedJob {

    /**
     * The detected job.
     */
    private AbstractProject<?, ?> aProject;

    /**
     * Contains the failure cause of the detected job.
     */
    private String failureCause;

    /**
     * If true, the job should get deleted.
     */
    private boolean deleteJob;
    
    /**
     * Days since last build.
     */
    private int timeOfLastBuild;
    
    /**
     * Result of the last build.
     */
    private String resultOfLastBuild;

    /**
     * Default constructor.
     */
    public DetectedJob() {

    }

    /**
     * Getter for the detected job.
     * 
     * @return is the detected job.
     */
    public AbstractProject<?, ?> getaProject() {
        return aProject;
    }

    /**
     * Setter for the detected job.
     * 
     * @param aProject
     *            is the detected job.
     */
    public void setaProject(AbstractProject<?, ?> aProject) {
        this.aProject = aProject;
    }

    /**
     * Getter for the failure cause of the detected job.
     * 
     * @return the failure cause of the detected job.
     */
    public String getFailureCause() {
        return failureCause;
    }

    /**
     * Setter for the failure cause of the detected job.
     * 
     * @param failureCause
     *            of the detected job.
     */
    public void setFailureCause(String failureCause) {
        this.failureCause = failureCause;
    }

    /**
     * Getter for the information, if the detected job should get deleted.
     * 
     * @return true, if the job should get deleted.
     */
    public boolean isDeleteJob() {
        return deleteJob;
    }

    /**
     * Setter for the information, if the detected job should get deleted.
     * 
     * @param deleteJob
     *            is the information, if the detected job should get deleted.
     */
    public void setDeleteJob(boolean deleteJob) {
        this.deleteJob = deleteJob;
    }
    
    /**
     * Getter for the days since last build.
     * @return days since last build.
     */
    public int getTimeOfLastBuild(){
        return this.timeOfLastBuild;
    }
    
    /**
     * Setter for the days since last build.
     * @param timeOfLastBuild
     */
    public void setTimeOfLastBuild(int timeOfLastBuild){
        this.timeOfLastBuild = timeOfLastBuild;
    }
    
    /**
     * Getter for the result of the last build.
     * @return result of the last build.
     */
    public String getResultOfLastBuild(){
        return this.resultOfLastBuild;
    }
    
    /**
     * Setter for the result of the last build.
     * @param resultOfLastBuild
     */
    public void setResultOfLastBuild(String resultOfLastBuild){
        this.resultOfLastBuild = resultOfLastBuild;
    }

}
