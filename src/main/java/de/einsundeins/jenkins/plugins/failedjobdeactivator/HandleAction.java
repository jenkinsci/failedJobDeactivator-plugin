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
import hudson.model.AbstractProject;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles all actions to be done with jobs, found by detektion class.
 * 
 * @author Jochen A. Fuerbacher
 *
 */
public class HandleAction {

    /**
     * The class logger.
     */
    private static final Logger logger = Logger.getLogger(HandleAction.class
            .getName());
    
    public HandleAction(){
        
    }

    /**
     * Logic method for handling jobs.
     * @param detectedJobs, list of all detected jobs.
     */
    public void handleJobs(List<DetectedJob> detectedJobs) {

        int x = 0;

        while (x < detectedJobs.size()) {

            if (detectedJobs.get(x).isDeleteJob()) {
                deleteJob(detectedJobs.get(x).getaProject());
            } else {
                deactivateJob(detectedJobs.get(x).getaProject());
            }

            x++;
        }

    }

    /**
     * Deactivates job.
     * 
     * @param job
     *            is the job to get deactivated.
     */
    private void deactivateJob(AbstractProject<?, ?> job) {
        try {
            job.disable();
        } catch (IOException e) {
            logger.log(INFO, "Failed to disable job.", e);
        }
    }

    /**
     * Deletes job.
     * 
     * @param job
     *            is the job to get deleted.
     */
    private void deleteJob(AbstractProject<?, ?> job) {

        try {
            job.delete();
        } catch (IOException e) {
            logger.log(INFO, "Failed to delete job.", e);
        } catch (InterruptedException e) {
            logger.log(INFO, "Failed to delete job.", e);
        }

    }

}
