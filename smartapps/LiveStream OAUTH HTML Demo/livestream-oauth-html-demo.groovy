/**
 *  LiveStream OAUTH HTML Demo
 *
 *  Version 1.0 7/24/2015
 *
 *  Copyright 2015 Patrick Stuart
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "LiveStream OAUTH HTML Demo",
    namespace: "pstuart",
    author: "Patrick Stuart",
    description: "LiveStream OAUTH HTML Demo",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)
    
import groovy.json.JsonBuilder

preferences {
	section("Lights") {
		input "switches", "capability.switchLevel", title: "Which Switches?", multiple: true, required: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(switches, "switch", handleEvent) 
    log.debug "apiServerURL: ${apiServerUrl("/html")}"
    log.debug switches[0].getProperties()
}

mappings {
	path("/html") {action: [GET: "html"]}
    path("/updates") {action: [GET: "updates"]}
    path("/:id/:command") {action: [GET: "deviceAction"]}
    path("/revoke") {action: [GET: "revoke"]}
}


def handleEvent(evt) {
	def js = eventJson(evt) //.inspect().toString()
    if (!state.updates) state.updates = []
    def x = state.updates.find { it.id = js.id}  //TODO add find it.name match
    log.debug x
    log.debug "found x"
    if(x) state.updates.remove(x) 
    //state.updates.add(js)
    //log.debug state.updates
  
    
}

private eventJson(evt) {
	def update = [:]
    update.id = evt.deviceId
    update.name = evt.name
    update.value = evt.value
    update.name = evt.displayName
    update.date = evt.isoDate
    log.debug update
    return update
}

def updates() {
	//render out json of all updates since last html loaded
    render contentType: "text/json", data:  new JsonBuilder(state.updates).toPrettyString()
}
def deviceAction() {
	log.debug params.id
    log.debug params.command
    def sw = switches.find {it.id == params.id }
    if (sw.currentValue("switch") == "on") {
    sw.off() 
     render contentType: "text/html", data: """off"""
    }
    else 
    {
    sw.on()
    render contentType: "text/html", data: """on"""
    }
    
    
}

def revoke() {
  	revokeAccessToken()
	log.debug "Current Access Token is now revoked"
    render contentType: "text/plain", data: "token revoked"
}

def html() {
	state.updates = []
	render contentType: "text/html", data: """
    <html>
    <head>
    ${js()}
    </head>
    <body>
    <div id="wrapper">
    <div id="header">
    	Live Code Friday - HTML Remote Control
    </div>
    <div id="remote">
    <div id="lights">
    ${sws()}
    </div>
    </div>
    </div>
    </body>
    </html>
    """
}

def sws() {
	def markup = ""
    switches.each {
    	markup = markup + """
        	<div class="switch" id="switch_${it.id}">
            	<div class="name">${it.displayName}</div>
            	<div class="id">${it.id}</div>
                <div class="status">${it.currentValue("switch")}</div>
                <div class="level">${it.currentValue("level")}</div>
                <div class="actions"><a class="action" href="https://graph.api.smartthings.com/api/smartapps/installations/6412f0eb-5844-4bb0-b914-faa1d2af6d76/${it.id}/toggle">Toggle</a></div>
            </div>
        """
    }
    
    markup
}

def js() { """
<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js"></script>
<script type="text/javascript">
	\$(document).ready(function() {
    	//def url = "https://graph.api.smartthings.com/api/smartapps/installations/" + ${app.id} +
    	alert("ready");
        
        \$(".action").click(function(e) { 
        	 
        	//alert(\$(this).attr('href'));
            var link = \$(this).attr('href');
            //alert(link);
            var x = \$(this)
            
            \$.get( link, function(data) {
            	alert( data );
                //alert (x.parent('.status'));
                x.parents().children('.status').html(data);
            });
            
            
           	e.preventDefault();
            return false;
    	});
    	
    });
</script>

"""
}