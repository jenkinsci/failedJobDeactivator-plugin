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

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;

import com.github.mjdetullio.jenkins.plugins.multibranch.MavenMultiBranchProject;

import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import jenkins.model.Jenkins;

public class JobScanner {

	private Logger logger = Logger.getLogger(JobScanner.class.getName());

	// Scanner configuration
	long lastSuccessfulBuild;
	int limit;
	String regex;
	transient long systemtime;
	transient boolean regexProvided;
	transient boolean isWorkflowMultibranchAvailable;
	transient boolean isMavenMultiBranchAvailable;

	List<Job<?, ?>> detectedJobs;

	public JobScanner(long lastSuccessfulBuild, int limit, String regex) {
		this.lastSuccessfulBuild = lastSuccessfulBuild
				* Constants.DAYS_TO_64BIT_UNIXTIME;
		this.limit = limit;
		this.regex = regex;
	}

	public void startDetection() {
		this.detectedJobs = new LinkedList<>();
		systemtime = System.currentTimeMillis();
		regexProvided = regex != null && !regex.isEmpty();
		Jenkins jenkins = Jenkins.getInstance();
		isWorkflowMultibranchAvailable = Util.isWorkflowMultibranchAvailable();
		isMavenMultiBranchAvailable = Util.isMavenMultiBranchAvailable();

		if (jenkins == null)
			return;
		for (Item item : jenkins.getAllItems()) {
			if (limit == 0)
				return;

			if (!isCandidate(item))
				continue;

			Job<?, ?> job = (Job<?, ?>) item;
			if (jobHasNoBuildsAndExistsTooLong(job)) {
				detectedJobs.add(job);
				limit--;
				continue;
			}
			if (job.getBuilds().isEmpty())
				continue;

			if (jobHasNoSuccessfulBuilds(job)) {
				limit--;
				detectedJobs.add(job);
			}

		}
	}

	private boolean isCandidate(Item item) {
		// Only check TopLevelItems.
		if (!(item instanceof TopLevelItem))
			return false;

		// Only check jobs.
		if (!(item instanceof Job))
			return false;

		// Do not check job if it is part of a Maven multi branch project.
		if (isMavenMultiBranchAvailable
				&& item.getParent() instanceof MavenMultiBranchProject)
			return false;

		// Do not check job if it is part of a multibranch pipeline.
		if (isWorkflowMultibranchAvailable
				&& item.getParent() instanceof WorkflowMultiBranchProject)
			return false;

		// Only check items matching a pattern (if provided).
		if (regexProvided && !jobnameMatchesPattern(item.getName()))
			return false;

		return true;
	}

	private boolean jobHasNoBuildsAndExistsTooLong(Job<?, ?> job) {

		logger.log(Level.FINEST,
				"Check if job " + job.getName() + " has no builds.");

		if (!job.getBuilds().isEmpty())
			return false;

		if (isInDeadline(job.getBuildDir().lastModified()))
			return false;

		return true;
	}

	/**
	 * Checks if the last successful build is too long ago or, if there is no
	 * successful build, if the jobs exists too long
	 */
	private boolean jobHasNoSuccessfulBuilds(Job<?, ?> job) {
		logger.log(Level.FINEST,
				"Check if job " + job.getName() + " has no successful builds.");

		Run<?, ?> lastSuccessfulBuild = job.getLastSuccessfulBuild();

		if (lastSuccessfulBuild != null
				&& isInDeadline(lastSuccessfulBuild.getTimeInMillis()))
			return false;

		Run<?, ?> firstBuild = job.getFirstBuild();

		if (lastSuccessfulBuild == null
				&& isInDeadline(firstBuild.getTimeInMillis()))
			return false;

		return true;
	}

	private boolean isInDeadline(long jobtime) {
		if ((systemtime - jobtime) < lastSuccessfulBuild)
			return true;

		return false;
	}

	public List<Job<?, ?>> getDetectedJobs() {
		return detectedJobs;
	}

	private boolean jobnameMatchesPattern(String jobName) {
		return Pattern.matches(regex, jobName);
	}

}
