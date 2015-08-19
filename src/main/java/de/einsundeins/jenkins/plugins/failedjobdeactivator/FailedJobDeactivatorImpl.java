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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import de.einsundeins.jenkins.plugins.failedjobdeactivator.FailedJobDeactivator.DescriptorImpl;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import hudson.Plugin;
import hudson.model.AbstractProject;

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
    
    private List<DetectedJob> detectedJobs;
    
    private Logger logger = Logger.getLogger(FailedJobDeactivatorImpl.class
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

        try {
            startDetection(req.getSubmittedForm());
        } catch (ServletException e) {
            logger.log(WARNING, "Failed to get submitted form! " + e);
        }

    }
    
    /**
     * Starts detection
     */
    private void startDetection(JSONObject submittedForm) {
        detection = new Detection(submittedForm);
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
     * 
     * @return
     *      global configuration.
     */
    public FailedJobDeactivator.DescriptorImpl getDescriptor(){
        return (DescriptorImpl) Jenkins.getInstance().getDescriptor(
                FailedJobDeactivator.class);
    }
    
    /**
     * 
     * @param aProject
     * @return job property
     */
    public FailedJobDeactivator getProperty(AbstractProject<?, ?> aProject){
        return (FailedJobDeactivator) aProject
                .getProperty(FailedJobDeactivator.class);
    }

    /**
     * Handling logic of the detected jobs.
     * @param req
     * @param rsp
     * @throws IOException
     * @throws ServletException 
     */
    public void doHandleJobs(StaplerRequest req, StaplerResponse rsp)
            throws IOException, ServletException {
                
        if(req.getParameterMap().containsKey("exportCSV")){
            doExportCSV(req, rsp);
        }else{  
            
            rsp.sendRedirect("");
            try {
                this.detectedJobs = new ArrayList<DetectedJob>();
                reconfigureJobHandling(req.getSubmittedForm().get("handleJob"));
            } catch (ServletException e) {
                logger.log(WARNING, "Failed to get submitted form! " + e);
            }
            
            if(detectedJobs.size() != 0){
                
                Notification notification = new Notification();
                notification.doNotification(detectedJobs);
            
                HandleAction handleAction = new HandleAction();
                handleAction.handleJobs(detectedJobs);
        
            }             
            detection.clearLists();
        }
    }
    
    /**
     * Reconfigures detected jobs on how to handle them.
     * @param jobHandling Configured values from frontend (detected jobs). 
     */
    private void reconfigureJobHandling(Object jobHandling){
        
        JSONArray jobHandlingJson = (JSONArray)jobHandling;
      
        if(detection.getDetectedJobs().size() != jobHandlingJson.size()){
            logger.log(WARNING, "Could not perform job handling. Invalid size of detected jobs and configuration. Try again!");
        }else{
            for(int i = 0; i < detection.getDetectedJobs().size(); i++){
                                
                if(jobHandlingJson.get(i).equals("Delete")){
                    detectedJobs.add(detection.getDetectedJobs().get(i));
                    detectedJobs.get(detectedJobs.size()-1).setDeleteJob(true);
                }else if(jobHandlingJson.get(i).equals("Deactivate")){
                    detectedJobs.add(detection.getDetectedJobs().get(i));
                    detectedJobs.get(detectedJobs.size()-1).setDeleteJob(false);
                }
            }
        }
    }
    
    public void doExportCSV(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException{
        
        File folder = new File("exports/");
        if (!folder.exists() || !folder.isDirectory()) {
            folder.mkdir();
            System.out.println("Verzeichnis erstellt.");
        }
                        
        FileWriter writer = new FileWriter("exports/detectedJobs.csv");
        writer.append("Projectname,");
        writer.append("Failure causes,");
        writer.append("Days since last build,");
        writer.append("Result of last build,");
        writer.append("Job handling,");
        writer.append("Further information");
        writer.append('\n');
                
        JSONArray jobHandlingJson = (JSONArray)req.getSubmittedForm().get("handleJob");
        
        if(detection.getDetectedJobs().size() != jobHandlingJson.size()){
            logger.log(WARNING, "Could not perform CSV export. Invalid size of detected jobs and configuration. Try again!");
        }else{
            for(int i = 0; i < detection.getDetectedJobs().size(); i++){
                
                writer.append(detection.getDetectedJobs().get(i).getaProject().getFullName()+",");
                writer.append(detection.getDetectedJobs().get(i).getFailureCause()+",");
                writer.append(detection.getDetectedJobs().get(i).getTimeOfLastBuild()+",");
                writer.append(detection.getDetectedJobs().get(i).getResultOfLastBuild()+",");
                writer.append(jobHandlingJson.get(i)+",");
                writer.append("Deactivated: " + detection.getDetectedJobs().get(i).getaProject().isDisabled()+"<br />");
                if(detection.getDetectedJobs().get(i).getaProject().getProperty(FailedJobDeactivator.class)!=null){
                    writer.append("Plugin active: " + detection.getDetectedJobs().get(i).getaProject().getProperty(FailedJobDeactivator.class).getActive());
                }
                writer.append('\n');
            }
        }
          
      writer.flush();
      writer.close();
            
      rsp.setHeader("Content-Type", "text/csv");
      rsp.serveFile(req, new File("exports/detectedJobs.csv").toURI().toURL());
  }

}
