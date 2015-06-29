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

import java.util.ArrayList;
import java.util.List;

import hudson.model.FreeStyleProject;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class HandleActionTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();
    
    @Test
    public void testHandleJobs() throws Exception{
                     
        FreeStyleProject projectDelete = j.createFreeStyleProject();
        FreeStyleProject projectDeactivate = j.createFreeStyleProject();
        
        DetectedJob detectedJobTest1 = new DetectedJob();
        DetectedJob detectedJobTest2 = new DetectedJob();
        
        detectedJobTest1.setaProject(projectDelete);
        detectedJobTest1.setDeleteJob(true);
        
        detectedJobTest2.setaProject(projectDeactivate);
        detectedJobTest2.setDeleteJob(false);
        
        List<DetectedJob> list = new ArrayList<DetectedJob>();
        list.add(detectedJobTest1);
        list.add(detectedJobTest2);
        
        HandleAction handleAction = new HandleAction();
        handleAction.handleJobs(list);
        
        assertTrue((j.getInstance().getAllItems().size() == 1) && (j.getInstance().getAllItems().get(0) == projectDeactivate));
    }
}
