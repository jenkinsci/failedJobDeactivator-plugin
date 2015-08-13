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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import jenkins.model.Jenkins;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import net.sf.json.JSONObject;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import de.einsundeins.jenkins.plugins.failedjobdeactivator.FailedJobDeactivator.DescriptorImpl;

/**
 * Contains test cases for the detection.
 * @author Jochen A. Fuerbacher
 *
 */
public class DetectionTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();
    
    /**
     * Tests setter of DetectedJob as well as the getter for the detected jobs.
     * @throws Exception
     */
    @Test
    public void testDetectedJobs1() throws Exception {
        
        FreeStyleProject projectDelete = j.createFreeStyleProject();
        FreeStyleProject projectDeactivate = j.createFreeStyleProject();
               
        JSONObject detectionConfiguration = new JSONObject();
        detectionConfiguration.accumulate("showDeactivatedJobs", false);
        detectionConfiguration.accumulate("showExcludedJobs", false);
        detectionConfiguration.accumulate("deleteNeverBuiltJobs", false);
        detectionConfiguration.accumulate("forceGlobalDeadlines", false);
        detectionConfiguration.accumulate("globalLastSuccessfulBuild", 28);
        detectionConfiguration.accumulate("globalLastManuallyTriggered", 14);
                
        Detection detection = new Detection(detectionConfiguration);
        
        detection.testSetDetectedJob(projectDelete, "Test1", true, 2, "FAILURE");
        detection.testSetDetectedJob(projectDeactivate, "Test2", false, -1, "ANY_RESULT");
        
        assertEquals(projectDelete, detection.getDetectedJobs().get(0).getaProject());
        assertEquals("Test1", detection.getDetectedJobs().get(0).getFailureCause());
        assertEquals(true, detection.getDetectedJobs().get(0).isDeleteJob());
        assertEquals(2, detection.getDetectedJobs().get(0).getTimeOfLastBuild());
        assertEquals("FAILURE", detection.getDetectedJobs().get(0).getResultOfLastBuild());
        
        assertEquals(projectDeactivate, detection.getDetectedJobs().get(1).getaProject());
        assertEquals("Test2", detection.getDetectedJobs().get(1).getFailureCause());
        assertEquals(false, detection.getDetectedJobs().get(1).isDeleteJob());
        assertEquals(-1, detection.getDetectedJobs().get(1).getTimeOfLastBuild());
        assertEquals("ANY_RESULT", detection.getDetectedJobs().get(1).getResultOfLastBuild());
        
        assertTrue(detection.getDetectedJobs().size() == 2);
        
        detection.clearLists();
        
        assertTrue(detection.getDetectedJobs().size() == 0);
    }
    
    /**
     * Tests the deadlines when no property is configured.
     * @throws Exception
     */
    @Test
    public void testDeadlines1() throws Exception{
        
        JSONObject detectionConfiguration = new JSONObject();
        detectionConfiguration.accumulate("showDeactivatedJobs", false);
        detectionConfiguration.accumulate("showExcludedJobs", false);
        detectionConfiguration.accumulate("deleteNeverBuiltJobs", false);
        detectionConfiguration.accumulate("forceGlobalDeadlines", false);
        detectionConfiguration.accumulate("globalLastSuccessfulBuild", 28);
        detectionConfiguration.accumulate("globalLastManuallyTriggered", 14);
        
        Detection detection = new Detection(detectionConfiguration);
        
        assertEquals(Constants.LASTMANUALLYTRIGGERED_DEFAULT*Constants.DAYS_TO_64BIT_UNIXTIME, 
                detection.testGetDeadlineLastManuallyTriggered(null));
        assertEquals(Constants.LASTSUCCESSFULBUILD_DEFAULT*Constants.DAYS_TO_64BIT_UNIXTIME, 
                detection.testGetDeadlineLastSuccessfulBuild(null));
    }
    
    /**
     * Tests the deadlines when no job property but global descriptor is configured.
     * @throws Exception
     */
    @Test
    public void testDeadlines2() throws Exception{
        
        FailedJobDeactivator.DescriptorImpl descriptor = (DescriptorImpl) Jenkins
                .getInstance().getDescriptor(FailedJobDeactivator.class);
        
        int globalLastManuallyTriggered = 2;
        int globalLastSuccessfulBuild = 4;
        
        JSONObject configure = new JSONObject();
        configure.accumulate("adminNotification", "");
        configure.accumulate("deleteNeverBuiltJobs", false);
        configure.accumulate("deleteJobsWithoutFailureCauses", false);
        configure.accumulate("globalLastManuallyTriggered", globalLastManuallyTriggered);
        configure.accumulate("globalLastSuccessfulBuild", globalLastSuccessfulBuild);
        descriptor.configure(null, configure);
        
        JSONObject detectionConfiguration = new JSONObject();
        detectionConfiguration.accumulate("showDeactivatedJobs", false);
        detectionConfiguration.accumulate("showExcludedJobs", false);
        detectionConfiguration.accumulate("deleteNeverBuiltJobs", false);
        detectionConfiguration.accumulate("forceGlobalDeadlines", false);
        detectionConfiguration.accumulate("globalLastSuccessfulBuild", descriptor.getGlobalLastSuccessfulBuild());
        detectionConfiguration.accumulate("globalLastManuallyTriggered", descriptor.getGlobalLastManuallyTriggered());
        
        Detection detection = new Detection(detectionConfiguration);
        
        assertEquals(globalLastManuallyTriggered*Constants.DAYS_TO_64BIT_UNIXTIME, detection.testGetDeadlineLastManuallyTriggered(null));
        assertEquals(globalLastSuccessfulBuild*Constants.DAYS_TO_64BIT_UNIXTIME, detection.testGetDeadlineLastSuccessfulBuild(null));
    }
    
    /**
     * Tests deadlines when job property is available.
     * @throws Exception
     */
    @Test
    public void testDeadlines3() throws Exception{
        
        FailedJobDeactivator.DescriptorImpl descriptor = (DescriptorImpl) Jenkins
                .getInstance().getDescriptor(FailedJobDeactivator.class);
        
        int globalLastManuallyTriggered = 2;
        int globalLastSuccessfulBuild = 4;
        
        JSONObject configure = new JSONObject();
        configure.accumulate("adminNotification", "");
        configure.accumulate("deleteNeverBuiltJobs", false);
        configure.accumulate("deleteJobsWithoutFailureCauses", false);
        configure.accumulate("globalLastManuallyTriggered", globalLastManuallyTriggered);
        configure.accumulate("globalLastSuccessfulBuild", globalLastSuccessfulBuild);
        descriptor.configure(null, configure);
        

        boolean active = true;
        int lastSuccessfulBuild = 8;
        int lastManuallyTriggered = 4;
        String userNotification = "user@test.com";
               
        FailedJobDeactivator.LocalValues localdataTest = new FailedJobDeactivator.LocalValues
                (active,lastManuallyTriggered, lastSuccessfulBuild,userNotification);
        FailedJobDeactivator test = new FailedJobDeactivator(localdataTest);      
        
        JSONObject detectionConfiguration = new JSONObject();
        detectionConfiguration.accumulate("showDeactivatedJobs", false);
        detectionConfiguration.accumulate("showExcludedJobs", false);
        detectionConfiguration.accumulate("deleteNeverBuiltJobs", descriptor.getDeleteNeverBuiltJobs());
        detectionConfiguration.accumulate("forceGlobalDeadlines", false);
        detectionConfiguration.accumulate("globalLastSuccessfulBuild", descriptor.getGlobalLastSuccessfulBuild());
        detectionConfiguration.accumulate("globalLastManuallyTriggered", descriptor.getGlobalLastManuallyTriggered());
        
        Detection detection = new Detection(detectionConfiguration);        
        assertEquals(lastManuallyTriggered*Constants.DAYS_TO_64BIT_UNIXTIME, detection.testGetDeadlineLastManuallyTriggered(test));
        assertEquals(lastSuccessfulBuild*Constants.DAYS_TO_64BIT_UNIXTIME, detection.testGetDeadlineLastSuccessfulBuild(test));
    }
      
    /**
     * Tests detection of a job that has never been built successfully.
     * The local data job used here does not contain any failure cause.
     * @throws Exception
     */
    @Test
    @LocalData
    public void testCheckHasNeverRunnedSuccessfully() throws Exception{
        
        FailedJobDeactivator.DescriptorImpl descriptor = (DescriptorImpl) Jenkins
                .getInstance().getDescriptor(FailedJobDeactivator.class);
        
        int globalLastManuallyTriggered = 1;
        int globalLastSuccessfulBuild = 1;
        
        JSONObject configure = new JSONObject();
        configure.accumulate("adminNotification", "");
        configure.accumulate("deleteNeverBuiltJobs", false);
        configure.accumulate("deleteJobsWithoutFailureCauses", false);
        configure.accumulate("globalLastManuallyTriggered", globalLastManuallyTriggered);
        configure.accumulate("globalLastSuccessfulBuild", globalLastSuccessfulBuild);
        descriptor.configure(null, configure);
        
        AbstractProject<?,?> testProject = (AbstractProject<?,?>)j.getInstance().getItem("Test5_hasNeverBeenBuildSuccessfully");
        
        FailedJobDeactivator property = (FailedJobDeactivator) testProject
                .getProperty(FailedJobDeactivator.class);
        
        JSONObject detectionConfiguration = new JSONObject();
        detectionConfiguration.accumulate("showDeactivatedJobs", false);
        detectionConfiguration.accumulate("showExcludedJobs", false);
        detectionConfiguration.accumulate("deleteNeverBuiltJobs", descriptor.getDeleteNeverBuiltJobs());
        detectionConfiguration.accumulate("forceGlobalDeadlines", false);
        detectionConfiguration.accumulate("globalLastSuccessfulBuild", descriptor.getGlobalLastSuccessfulBuild());
        detectionConfiguration.accumulate("globalLastManuallyTriggered", descriptor.getGlobalLastManuallyTriggered());
        
        Detection detection = new Detection(detectionConfiguration);
        detection.testCheckHasNeverRunnedSuccessfully(testProject,property);
        
        assertTrue(detection.getDetectedJobs().size() == 1);
        assertEquals(testProject, detection.getDetectedJobs().get(0).getaProject());
        assertTrue(!detection.getDetectedJobs().get(0).isDeleteJob());
        
        
        detection.clearLists();
        
        
        JSONObject configure2 = new JSONObject();
        configure2.accumulate("adminNotification", "");
        configure2.accumulate("deleteNeverBuiltJobs", false);
        configure2.accumulate("deleteJobsWithoutFailureCauses", true);
        configure2.accumulate("globalLastManuallyTriggered", globalLastManuallyTriggered);
        configure2.accumulate("globalLastSuccessfulBuild", globalLastSuccessfulBuild);
        descriptor.configure(null, configure2);
        
        detection.testCheckHasNeverRunnedSuccessfully(testProject,property);
        
        assertTrue(detection.getDetectedJobs().size() == 1);
        assertEquals(testProject, detection.getDetectedJobs().get(0).getaProject());
        assertTrue(detection.getDetectedJobs().get(0).isDeleteJob());

    }
    
    /**
     * Tests detection of a job that has been built unsuccessfully for a longer time.
     * The local data job used here contains failure causes.
     * @throws Exception
     */
    @Test
    @LocalData
    public void testCheckLastSuccessfulBuildTooLongAgo() throws Exception{
        
        AbstractProject<?,?> testProject = (AbstractProject<?,?>)j.getInstance().getItem("Test6_lastSuccessfulBuildTooLongAgo");
        
        FailedJobDeactivator.DescriptorImpl descriptor = (DescriptorImpl) Jenkins
                .getInstance().getDescriptor(FailedJobDeactivator.class);
        
        int globalLastManuallyTriggered = 1;
        int globalLastSuccessfulBuild = 1;
        
        JSONObject configure = new JSONObject();
        configure.accumulate("adminNotification", "");
        configure.accumulate("deleteNeverBuiltJobs", false);
        configure.accumulate("deleteJobsWithoutFailureCauses", false);
        configure.accumulate("globalLastManuallyTriggered", globalLastManuallyTriggered);
        configure.accumulate("globalLastSuccessfulBuild", globalLastSuccessfulBuild);
        descriptor.configure(null, configure);
               
        FailedJobDeactivator property = (FailedJobDeactivator) testProject
                .getProperty(FailedJobDeactivator.class);
        
        JSONObject detectionConfiguration = new JSONObject();
        detectionConfiguration.accumulate("showDeactivatedJobs", false);
        detectionConfiguration.accumulate("showExcludedJobs", false);
        detectionConfiguration.accumulate("deleteNeverBuiltJobs", descriptor.getDeleteNeverBuiltJobs());
        detectionConfiguration.accumulate("forceGlobalDeadlines", false);
        detectionConfiguration.accumulate("globalLastSuccessfulBuild", descriptor.getGlobalLastSuccessfulBuild());
        detectionConfiguration.accumulate("globalLastManuallyTriggered", descriptor.getGlobalLastManuallyTriggered());
        
        Detection detection = new Detection(detectionConfiguration);
        detection.testCheckLastSuccessfulBuildTooLongAgo(testProject,property);
        
        assertTrue(detection.getDetectedJobs().size() == 1);
        assertEquals(testProject, detection.getDetectedJobs().get(0).getaProject());
        assertTrue(!detection.getDetectedJobs().get(0).isDeleteJob());
        
        
        detection.clearLists();
                
        JSONObject configure2 = new JSONObject();
        configure2.accumulate("adminNotification", "");
        configure2.accumulate("deleteNeverBuiltJobs", false);
        configure2.accumulate("deleteJobsWithoutFailureCauses", true);
        configure2.accumulate("globalLastManuallyTriggered", Integer.MAX_VALUE);
        configure2.accumulate("globalLastSuccessfulBuild", Integer.MAX_VALUE);
        descriptor.configure(null, configure2);
        
        detection.testCheckHasNeverRunnedSuccessfully(testProject,property);
        
        assertTrue(detection.getDetectedJobs().size() == 0);
    }
    
    @Test
    @LocalData
    public void testCheckLastSuccessfulBuildTooLongAgoWithFailureCauses() throws Exception{
        
        AbstractProject<?,?> testProject = (AbstractProject<?,?>)j.getInstance().getItem("Test6_lastSuccessfulBuildTooLongAgo");
        
        FailedJobDeactivator.DescriptorImpl descriptor = (DescriptorImpl) Jenkins
                .getInstance().getDescriptor(FailedJobDeactivator.class);
        
        FailedJobDeactivator property = (FailedJobDeactivator) testProject
                .getProperty(FailedJobDeactivator.class);
        
        int globalLastManuallyTriggered = 1;
        int globalLastSuccessfulBuild = 1;
        List<Object> failureCause = new LinkedList<Object>();
        failureCause.add("bd154ab2-31ef-4d11-92bf-e22292db6ba3");
        List<Object> deleteJob = new LinkedList<Object>();
        deleteJob.add("Delete");
                
        JSONObject configure = new JSONObject();
        configure.accumulate("adminNotification", "");
        configure.accumulate("deleteNeverBuiltJobs", false);
        configure.accumulate("deleteJobsWithoutFailureCauses", false);
        configure.accumulate("globalLastManuallyTriggered", globalLastManuallyTriggered);
        configure.accumulate("globalLastSuccessfulBuild", globalLastSuccessfulBuild);
        configure.accumulate("failureCauseId", failureCause);
        configure.accumulate("deleteJob", deleteJob);
        descriptor.configure(null, configure);
        
        JSONObject detectionConfiguration = new JSONObject();
        detectionConfiguration.accumulate("showDeactivatedJobs", false);
        detectionConfiguration.accumulate("showExcludedJobs", false);
        detectionConfiguration.accumulate("deleteNeverBuiltJobs", descriptor.getDeleteNeverBuiltJobs());
        detectionConfiguration.accumulate("forceGlobalDeadlines", false);
        detectionConfiguration.accumulate("globalLastSuccessfulBuild", descriptor.getGlobalLastSuccessfulBuild());
        detectionConfiguration.accumulate("globalLastManuallyTriggered", descriptor.getGlobalLastManuallyTriggered());
        
        Detection detection = new Detection(detectionConfiguration);
        detection.testCheckLastSuccessfulBuildTooLongAgo(testProject,property);
        
        assertTrue(detection.getDetectedJobs().size() == 1);
        assertEquals(testProject, detection.getDetectedJobs().get(0).getaProject());
        assertTrue(detection.getDetectedJobs().get(0).isDeleteJob());

        
        detection.clearLists();
        
        
        failureCause = new LinkedList<Object>();
        failureCause.add("AnyWrongId");
        deleteJob = new LinkedList<Object>();
        deleteJob.add("Delete");
                
        JSONObject configure2 = new JSONObject();
        configure2.accumulate("adminNotification", "");
        configure2.accumulate("deleteNeverBuiltJobs", false);
        configure2.accumulate("deleteJobsWithoutFailureCauses", false);
        configure2.accumulate("globalLastManuallyTriggered", globalLastManuallyTriggered);
        configure2.accumulate("globalLastSuccessfulBuild", globalLastSuccessfulBuild);
        configure2.accumulate("failureCauseId", failureCause);
        configure2.accumulate("deleteJob", deleteJob);
        descriptor.configure(null, configure2);
                      
        detection = new Detection(detectionConfiguration);
        detection.testCheckLastSuccessfulBuildTooLongAgo(testProject,property);
        
        assertTrue(detection.getDetectedJobs().size() == 1);
        assertEquals(testProject, detection.getDetectedJobs().get(0).getaProject());
        assertTrue(!detection.getDetectedJobs().get(0).isDeleteJob());
    }
    
    @Test
    @LocalData
    public void testDetection() throws Exception{
        
        AbstractProject<?,?> testProject1 = (AbstractProject<?,?>)j.getInstance().getItem("Test5_hasNeverBeenBuildSuccessfully");
        AbstractProject<?,?> testProject2 = (AbstractProject<?,?>)j.getInstance().getItem("Test6_lastSuccessfulBuildTooLongAgo");
        
        FailedJobDeactivator.DescriptorImpl descriptor = (DescriptorImpl) Jenkins
                .getInstance().getDescriptor(FailedJobDeactivator.class);
        
        int globalLastManuallyTriggered = 1;
        int globalLastSuccessfulBuild = 1;
        
        JSONObject configure = new JSONObject();
        configure.accumulate("adminNotification", "");
        configure.accumulate("deleteNeverBuiltJobs", false);
        configure.accumulate("deleteJobsWithoutFailureCauses", true);
        configure.accumulate("globalLastManuallyTriggered", globalLastManuallyTriggered);
        configure.accumulate("globalLastSuccessfulBuild", globalLastSuccessfulBuild);
        descriptor.configure(null, configure);
                    
        JSONObject detectionConfiguration = new JSONObject();
        detectionConfiguration.accumulate("showDeactivatedJobs", false);
        detectionConfiguration.accumulate("showExcludedJobs", false);
        detectionConfiguration.accumulate("deleteNeverBuiltJobs", descriptor.getDeleteNeverBuiltJobs());
        detectionConfiguration.accumulate("forceGlobalDeadlines", false);
        detectionConfiguration.accumulate("globalLastSuccessfulBuild", descriptor.getGlobalLastSuccessfulBuild());
        detectionConfiguration.accumulate("globalLastManuallyTriggered", descriptor.getGlobalLastManuallyTriggered());
        
        Detection detection = new Detection(detectionConfiguration);
        detection.startDetection();
        
        assertTrue(detection.getDetectedJobs().size() == 2);
        assertEquals(testProject1, detection.getDetectedJobs().get(0).getaProject());
        assertTrue(detection.getDetectedJobs().get(0).isDeleteJob());
        assertEquals(testProject2, detection.getDetectedJobs().get(1).getaProject());
        assertTrue(!detection.getDetectedJobs().get(1).isDeleteJob());
    }
}
