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
import hudson.model.Item;
import hudson.model.Job;
import jenkins.model.Jenkins;

public class JobHandling {

	private Logger logger = Logger.getLogger(JobHandling.class.getName());

	public boolean performJobHandling(Map<String, String> jobs) {

		try {
			Iterator<Map.Entry<String, String>> iter = jobs.entrySet()
					.iterator();

			while (iter.hasNext()) {
				Map.Entry<String, String> job = iter.next();
				String jobname = job.getKey();
				String jobaction = job.getValue();

				switch (jobaction) {
					case "disable" :
						disableJob(jobname);
						break;
					case "delete" :
						deleteJob(jobname);
				}

			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void disableJob(String job) {

		logger.log(Level.INFO, "Disable job " + job + ".");

		AbstractProject<?, ?> project;
		try {
			for (Item item : Jenkins.getInstance().getAllItems()) {
				if (item.getName().equals(job)) {
					project = (AbstractProject<?, ?>) item;

					try {
						project.disable();
						return;
					} catch (IOException e) {
						logger.log(Level.WARNING,
								"Failed to disable job " + job + ".", e);
						return;
					}
				}
			}
			logger.log(Level.INFO, "Job " + job + " not found.");
			return;
		} catch (ClassCastException e) {
			logger.log(Level.INFO, "Cannot disable " + job + ".");
			return;
		}
	}

	private void deleteJob(String job) {
		logger.log(Level.INFO, "Delete job " + job + ".");

		for (Item item : Jenkins.getInstance().getAllItems()) {
			if (item.getName().equals(job)) {
				Job<?, ?> project = (Job<?, ?>) item;

				try {
					project.delete();
					return;
				} catch (InterruptedException | IOException e) {
					logger.log(Level.WARNING,
							"Failed to delete job " + job + ".", e);
					return;
				}
			}
		}
		logger.log(Level.INFO, "Job " + job + " not found.");
		return;
	}

}
