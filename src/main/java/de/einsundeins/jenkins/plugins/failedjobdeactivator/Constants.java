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

/**
 * Class containing constants.
 * 
 * @author Jochen A. Fuerbacher
 *
 */
public final class Constants {

    /**
     * Default value for the deadline of the last manually triggered build.
     */
    public static final int LASTMANUALLYTRIGGERED_DEFAULT = 14;

    /**
     * Default value for the deadline of the last successful build.
     */
    public static final int LASTSUCCESSFULBUILD_DEFAULT = 28;

    /**
     * Convertion from day time to 64 bit unix time. Value = 24 hours * 3600
     * seconds * 1000 ms
     */
    public static final long DAYS_TO_64BIT_UNIXTIME = 86400000L;

    /**
     * Values for the String Array in class FailedJobDeactivator.
     */
    public static final int FAILURE_CAUSE_ID = 0;
    public static final int HOW_TO_HANDLE_JOB = 1;

    /**
     * The default value of handling a job.
     */
    public static final String DEFAULT_HANDLING = "Delete";
    
    public static final boolean DELETE_NEVER_BUILT_JOBS_DEFAULT = true;
    
    /**
     * URL to the icon file.
     */
    public static final String ICON_FILE_ULR = "/plugin/failedJobDeactivator/icons/user-trash.png";
}
