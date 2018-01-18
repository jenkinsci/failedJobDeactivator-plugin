/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Jochen A. Fuerbacher, 1&1 Telecommunication SE
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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.Plugin;
import hudson.model.Job;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class FailedJobDeactivatorModel extends Plugin {

	private Logger logger = Logger
			.getLogger(FailedJobDeactivatorModel.class.getName());

	private JobScanner scanner;
	private boolean checkUsers;
	private boolean checkBuildFailures;

	public int getDefaultLastSuccessfulBuild() {
		return Constants.DEFAULT_LAST_SUCCESSFUL_BUILD;
	}

	public boolean getCheckUsers() {
		return checkUsers;
	}

	public boolean getCheckFailureCauses() {
		return checkBuildFailures;
	}

	public void doStartDetection(StaplerRequest req, StaplerResponse rsp)
			throws IOException {

		rsp.sendRedirect("showDetectedJobs");

		try {
			JSONObject submittedForm = req.getSubmittedForm();
			try {
				checkUsers = submittedForm.getBoolean("checkUsers");
			} catch (Exception e) {
				checkUsers = false;
			}
			try {
				checkBuildFailures = submittedForm
						.getBoolean("checkBuildFailures");
			} catch (Exception e) {
				checkBuildFailures = false;
			}
			scanner = new JobScanner(
					submittedForm.getLong("lastSuccessfulBuild"),
					submittedForm.getInt("limit"),
					submittedForm.getString("regex"));
			scanner.startDetection();
		} catch (JSONException | ServletException e) {
			logger.log(Level.WARNING, "Failed to get submitted form! " + e);
		}
	}

	public List<Job<?, ?>> getDetectedJobs() {
		return scanner.getDetectedJobs();
	}

	public boolean isInstanceOfAbstractProject(Job<?, ?> job) {
		return Util.isInstanceOfAbstractProject(job);
	}

	public boolean isBuildFailureAnalyzerAvailable() {
		return Util.isBuildFailureAnalyzerAvailable();
	}

	public boolean isJobConfigHistoryAvailable() {
		return Util.isJobConfigHistoryAvailable();
	}

	public boolean canPipelineJobsGetDisabled() {
		return Util.canPipelineJobsGetDisabled();
	}

	public String getFailureCauses(Job<?, ?> job) {
		return Util.getFailureCauses(job);
	}

	public String getLastUser(Job<?, ?> job) {
		return Util.getLastUser(job);
	}

	public void doHandleJobs(StaplerRequest req, StaplerResponse rsp)
			throws IOException, ServletException {

		if (req.hasParameter("generateCsv")) {
			File file = Util.generateCsv(getDetectedJobs());
			rsp.setContentType("text/csv");
			rsp.addHeader("Content-Disposition",
					"attachment; filename=" + Constants.CSV_FILENAME);
			rsp.serveFile(req, file.toURI().toURL());
		} else {
			rsp.forwardToPreviousPage(req);
			new JobHandling().performJobHandling(
					Util.convertJsonToMap(req.getSubmittedForm()));
		}
	}

}
