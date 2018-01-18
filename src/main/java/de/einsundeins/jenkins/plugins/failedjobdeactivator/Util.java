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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sonyericsson.jenkins.plugins.bfa.model.FailureCauseBuildAction;
import com.sonyericsson.jenkins.plugins.bfa.model.FoundFailureCause;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Plugin;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.jobConfigHistory.ConfigInfo;
import hudson.plugins.jobConfigHistory.JobConfigHistoryProjectAction;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

public class Util {

	private static Logger logger = Logger.getLogger(Util.class.getName());

	public static boolean isInstanceOfAbstractProject(Job<?, ?> job) {
		if (job instanceof AbstractProject)
			return true;

		return false;
	}

	public static boolean isBuildFailureAnalyzerAvailable() {
		Jenkins jenkins = Jenkins.getInstance();
		if (jenkins == null)
			return false;
		Plugin plugin = jenkins.getPlugin("build-failure-analyzer");

		if (plugin == null)
			return false;

		return true;
	}

	public static boolean isJobConfigHistoryAvailable() {
		Jenkins jenkins = Jenkins.getInstance();
		if (jenkins == null)
			return false;
		Plugin plugin = jenkins.getPlugin("jobConfigHistory");

		if (plugin == null)
			return false;

		return true;
	}
	
	public static boolean isWorkflowMultibranchAvailable() {
		Jenkins jenkins = Jenkins.getInstance();
		if (jenkins == null)
			return false;
		Plugin plugin = jenkins.getPlugin("workflow-multibranch");

		if (plugin == null)
			return false;

		return true;
	}
	
	public static boolean isMavenMultiBranchAvailable() {
		Jenkins jenkins = Jenkins.getInstance();
		if (jenkins == null)
			return false;
		Plugin plugin = jenkins.getPlugin("multi-branch-project-plugin");

		if (plugin == null)
			return false;

		return true;
	}

	public static String getFailureCauses(Job<?, ?> job) {

		if (job == null)
			return null;

		Run<?, ?> build = job.getLastBuild();
		if (build == null)
			return null;

		FailureCauseBuildAction action = build
				.getAction(FailureCauseBuildAction.class);
		if (action == null)
			return null;

		StringBuffer failureCauses = new StringBuffer();
		for (FoundFailureCause failureCause : action.getFoundFailureCauses()) {
			failureCauses.append(failureCause.getName());
			failureCauses.append("\n");
		}

		return failureCauses.toString();
	}

	public static String getLastUser(Job<?, ?> job) {

		if (job == null)
			return null;

		JobConfigHistoryProjectAction historyconfig = new JobConfigHistoryProjectAction(
				job);
		try {
			for (ConfigInfo info : historyconfig.getJobConfigs()) {
				String userId = info.getUserID();
				return userId;
			}
		} catch (IOException e) {
			return null;
		}

		return null;
	}

	public static Job<?, ?> getJobByName(String jobName) {
		Jenkins jenkins = Jenkins.getInstance();
		if (jenkins == null)
			return null;
		for (Item item : jenkins.getAllItems()) {
			if (item.getName().equals(jobName)) {
				Job<?, ?> job = (Job<?, ?>) item;
				return job;
			}
		}
		return null;
	}

	public static Map<Job<?, ?>, String> convertJsonToMap(JSONObject json) {
		Map<Job<?, ?>, String> map = new HashMap<>();
		try {
			Iterator<?> jobs = json.keys();
			while (jobs.hasNext()) {
				String jobName = (String) jobs.next();
				String action = (String) json.get(jobName);
				if (!action.equals("ignore")) {
					map.put(getJobByName(jobName), action);
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to convert json to map. ", e);
		}

		return map;
	}

	@SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_EXCEPTION",
			"OS_OPEN_STREAM"})
	public static File generateCsv(List<Job<?, ?>> jobs) {
		Jenkins jenkins = Jenkins.getInstance();
		if (jenkins == null)
			return null;
		String filePath = jenkins.getRootDir() + "/" + Constants.CSV_FILENAME;
		File file = new File(filePath);
		Writer writer = null;
		try {
			StringBuilder stringBuilder = new StringBuilder();
			writer = new OutputStreamWriter(new FileOutputStream(file),
					StandardCharsets.UTF_8);
			for (Job<?, ?> job : jobs) {
				String jobname = job.getName();
				String lastSuccessfulBuild = job
						.getLastSuccessfulBuild() != null
								? job.getLastSuccessfulBuild().getTime()
										.toString()
								: "";
				String lastBuild = job.getLastBuild() != null
						? job.getLastBuild().getTime().toString()
						: "";
				stringBuilder.append(jobname);
				stringBuilder.append(",").append(lastSuccessfulBuild);
				stringBuilder.append(",").append(lastBuild);

				if (isBuildFailureAnalyzerAvailable()) {
					String failureCauses = getFailureCauses(job);
					stringBuilder.append(",").append(failureCauses);
				}

				stringBuilder.append("\n");
			}

			writer.append(stringBuilder.toString());
			writer.flush();
		} catch (IOException e) {
			try {
				writer.close();
			} catch (IOException e1) {
			}
		}

		return file;
	}

}
