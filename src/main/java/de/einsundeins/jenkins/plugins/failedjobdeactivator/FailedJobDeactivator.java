package de.einsundeins.jenkins.plugins.failedjobdeactivator;

import java.io.IOException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.Plugin;

public class FailedJobDeactivator extends Plugin {

	public FailedJobDeactivatorGlobalConfiguration getGlobalConfiguration() {
		return FailedJobDeactivatorGlobalConfiguration.get();
	}

	public void doStartDetection(StaplerRequest req, StaplerResponse rsp)
			throws IOException {

		rsp.sendRedirect("showDetectedJobs");

	}

}
