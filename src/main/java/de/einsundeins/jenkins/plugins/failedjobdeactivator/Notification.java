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

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static java.util.logging.Level.*;

import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import de.einsundeins.jenkins.plugins.failedjobdeactivator.FailedJobDeactivator.DescriptorImpl;
import jenkins.model.Jenkins;
import hudson.XmlFile;
import hudson.model.AbstractProject;
import hudson.model.Api;
import hudson.model.Item;
import hudson.tasks.Mailer;
import hudson.plugins.jobConfigHistory.JobConfigHistory;
import hudson.plugins.jobConfigHistory.JobConfigHistoryProjectAction;
import hudson.plugins.jobConfigHistory.ConfigInfo;
import hudson.plugins.jobConfigHistory.HistoryDescr;

/**
 * All ways of notification.
 * 
 * @author Jochen A. Fuerbacher
 *
 */
public class Notification {

    /**
     * The class logger.
     */
    private final Logger logger = Logger.getLogger(Notification.class
            .getName());

    /**
     * The current data.
     */
    private Date date;


    /**
     * All SMTP information, configured with the mailer plugin.
     */
    private String smtpServer;
    private String smtpPort;
    private String smtpAuthUserName;
    private String smtpAuthPassword;
    private boolean useSsl;
    private String replyToAddress;
    private boolean mailerConfigured;
    
    /**
     * The mailer configuration session.
     */
    private Session session;
    
    private FailedJobDeactivator.DescriptorImpl descriptor;
    
    /**
     * Default constructor.
     */
    public Notification(){
        date = new Date();
        initSmtp();
    }

    /**
     * Starts all notification features.
     * @param detectedJobs, the list of all detected jobs.
     */
    public void doNotification(List<DetectedJob> detectedJobs) {

        int x = 0;
        while (x < detectedJobs.size()) {

            updateJobDescription(detectedJobs.get(x));
            logAction(detectedJobs.get(x));
            if (mailerConfigured) {
                notifyUsers(detectedJobs.get(x));
            }

            x++;
        }

    }

    /**
     * Updates the description field of the jobs which will be deactivated or
     * deleted.
     * 
     * @param detectedJob
     *            is a detected Job
     */
      private void updateJobDescription(DetectedJob detectedJob) {
        try {
            if (!detectedJob.isDeleteJob()) {
                detectedJob.getaProject().setDescription(detectedJob.getaProject().getDescription() + "<br>"
                        + date.toString() + " - Deactivated: " + detectedJob.getFailureCause()
                        + "\n");
            } else {
                detectedJob.getaProject().setDescription(detectedJob.getaProject().getDescription() + "<br>"
                        + date.toString() + " - Deleted: " + detectedJob.getFailureCause() + "\n");
            }
        } catch (IOException e) {
            logger.log(INFO, "Failed to update job description.", e);
        }
    }

    /**
     * Loggs the jobs which will be deleted or deactivated.
     * 
     * @param detectedJob
     *            is a detected Job
     */
    private void logAction(DetectedJob detectedJob) {

        if (!detectedJob.isDeleteJob()) {
            logger.log(INFO, date.toString() + " - " + detectedJob.getaProject().getFullName()
                    + " deactivated: " + detectedJob.getFailureCause());
        } else {
            logger.log(WARNING,
                    date.toString() + " - " + detectedJob.getaProject().getFullName()
                            + " deleted: " + detectedJob.getFailureCause());
        }
    }

    /**
     * Notifies users via e mail.
     * 
     * @param detectedJob
     *            is a detected Job
     */
    private void notifyUsers(DetectedJob detectedJob) {

        FailedJobDeactivator property = (FailedJobDeactivator) detectedJob.getaProject()
                .getProperty(FailedJobDeactivator.class);
        descriptor = (DescriptorImpl) Jenkins
                .getInstance().getDescriptor(FailedJobDeactivator.class);

        MimeMessage msg = new MimeMessage(session);

        try {
            msg.setSubject("Failed Job Deactivator");
            if (!detectedJob.isDeleteJob()) {
                msg.setText("The job " + detectedJob.getaProject().getFullName()
                        + " was deactivated. - " + detectedJob.getFailureCause());
            } else {
                msg.setText("The job " + detectedJob.getaProject().getFullName()
                        + " was deleted. - " + detectedJob.getFailureCause());
            }
            msg.setFrom(new InternetAddress(replyToAddress));
            msg.setSentDate(new Date());

            //Add users from job config page.
            if ((property != null) && (property.getUserNotification() != null)) {
                msg.setRecipients(Message.RecipientType.TO, InternetAddress
                        .parse(property.getUserNotification(), true));
            }
            
            //Add user from jobconfighistory.
            LinkedList<String> responsibleUserAddresses = detectResponsibleUsers(detectedJob.getaProject());
            if(responsibleUserAddresses.size()>0){ 
                for(String userAddress : responsibleUserAddresses){
                    msg.addRecipients(Message.RecipientType.TO,
                        userAddress);
                }
            }

            //Add admin.
            if ((descriptor.getAdminNotification() != null)) {
                msg.addRecipients(Message.RecipientType.TO,
                        descriptor.getAdminNotification());
            }

            if (msg.getRecipients(Message.RecipientType.TO) != null) {
                Transport.send(msg);
            }

        } catch (MessagingException e) {
            logger.log(WARNING, "Sending email failed: " + e);
        }
    }
    
