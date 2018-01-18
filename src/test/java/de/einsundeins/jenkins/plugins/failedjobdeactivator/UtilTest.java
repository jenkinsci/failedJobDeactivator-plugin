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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.sonyericsson.jenkins.plugins.bfa.model.FailureCause;
import com.sonyericsson.jenkins.plugins.bfa.model.FailureCauseBuildAction;
import com.sonyericsson.jenkins.plugins.bfa.model.FoundFailureCause;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.queue.QueueTaskFuture;

public class UtilTest {

	@Rule
	public JenkinsRule j = new JenkinsRule();

	@Test
	public void testIsInstanceOfAbstractProject() throws IOException {
		FreeStyleProject job = j.createFreeStyleProject();

		assertTrue(Util.isInstanceOfAbstractProject(job));
	}

	@Test
	public void testIsBuildFailureAnalyzerAvailable() {
		assertTrue(Util.isBuildFailureAnalyzerAvailable());
	}

	@Test
	public void testIsJobConfigHistoryAvailable() {
		assertTrue(Util.isJobConfigHistoryAvailable());
	}
	
	@Test
	public void testIsMavenMultiBranchAvailable() {
		assertTrue(Util.isMavenMultiBranchAvailable());
	}
	
	@Test
	public void testIsWorkflowMultibranchAvailable() {
		assertTrue(Util.isWorkflowMultibranchAvailable());
	}

	@Test
	public void testGetFailureCauses()
			throws IOException, InterruptedException, ExecutionException {
		FreeStyleProject job = j.createFreeStyleProject();

		FailureCause cause1 = new FailureCause("A Name", "A description");
		FoundFailureCause found1 = new FoundFailureCause(cause1);

		FailureCause cause2 = new FailureCause("A second Name",
				"A second description");
		FoundFailureCause found2 = new FoundFailureCause(cause2);

		List<FoundFailureCause> causes = new LinkedList<>();
		causes.add(found1);
		causes.add(found2);

		FailureCauseBuildAction action = new FailureCauseBuildAction(causes);

		QueueTaskFuture<FreeStyleBuild> future = job.scheduleBuild2(0);
		future.get();

		assertTrue(job.getLastBuild() != null);

		FreeStyleBuild build = job.getLastBuild();
		build.addAction(action);
		build.save();

		assertEquals("A Name\nA second Name\n", Util.getFailureCauses(job));
	}

	@Test
	public void testGetJobByName() throws IOException {
		FreeStyleProject job = j.createFreeStyleProject();

		job.renameTo("I am a Job");
		job.save();

		Job<?, ?> job2 = Util.getJobByName("I am a Job");

		assertTrue(job2 != null);
		assertEquals("I am a Job", job2.getDisplayName());
	}

}
