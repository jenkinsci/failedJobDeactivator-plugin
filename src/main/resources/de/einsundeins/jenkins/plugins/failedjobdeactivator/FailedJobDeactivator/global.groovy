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

	p(){
		f.section(title:"Failed Job Deactivator")
		
		f.entry(title:_("Note")+":"){
			text(_("To send e mails, the mailer plugin has to be configured correctly (SMTP server and reply-to address)")+"!")
		}
		
		f.entry(title:_("Email address for notification"), field:"adminNotification"){
			f.textbox()
		}
		
		f.entry(title:_("Delete never built jobs"), field:"deleteNeverBuiltJobs"){
			f.checkbox(checked: descriptor.getDeleteNeverBuiltJobs(), default:true)
		}
		
		f.entry(title:_("Last successful build"), field:"globalLastSuccessfulBuild", 
				description:_("Value in days. Default value is")+" "+ descriptor.getGlobalLastSuccessfulBuild() + "."){
     		f.textbox()
     	}
  
  		f.entry(title:_("Last manual triggered build"), field:"globalLastManuallyTriggered", 
  				description:_("Value in days. Default value is")+" "+ descriptor.getGlobalLastManuallyTriggered() + "."){
  			f.textbox()
  		}
  						
		
		f.advanced(title:_("Failure Causes")){
		
			f.entry(title:_("Delete jobs without detected failure causes"), field:"deleteJobsWithoutFailureCauses"){
				f.checkbox(checked: descriptor.getDeleteJobsWithoutFailureCauses(), default:false)
			}
		
			def i = 0			
			
			while(i < descriptor.getAvailableFailureCauses().size()){
								
				f.entry(title:"${descriptor.getAvailableFailureCauses().get(i).getName()}"){
					
					def config = descriptor.getJobHandlingConfigForFrontend(descriptor.getAvailableFailureCauses().get(i).getId())
					
					select(name:"deleteJob"){
						f.option(value: "Deactivate", selected:(config == 'Deactivate'), _("Deactivate"))
						f.option(value: "Delete", selected:(config == 'Delete'), _("Delete"))
					}
				}
				
				f.invisibleEntry(){
					f.textbox(name:"failureCauseId", value:"${descriptor.getAvailableFailureCauses().get(i).getId()}")
				}
				i++
			}
					
		}
	
	
	}