    /**
     * Returns the email address of the responsible user (first user in history) for the job.
     * @param project
     * @return the email address of the first user in history data.
     */
    private LinkedList<String> detectResponsibleUsers(AbstractProject<?,?> project){
        
        JobConfigHistoryProjectAction historyconfig = new JobConfigHistoryProjectAction(project);
        String responsibleUserId = null;
        String userAddress = null;
        LinkedList<String> responsibleUsers = new LinkedList<String>();
        
        try {
            if(historyconfig.getJobConfigs().size()>0){
                
                int countOfLastUsersToGetNotified = descriptor.getCountOfLastUsersToGetNotified() > 0 
                        ? descriptor.getCountOfLastUsersToGetNotified() : Constants.NUMBER_OF_RESPONSIBLE_USERS;
                
                int i = 0;
                while(i<countOfLastUsersToGetNotified && i<historyconfig.getJobConfigs().size()){
                    responsibleUserId = historyconfig.getJobConfigs().get(i).getUserID();
                    if(!responsibleUserId.contains("anonymous")){
                        userAddress = Jenkins.getInstance().getUser(responsibleUserId).getProperty(Mailer.UserProperty.class).getAddress();
                        
                        if(!responsibleUsers.contains(userAddress)){
                            responsibleUsers.add(userAddress);
                        }
                    }
                                        
                    i++;
                }
            }
        } catch (IOException e) {
            logger.log(WARNING, "Failed to get responsible user from JobConfigHistory. " + e);
        }
        
        return responsibleUsers;
    }

    /**
     * Initializes SMTP configuration.
     */
    private void initSmtp() {

        Mailer.DescriptorImpl mailer = (Mailer.DescriptorImpl) Jenkins.getInstance()
                .getDescriptorByType(Mailer.DescriptorImpl.class);

        smtpServer = mailer.getSmtpServer();
        smtpPort = mailer.getSmtpPort();
        smtpAuthUserName = mailer.getSmtpAuthUserName();
        smtpAuthPassword = mailer.getSmtpAuthPassword();
        useSsl = mailer.getUseSsl();
        replyToAddress = mailer.getReplyToAddress();
        
        if(smtpServer != null && replyToAddress != null){
            mailerConfigured = true;
            createSession();
        }else{
            mailerConfigured = false;
        }
    }
    
    /**
     * Creates mailing session
     */
      private void createSession() {

        Properties props = new Properties(System.getProperties());
        props.put("mail.smtp.host", smtpServer);
        
        //Set SMTP port to default if no port is entered. 
        if(smtpPort != null) {
            props.put("mail.smtp.port", smtpPort);
        }else{
            props.put("mail.smtp.port", "25");
        }        

        //Set SSL configuration
        if(useSsl) {

            //Used to create SMTP sockets.
            if(props.getProperty("mail.smtp.socketFactory.class") == null) {
                props.put("mail.smtp.socketFactory.class",
                        "javax.net.ssl.SSLSocketFactory");
            }
            
            //Set SMTP port to default if no port is entered.
            if(props.getProperty("mail.smtp.socketFactory.port") == null) {
                String port = (smtpPort == null) ? "465" : smtpPort;
                props.put("mail.smtp.port", port);
                props.put("mail.smtp.socketFactory.port", port);
            }
            
            props.put("mail.smtp.socketFactory.fallback", "false");
        }

        //Used to set authentification
        if(smtpAuthUserName != null) {
            props.put("mail.smtp.auth", "true");
        }

        //Set timeout (default is infinity).
        props.put("mail.smtp.timeout", "60000");
        props.put("mail.smtp.connectiontimeout", "60000");
        
        session = Session.getInstance(props, authenticator());
    }
      
    /**
     * Generates the mailer authenticator
     * @return null, if no user name, else authenticator
     */
    private Authenticator authenticator() {

        if (smtpAuthUserName == null) {
          return null;
        }
        
        return new Authenticator() {
            
          @Override
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(smtpAuthUserName, smtpAuthPassword);
          }
        };
    }
        
    /**
     * Used for testing only.
     * @return value of mailerConfigured
     */
    protected boolean isMailerConfigured(){
        return mailerConfigured;
    }
    
    /**
     * Used for testing only
     * @return SMTP host
     */
    protected String getSmtpServer(){
        return smtpServer;
    }
    
    /**
     * Used for testing only
     * @return SMTP port
     */
    protected String getSmtpPort(){
        return smtpPort;
    }
    
    /**
     * Used for testing only
     * @return Reply-to address
     */
    protected String getReplyToAddress(){
        return replyToAddress;
    }
    
    /**
     * Used for testing only
     */
    protected void testUpdateJobDescription(DetectedJob detectedJob){
        updateJobDescription(detectedJob);
    }
}
