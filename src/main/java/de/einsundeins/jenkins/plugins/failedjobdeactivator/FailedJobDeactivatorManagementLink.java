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

import hudson.Extension;
import hudson.model.ManagementLink;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Class that generates the management link.
 * @author Jochen A. Fuerbacher
 *
 */
@Extension
public class FailedJobDeactivatorManagementLink extends ManagementLink {

    /**
     * Getter for icon link.
     */
    public String getIconFileName() {

        return "/plugin/failedJobDeactivator/icons/user-trash.png";

    }

    /**
     * Getter for the display name.
     */
    public String getDisplayName() {

        return "Failed Job Deactivator";

    }

    /**
     * Getter for the url.
     */
    @Override
    public String getUrlName() {
        return getUrlName(Stapler.getCurrentRequest());
    }

    /**
     * Getter for the url.
     * @param request
     * @return the url name.
     */
    protected static String getUrlName(final StaplerRequest request) {
        if (request == null) {
            return "/plugin/failedJobDeactivator/";
        }
        return request.getContextPath() + "/plugin/failedJobDeactivator/";
    }

    /**
     * Getter for the description.
     */
    @Override
    public String getDescription() {

        return "Start triggering of detection.";
    }
}