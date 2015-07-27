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

import static java.util.logging.Level.INFO;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import com.sonyericsson.jenkins.plugins.bfa.PluginImpl;
import com.sonyericsson.jenkins.plugins.bfa.model.FailureCause;

/**
 *
 * @author Jochen A. Fuerbacher
 */
public class FailedJobDeactivator extends JobProperty<Job<?, ?>> {

    /**
     * The deadline of the last manually triggered build (job config).
     */
    private int lastManuallyTriggered;
    
    /**
     * The deadline ot the last successful build (job config).
     */
    private int lastSuccessfulBuild;

    /**
     * Plugin active for the job?
     */
    private boolean active;

    /**
     * E mail addresses for user notification in job config.
     */
    private String userNotification;

    /**
     * Job already configured for plugin?
     */
    private boolean isConfigured;
    
    /**
     * Timestamp of the first job configuration.
     */
    private long dateOfFirstJobConfiguration;
    

    /**
     * Default DataBoundConstructor.
     * @param optionalBlock
     */
    @DataBoundConstructor
    public FailedJobDeactivator(LocalValues optionalBlock, long dateOfFirstJobConfiguration) {

        if (optionalBlock != null) {
            this.active = optionalBlock.active;
            this.lastManuallyTriggered = optionalBlock.lastManuallyTriggered;
            this.lastSuccessfulBuild = optionalBlock.lastSuccessfulBuild;
            this.userNotification = optionalBlock.userNotification;
            this.isConfigured = true;
        } else {
            this.active = true;
            this.isConfigured = false;
        }
                      
        if(dateOfFirstJobConfiguration <= 0L){
        	this.dateOfFirstJobConfiguration = System.currentTimeMillis();
        }else{
        	this.dateOfFirstJobConfiguration = dateOfFirstJobConfiguration;
        }
                
    }

    /**
     * @return Returns if the detection is enabled or disabled for this job.
     */
    public boolean getActive() {
        return active;
    }

    /**
     * @return Configured time in days since last manually triggered build
     */
    public int getLastManuallyTriggered() {
        if (isConfigured) {
            return lastManuallyTriggered;
        } else {
            return getDescriptor().getGlobalLastManuallyTriggered();
        }
    }

    /**
     * @return Configured time in days since last successful build
     */
    public int getLastSuccessfulBuild() {
        if (isConfigured) {
            return lastSuccessfulBuild;
        } else {
            return getDescriptor().getGlobalLastSuccessfulBuild();
        }
    }

    /**
     * @return Returns the email address of the user who should get notified.
     */
    public String getUserNotification() {
        return userNotification;
    }

    /**
     * 
     * @return True if the job is already configured.
     */
    public boolean getIsConfigured() {
        return this.isConfigured;
    }
    
    /**
     * 
     * @return Timestamp of the first job configuration.
     */
    public long getDateOfFirstJobConfiguration(){
    	return this.dateOfFirstJobConfiguration;
    }
    

