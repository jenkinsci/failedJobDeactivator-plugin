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
		f.section(title:_("Failed Job Deactivator"))
		
		f.optionalBlock(title:_("Configure Failed Job Deactivator"), field:"optionalBlock", checked: instance?.isConfigured){
			
			f.entry(field:"active"){
				f.checkbox(title:_("Activate the automated deactivation or deletion"), default:"true")
			}
		
			
			f.nested(){
     			table() {
     			
     				f.entry(title:_("Last successful build run"), field:"lastSuccessfulBuild", 
     					description:_("Value in days. Default value is")+" "+"${descriptor.getGlobalLastSuccessfulBuild()}"+"."){
     						f.textbox(default:"${descriptor.getGlobalLastSuccessfulBuild()}")
     				}
  
  					f.entry(title:_("Last manual triggered build run"), field:"lastManuallyTriggered", 
  						description:_("Value in days. Default value is")+" "+"${descriptor.getGlobalLastManuallyTriggered()}"+"."){
  							f.textbox(default:"${descriptor.getGlobalLastManuallyTriggered()}")
  					}
	
					f.entry(title:_("Email address for notification"), field:"userNotification", 
						description:_("Comma separated list of email addresses.")){
							f.textbox()
					}	
     			}
   			}	
		}
		
		f.section(title:"")
	}