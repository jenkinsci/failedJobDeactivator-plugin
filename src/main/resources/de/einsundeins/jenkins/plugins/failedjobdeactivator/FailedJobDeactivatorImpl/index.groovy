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

// Namespaces
st = namespace("jelly:stapler")
j = namespace("jelly:core")
i = namespace("jelly:fmt")
t = namespace("/lib/hudson")
f = namespace("/lib/form")
d = namespace("jelly:define")

import hudson.Util
import hudson.model.Hudson;
import lib.LayoutTagLib

import de.einsundeins.jenkins.plugins.failedjobdeactivator.FailedJobDeactivatorImpl

def l=namespace(LayoutTagLib.class)

l.layout(title: _("Failed Job Deactivator"), secured: "true") {
	
	l.side_panel() {
		l.tasks() {
			l.task(icon: "images/24x24/up.gif", title: _("Back to Dashboard"), href: "${rootURL}/")
		}
	}
	
	l.main_panel() {
		h1(_("Failed Job Deactivator"))
		
		f.form(action: "startDetection") {
		
			if(my.getDescriptor() != null){

				f.entry(title:_("Delete never built jobs"), field:"deleteNeverBuiltJobs"){
					f.checkbox(checked: my.getDescriptor().getDeleteNeverBuiltJobs())
				}
		
				f.entry(title:_("Deadline of last successful build"), field:"globalLastSuccessfulBuild"){
     				f.textbox(default:my.getDescriptor().getGlobalLastSuccessfulBuild())
     			}
  
  				f.entry(title:_("Deadline of last manual triggered build"), field:"globalLastManuallyTriggered"){
  					f.textbox(default:my.getDescriptor().getGlobalLastManuallyTriggered())
  				}
				
			}
		
			f.entry(title:_("Also show deactivated jobs"), field:"showDeactivatedJobs"){
				f.checkbox(default:"false")
			}
			
			f.entry(title:_("Also show excluded jobs"), field:"showExcludedJobs"){
				f.checkbox(default:"false")
			}
			
			f.entry(title:_("Ignore individual deadlines in job config"), field:"forceGlobalDeadlines"){
				f.checkbox(default:"false")
			}
			
			f.entry(){
				f.submit(value:_("Start detection"))
			}
		}
	}
	
}