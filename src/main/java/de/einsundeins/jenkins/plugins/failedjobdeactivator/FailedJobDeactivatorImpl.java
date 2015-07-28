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

import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import hudson.Plugin;

/**
 * Main class for the plugin. Containing all logic (except configuration).
 * 
 * @author Jochen A. Fuerbacher
 */
public class FailedJobDeactivatorImpl extends Plugin {

    /**
     * The actual detection object.
     */
    private Detection detection;
    
    private List<DetectedJob> dj;
    
    Logger logger = Logger.getLogger(FailedJobDeactivatorImpl.class
            .getName());

    /**
     * Getter for the plugin instance.
     * @return the Plugin to the Jenkins instance.
     */
    public static FailedJobDeactivatorImpl getInstance() {
        return Jenkins.getInstance().getPlugin(FailedJobDeactivatorImpl.class);
    }

    /**
     * Start of the detection.
     * @param req
     * @param rsp
     * @throws IOException
     */
    public void doStartDetection(StaplerRequest req, StaplerResponse rsp)
            throws IOException {

        rsp.sendRedirect("showDetectedJobs");

        startDetection();

    }

    /**
     * Starts detection
     */
    private void startDetection() {
        detection = new Detection();
        detection.startDetection();
    }

    /**
     * Getter for the list of all detected jobs.
     * @return the list of the detected jobs.
     */
    public List<DetectedJob> getDetectedJobs() {

        return detection.getDetectedJobs();
    }

    /**
     * Handling logic of the detected jobs.
     * @param req
     * @param rsp
     * @throws IOException
     */
    public void doHandleJobs(StaplerRequest req, StaplerResponse rsp)
            throws IOException {

        rsp.sendRedirect("");
                        
        try {
            this.dj = new ArrayList<DetectedJob>();
            reconfigureJobHandling(req.getSubmittedForm().get("handleJob"));
        } catch (ServletException e) {
            logger.log(WARNING, "Failed to get submitted form! " + e);
        }
             
        if(dj.size() != 0){
            
            Notification notification = new Notification();
            notification.doNotification(dj);
        
            HandleAction handleAction = new HandleAction();
            handleAction.handleJobs(dj);
        
        }

        detection.clearLists();
    }
    
    /**
     * Reconfigures detected jobs on how to handle them.
     * @param jobHandling Configured values from frontend (detected jobs). 
     */
    private void reconfigureJobHandling(Object jobHandling){
        
        JSONArray js = (JSONArray)jobHandling;
      
        if(detection.getDetectedJobs().size() != js.size()){
            logger.log(WARNING, "Could not perform job handling. Invalid size of detected jobs and configuration. Try again!");
        }else{
            for(int i = 0; i < detection.getDetectedJobs().size(); i++){
                                
                if(js.get(i).equals("Delete")){
                    dj.add(detection.getDetectedJobs().get(i));
                    dj.get(dj.size()-1).setDeleteJob(true);
                }else if(js.get(i).equals("Deactivate")){
                    dj.add(detection.getDetectedJobs().get(i));
                    dj.get(dj.size()-1).setDeleteJob(false);
                }
            }
        }
    }

}
