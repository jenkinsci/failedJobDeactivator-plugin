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

import static org.junit.Assert.*;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.tasks.Mailer;
import jenkins.model.Jenkins;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class NotificationTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();
        
    @Test
    public void testMailer() throws Exception{
                     
        Mailer.DescriptorImpl mailer = (Mailer.DescriptorImpl) Jenkins.getInstance()
                .getDescriptorByType(Mailer.DescriptorImpl.class);
        mailer.setSmtpHost("localhost");
        mailer.setSmtpPort("25");
        mailer.setReplyToAddress("devnull@devnull");
        
        Notification notificationTest = new Notification();
       
        assertTrue(notificationTest.isMailerConfigured());
        assertEquals("localhost", notificationTest.getSmtpServer());
        assertEquals("25", notificationTest.getSmtpPort());
        assertEquals("devnull@devnull", notificationTest.getReplyToAddress());
    }
    
    @Test
    public void testUpdateJobDescription() throws Exception{
                     
        FreeStyleProject projectDelete = j.createFreeStyleProject();
        DetectedJob detectedJobTest1 = new DetectedJob();
        detectedJobTest1.setaProject(projectDelete);
        detectedJobTest1.setFailureCause("FailureCause1");
        detectedJobTest1.setDeleteJob(true);
        
        FreeStyleProject projectDeactivate = j.createFreeStyleProject();
        DetectedJob detectedJobTest2 = new DetectedJob();
        detectedJobTest2.setaProject(projectDeactivate);
        detectedJobTest2.setFailureCause("FailureCause2");
        detectedJobTest2.setDeleteJob(false);
                        
        Notification notificationTest = new Notification();
        notificationTest.testUpdateJobDescription(detectedJobTest1);
        notificationTest.testUpdateJobDescription(detectedJobTest2);
                     
        AbstractProject<?,?> dj1 = (AbstractProject<?,?>)j.getInstance().getAllItems().get(0);
        AbstractProject<?,?> dj2 = (AbstractProject<?,?>)j.getInstance().getAllItems().get(1);
          
        
        assertTrue(dj1.getDescription().contains(detectedJobTest1.getFailureCause()));
        assertTrue(dj1.getDescription().contains("Deleted"));
        
        assertTrue(dj2.getDescription().contains(detectedJobTest2.getFailureCause()));
        assertTrue(dj2.getDescription().contains("Deactivated"));
    }
}
