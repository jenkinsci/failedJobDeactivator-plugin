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

import hudson.model.FreeStyleProject;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import static org.junit.Assert.*;

public class DetectedJobTest extends JenkinsRule {
    
    @Rule
    public JenkinsRule j = new JenkinsRule();
    
    @Test
    public void testDeleteJob1(){
        DetectedJob detectedJobTest = new DetectedJob();
        
        detectedJobTest.setDeleteJob(false);
        
        boolean expRes = false;
        
        assertEquals(expRes, detectedJobTest.isDeleteJob());
    }
    
    @Test
    public void testDeleteJob2(){
        DetectedJob detectedJobTest = new DetectedJob();
        
        detectedJobTest.setDeleteJob(true);
        
        boolean expRes = true;
        
        assertEquals(expRes, detectedJobTest.isDeleteJob());
    }
     
    @Test
    public void testFailureCause1(){
        DetectedJob detectedJobTest = new DetectedJob();
        
        detectedJobTest.setFailureCause("failureCauseExample");
        
        String expRes = "failureCauseExample";
        
        assertEquals(expRes, detectedJobTest.getFailureCause());
    }
    
    @Test
    public void testFailureCause2(){
        DetectedJob detectedJobTest = new DetectedJob();
        
        detectedJobTest.setFailureCause("failureCauseExample");
        
        String expRes = "wrongFailureCauseExample";
        
        assertNotEquals(expRes, detectedJobTest.getFailureCause());
    }
    
    @Test
    public void testAProject() throws Exception{
             
        FreeStyleProject project = j.createFreeStyleProject();
        String displayName = "TestProject1";
        project.setDisplayName(displayName);
        
        DetectedJob detectedJobTest = new DetectedJob();        
        detectedJobTest.setaProject(project);
        
        assertEquals(displayName, detectedJobTest.getaProject().getDisplayName());
    }

}
