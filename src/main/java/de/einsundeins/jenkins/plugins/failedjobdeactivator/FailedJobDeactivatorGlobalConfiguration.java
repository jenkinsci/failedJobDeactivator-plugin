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

import javax.servlet.ServletException;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

@Extension
public class FailedJobDeactivatorGlobalConfiguration
		extends
			GlobalConfiguration {

	private boolean deleteJobsWithoutBuilds;

	private int lastSuccessfulBuild;

	public FailedJobDeactivatorGlobalConfiguration() {
		load();
	}

	@Override
	public final boolean configure(final StaplerRequest req,
			final JSONObject formData) throws FormException {

		this.deleteJobsWithoutBuilds = formData
				.getBoolean("deleteJobsWithoutBuilds");
		try {
			this.lastSuccessfulBuild = formData.getInt("lastSuccessfulBuild");
		} catch (JSONException e) {
			this.lastSuccessfulBuild = Constants.DEFAULT_LAST_SUCCESSFUL_BUILD;
		}

		save();
		return true;
	}

	public boolean getDeleteJobsWithoutBuilds() {
		return deleteJobsWithoutBuilds;
	}

	public int getLastSuccessfulBuild() {
		return lastSuccessfulBuild;
	}

	public int getDefaultLastSuccessfulBuild() {
		return Constants.DEFAULT_LAST_SUCCESSFUL_BUILD;
	}

	public static FailedJobDeactivatorGlobalConfiguration get() {
		return GlobalConfiguration.all()
				.get(FailedJobDeactivatorGlobalConfiguration.class);
	}

	@Override
	public final String getDisplayName() {
		return Messages.displayName();
	}

	public FormValidation doCheckLastSuccessfulBuild(@QueryParameter int value)
			throws IOException, ServletException {
		if (value < 1) {
			return FormValidation
					.error(Messages.errorMessageFormValidationDeadline());
		}
		return FormValidation.ok();
	}
}
