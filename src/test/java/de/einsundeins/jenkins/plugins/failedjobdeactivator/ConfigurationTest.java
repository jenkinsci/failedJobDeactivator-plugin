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

import jenkins.model.Jenkins;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import net.sf.json.JSONObject;
import static org.junit.Assert.*;

import de.einsundeins.jenkins.plugins.failedjobdeactivator.FailedJobDeactivator.DescriptorImpl;

/**
 * Contains test cases for global and job configuration.
 * @author Jochen A. Fuerbacher
 *
 */
public class ConfigurationTest {
    
    @Rule
    public JenkinsRule j = new JenkinsRule();
    
    @Test
    public void testJobConfig1() throws Exception {
        
        boolean active = true;
        int lastSuccessfulBuild = 28;
        int lastManuallyTriggered = 14;
        String userNotification = "test@test.com";
        
        FailedJobDeactivator.LocalValues localdataTest = new FailedJobDeactivator.LocalValues
                (active,lastManuallyTriggered, lastSuccessfulBuild,userNotification);
        FailedJobDeactivator test = new FailedJobDeactivator(localdataTest);
        
        assertEquals(active,test.getActive());
        assertEquals(lastSuccessfulBuild,test.getLastSuccessfulBuild());
        assertEquals(lastManuallyTriggered,test.getLastManuallyTriggered());
        assertEquals(userNotification,test.getUserNotification());
        assertTrue(test.getIsConfigured());
    }
    
    @Test
    public void testJobConfig2() throws Exception {
        
        FailedJobDeactivator test = new FailedJobDeactivator(null);
        
        assertEquals(false,test.getIsConfigured());
        assertEquals(true,test.getActive());
        assertEquals(Constants.LASTSUCCESSFULBUILD_DEFAULT,test.getLastSuccessfulBuild());
        assertEquals(Constants.LASTMANUALLYTRIGGERED_DEFAULT,test.getLastManuallyTriggered());
        assertTrue(test.getUserNotification() == null);
    }
    
    @Test
    public void testGlobalConfig1() throws Exception {
              
        FailedJobDeactivator.DescriptorImpl descriptor = (DescriptorImpl) Jenkins
                .getInstance().getDescriptor(FailedJobDeactivator.class);
        
        String adminNotification = "test@test.com";
        boolean deleteNeverBuiltJobs = false;
        boolean deleteJobsWithoutFailureCauses = false;
        int globalLastManuallyTriggered = 14;
        int globalLastSuccessfulBuild = 28;
        
        JSONObject configure = new JSONObject();
        configure.accumulate("adminNotification", adminNotification);
        configure.accumulate("deleteNeverBuiltJobs", deleteNeverBuiltJobs);
        configure.accumulate("deleteJobsWithoutFailureCauses", deleteJobsWithoutFailureCauses);
        configure.accumulate("globalLastManuallyTriggered", globalLastManuallyTriggered);
        configure.accumulate("globalLastSuccessfulBuild", globalLastSuccessfulBuild);
        
        descriptor.configure(null, configure);
        
        assertEquals(adminNotification,descriptor.getAdminNotification());
        assertEquals(deleteNeverBuiltJobs,descriptor.getDeleteNeverBuiltJobs());
        assertEquals(deleteJobsWithoutFailureCauses,descriptor.getDeleteJobsWithoutFailureCauses());
        assertEquals(globalLastManuallyTriggered,descriptor.getGlobalLastManuallyTriggered());
        assertEquals(globalLastSuccessfulBuild,descriptor.getGlobalLastSuccessfulBuild());
    }
    
    @Test
    public void testGlobalConfig2() throws Exception {
              
        FailedJobDeactivator.DescriptorImpl descriptor = (DescriptorImpl) Jenkins
                .getInstance().getDescriptor(FailedJobDeactivator.class);
        
        String adminNotification = "abcdef123456789";
        boolean deleteNeverBuiltJobs = true;
        boolean deleteJobsWithoutFailureCauses = true;
        int globalLastManuallyTriggered = -1;
        int globalLastSuccessfulBuild = 0;
        
        JSONObject configure = new JSONObject();
        configure.accumulate("adminNotification", adminNotification);
        configure.accumulate("deleteNeverBuiltJobs", deleteNeverBuiltJobs);
        configure.accumulate("deleteJobsWithoutFailureCauses", deleteJobsWithoutFailureCauses);
        configure.accumulate("globalLastManuallyTriggered", globalLastManuallyTriggered);
        configure.accumulate("globalLastSuccessfulBuild", globalLastSuccessfulBuild);
        
        descriptor.configure(null, configure);
        
        assertEquals(adminNotification,descriptor.getAdminNotification());
        assertEquals(deleteNeverBuiltJobs,descriptor.getDeleteNeverBuiltJobs());
        assertEquals(deleteJobsWithoutFailureCauses,descriptor.getDeleteJobsWithoutFailureCauses());
        assertEquals(Constants.LASTMANUALLYTRIGGERED_DEFAULT,descriptor.getGlobalLastManuallyTriggered());
        assertEquals(Constants.LASTSUCCESSFULBUILD_DEFAULT,descriptor.getGlobalLastSuccessfulBuild());
    }
}