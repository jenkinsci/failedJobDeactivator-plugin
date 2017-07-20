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

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.model.AbstractProject;
import hudson.model.Job;

public class JobHandling {

	private Logger logger = Logger.getLogger(JobHandling.class.getName());

	public boolean performJobHandling(Map<Job<?, ?>, String> jobs) {

		try {
			Iterator<Map.Entry<Job<?, ?>, String>> iter = jobs.entrySet()
					.iterator();

			while (iter.hasNext()) {
				Map.Entry<Job<?, ?>, String> jobEntry = iter.next();
				Job<?, ?> job = jobEntry.getKey();
				String jobaction = jobEntry.getValue();

				switch (jobaction) {
					case "disable" :
						disableJob(job);
						break;
					case "delete" :
						deleteJob(job);
						break;
					default :
				}

			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void disableJob(Job<?, ?> job) {

		if (job == null)
			return;

		logger.log(Level.INFO, "Disable job " + job + ".");

		try {
			AbstractProject<?, ?> project = (AbstractProject<?, ?>) job;
			project.disable();
			return;

		} catch (IOException | ClassCastException e) {
			logger.log(Level.INFO, "Cannot disable " + job.getName() + ".");
			return;
		}
	}

	private void deleteJob(Job<?, ?> job) {

		if (job == null)
			return;

		logger.log(Level.INFO, "Delete job " + job.getName() + ".");

		try {
			job.delete();
			return;
		} catch (InterruptedException | IOException e) {
			logger.log(Level.WARNING,
					"Failed to delete job " + job.getName() + ".", e);
		}

		return;
	}

}