    /**
     * Descriptor for {@link FailedJobDeactivator}.
     */
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link FailedJobDeactivator}.
     */
    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {

        /**
         * E mail address for admin notification.
         */
        private String adminNotification;

        /**
         * Info if jobs, that have never been built should get deleted.
         */
        private boolean deleteNeverBuiltJobs;
        
        /**
         * Info if jobs without detected failure causes should get deleted.
         */
        private boolean deleteJobsWithoutFailureCauses;

        /**
         * Available failure causes and how to handle jobs when those failure causes are detected.
         */
        private List<String[]> jobHandling;

        /**
         * Global default deadline for the last successful build.
         */
        private int globalLastSuccessfulBuild;

        /**
         * Global default deadline for the last manually triggered build.
         */
        private int globalLastManuallyTriggered;
        

        /**
         * Loads the persisted global configuration.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field
         * 'lastManuallyTriggered'.
         *
         * @param value
         *            This parameter receives the value that the user has typed.
         * @return Indicates the outcome of the validation. This is sent to the
         *         browser.
         */
        public FormValidation doCheckLastManuallyTriggered(
                @QueryParameter int value) throws IOException, ServletException {
            if (value < 1) {
                return FormValidation.error(Messages.errorMessageFormValidationDeadline());
            }
            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation of the form field
         * 'lastSuccessfulBuild'.
         *
         * @param value
         *            This parameter receives the value that the user has typed.
         * @return Indicates the outcome of the validation. This is sent to the
         *         browser.
         */
        public FormValidation doCheckLastSuccessfulBuild(
                @QueryParameter int value) throws IOException, ServletException {
            if (value < 1) {
                return FormValidation.error(Messages.errorMessageFormValidationDeadline());
            }
            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation of the form field 'adminNotification'.
         * 
         * @param value
         * @return
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doCheckAdminNotification(
                @QueryParameter String value) throws IOException,
                ServletException {
            if (!value.equals("")
                    && !value
                            .matches("([0-9]|[a-z]|[A-Z]|\\.|-|_)+"
                                    + "@([0-9]|[a-z]|[A-Z]|\\.|-|_)+"
                                    +"\\.([0-9]|[a-z]|[A-Z]|\\.|-|_)+")) {
                return FormValidation.error(Messages.errorMessageFormValidationEmailAddresses());
            }
            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation of the form field
         * 'globalLastSuccessfulBuild'.
         * 
         * @param value
         * @return
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doCheckGlobalLastSuccessfulBuild(
                @QueryParameter int value) throws IOException, ServletException {
            if (value < 1) {
                return FormValidation.error(Messages.errorMessageFormValidationDeadline());
            }
            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation of the form field
         * 'globalLastManuallyTriggered'.
         * 
         * @param value
         * @return
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doCheckGlobalLastManuallyTriggered(
                @QueryParameter int value) throws IOException, ServletException {
            if (value < 1) {
                return FormValidation.error(Messages.errorMessageFormValidationDeadline());
            }
            return FormValidation.ok();
        }

        /**
         * Indicates that this builder can be used with all kinds of project
         * types.
         */
        public boolean isApplicable(Class<? extends Job> jobType) {

            return true;
        }
            
        
        /**
         * Safes global configuration.
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData)
                throws FormException {

            this.adminNotification = formData.getString("adminNotification");
            this.deleteNeverBuiltJobs = formData
                    .getBoolean("deleteNeverBuiltJobs");
            this.deleteJobsWithoutFailureCauses = formData
                    .getBoolean("deleteJobsWithoutFailureCauses");
            Logger logger = Logger.getLogger(FailedJobDeactivator.class
                    .getName());

            try {
                if (Integer.parseInt(formData
                        .getString("globalLastManuallyTriggered")) > 0) {
                    this.globalLastManuallyTriggered = Integer
                            .parseInt(formData
                                    .getString("globalLastManuallyTriggered"));
                }
                if (Integer.parseInt(formData
                        .getString("globalLastSuccessfulBuild")) > 0) {
                    this.globalLastSuccessfulBuild = Integer.parseInt(formData
                            .getString("globalLastSuccessfulBuild"));
                }
            } catch (NumberFormatException e) {
                logger.log(INFO, "Entered value is not a number. " + e);
            }

            configureSaveJobHandling(formData.get("deleteJob"),
                    formData.get("failureCauseId"));

            save();
            return super.configure(req, formData);
        }

        /**
         * Help method for configure(). Saves the global configuration of the
         * jobhandling.
         * 
         * @param deleteJob
         *            Value if the job should get deleted.
         * @param failureCauseId
         *            Failure cause.
         */
        @SuppressWarnings("unchecked")
        private void configureSaveJobHandling(Object deleteJob,
                Object failureCauseId) {
            
            // Contains info how to handle a job
            List<String> deletejobs = new LinkedList<String>();
            
            // Contains failure cause ids
            List<String> failureCauseIds = new LinkedList<String>();
            this.jobHandling = new LinkedList<String[]>();

            deletejobs = (List<String>) deleteJob;
            failureCauseIds = (List<String>) failureCauseId;

            int i = 0;
            
            //Assigning job handling to failure causes
            while (failureCauseId != null && i < failureCauseIds.size()) {

                String[] localJobHandling = new String[2];
                localJobHandling[Constants.FAILURE_CAUSE_ID] = failureCauseIds
                        .get(i);
                localJobHandling[Constants.HOW_TO_HANDLE_JOB] = deletejobs
                        .get(i);

                this.jobHandling.add(i, localJobHandling);

                i++;
            }
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return Messages.displayName();
        }

        /**
         * 
         * @return Returns email address of admin.
         */
        public String getAdminNotification() {

            return this.adminNotification;
        }

        /**
         * 
         * @return Returns configuration if jobs which have never been built
         *         should get deleted.
         */
        public boolean getDeleteNeverBuiltJobs() {

            return this.deleteNeverBuiltJobs;
        }
        
        /**
         * 
         * @return Returns configuration if jobs which do not have failure
         *         causes should get deleted.
         */
        public boolean getDeleteJobsWithoutFailureCauses(){
            
            return this.deleteJobsWithoutFailureCauses;
        }

        /**
         * 
         * @return A list of 2-dim arrays containing failure cause ids and how
         *         to handle them.
         */
        public List<String[]> getJobHandling() {

            return this.jobHandling;
        }

        /**
         * 
         * @return Global configuration of the deadline of the last successful
         *         build.
         */
        public int getGlobalLastManuallyTriggered() {

            if (this.globalLastManuallyTriggered > 0) {
                return this.globalLastManuallyTriggered;
            } else {
                return Constants.LASTMANUALLYTRIGGERED_DEFAULT;
            }
        }

        /**
         * 
         * @return Global configuration of the deadline of the last manually
         *         triggered build.
         */
        public int getGlobalLastSuccessfulBuild() {

            if (this.globalLastSuccessfulBuild > 0) {
                return this.globalLastSuccessfulBuild;
            } else {
                return Constants.LASTSUCCESSFULBUILD_DEFAULT;
            }
        }
        
        /**
         * 
         * @return Default deadline of the last successful build.
         */
        public int getDefaultGlobalLastManuallyTriggered() {

            return Constants.LASTMANUALLYTRIGGERED_DEFAULT;
        }

        /**
         * 
         * @return Default deadline of the last manually triggered build.
         */
        public int getDefaultGlobalLastSuccessfulBuild() {

            return Constants.LASTSUCCESSFULBUILD_DEFAULT;
        }


        /**
         * Getter for frontend generation of configured job handling.
         * 
         * @param id
         *            Failure cause id which should get analyzed.
         * @return If available failure cause is already configured, return
         *         configuration, else return default value.
         */
        private String getJobHandlingConfigForFrontend(String id) {

            int i = 0;

            while (this.jobHandling != null && i < this.jobHandling.size()) {
                if (this.jobHandling.get(i)[Constants.FAILURE_CAUSE_ID]
                        .equals(id)) {
                    return this.jobHandling.get(i)[Constants.HOW_TO_HANDLE_JOB];
                }

                i++;
            }

            return Constants.DEFAULT_HANDLING;
        }

        /**
         * Reads all available failure causes from Build Failure Analyzer
         * Plugin.
         * 
         * @return Returns the list of all available failure causes (names and
         *         ids).
         */
        public List<FailureCause> getAvailableFailureCauses() {

            Logger logger = Logger.getLogger(FailedJobDeactivator.class
                    .getName());

            Collection<FailureCause> failureCausesColl;
            List<FailureCause> failureCauseNames = null;
            try {
                failureCausesColl = Jenkins.getInstance()
                        .getPlugin(PluginImpl.class).getKnowledgeBase()
                        .getCauseNames();

                if (Jenkins.getInstance().getPlugin(PluginImpl.class)
                        .getKnowledgeBase().getCauseNames() instanceof List) {
                    failureCauseNames = (List<FailureCause>) failureCausesColl;
                } else {
                    failureCauseNames = new ArrayList<FailureCause>(
                            failureCausesColl);
                }
            } catch (Exception e) {
                logger.log(INFO, "Failed to load failure causes. ", e);
            }

            return failureCauseNames;

        }
    }

    /**
     * Class for optional block an job config page
     * 
     * @author Jochen A. Fuerbacher
     *
     */
    public static class LocalValues {

        /**
         * Plugin active for the job?
         */
        private boolean active;

        /**
         * The deadline of the last manually triggered build (job config).
         */
        private int lastManuallyTriggered;

        /**
         * The deadline ot the last successful build (job config).
         */
        private int lastSuccessfulBuild;

        /**
         * E mail addresses for user notification in job config.
         */
        private String userNotification;

        /**
         * Default DataBoundConstructor.
         */
        @DataBoundConstructor
        public LocalValues(boolean active, int lastManuallyTriggered,
                int lastSuccessfulBuild, String userNotification) {
            
            this.active = active;
            
            FailedJobDeactivator.DescriptorImpl descriptor = (DescriptorImpl)Jenkins.getInstance().getDescriptor(
                    FailedJobDeactivator.class);

            if (lastManuallyTriggered < 1) {
                this.lastManuallyTriggered = descriptor.getGlobalLastManuallyTriggered();
            } else {
                this.lastManuallyTriggered = lastManuallyTriggered;
            }

            if (lastSuccessfulBuild < 1) {
                this.lastSuccessfulBuild = descriptor.getGlobalLastSuccessfulBuild();
            } else {
                this.lastSuccessfulBuild = lastSuccessfulBuild;
            }

            this.userNotification = userNotification;
        }
    }
}
