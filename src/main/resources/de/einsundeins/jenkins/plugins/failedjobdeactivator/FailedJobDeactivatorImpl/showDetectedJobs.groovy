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
		h1(_("Failed Job Deactivator - Detected Jobs"))
		
		if(my.getDetectedJobs().size() == 0)
			text(_("No jobs detected."));
		
			
		else{
			f.form(action: "handleJobs") {
			
				text(_("Number of detected jobs") + ": " + my.getDetectedJobs().size());
		
				def i = 0		
				def j = 0
		
		
				adjunct "de.einsundeins.jenkins.plugins.failedjobdeactivator.FailedJobDeactivatorImpl.table"
				table(id:"showDetectedJobs", border:"1px", class:"pane sortable") {
					thead() {
						tr(){
							th(class:"pane-header") {
								text("Job")
							}
							th(class:"pane-header") {
								text(_("Failure causes"))
							}
							th(class:"pane-header") {
								text(_("Days since last build"))
							}
							th(class:"pane-header") {
								text(_("Result of last build"))
							}
							th(class:"pane-header") {
								text(_("Handling"))
							}
							th(class:"pane-header") {
								text(_("Further information"))
							}
						}
					}
		
			
					tbody(){
						while(i < my.getDetectedJobs().size()){
							tr(){
								td(align:"left") {							
									a(href:"${rootURL}/${my.getDetectedJobs().get(i).getaProject().getUrl()}", 
									my.getDetectedJobs().get(i).getaProject().getFullName())
								}
								td(align:"left"){
									text(my.getDetectedJobs().get(i).getFailureCause())
								}
								td(align:"right"){
									text(my.getDetectedJobs().get(i).getTimeOfLastBuild())
								}
								td(align:"left"){
									text(my.getDetectedJobs().get(i).getResultOfLastBuild())
								}
								td(align:"left"){
										def config = my.getDetectedJobs().get(i).isDeleteJob()
										select(name:"handleJob"){
											f.option(value: "Deactivate", selected:(config == false), _("Deactivate"))
											f.option(value: "Delete", selected:(config == true), _("Delete"))
											f.option(value: "Ignore", selected:false, _("Ignore")) //For future features
										}
								}
								td(align:"left"){
									text(_("Deactivated")+ ": " + _(my.getDetectedJobs().get(i).getaProject().isDisabled().toString()))
									br()
									
									def property = my.getProperty(my.getDetectedJobs().get(i).getaProject())
									if(property != null){
									   text(_("Plugin active")+": " + _(property.getActive().toString()))
									}
								}
							}
	
							i++
						}
					}
			
			
				}
				
				f.entry(){
					f.submit(value:_("Start handling jobs"))
				}
			}
		}
	}
	
}