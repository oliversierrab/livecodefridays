/**
 *  LCF HTML Tile Test
 *
 *  Copyright 2016 Patrick Stuart
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
metadata {
	definition (name: "LCF OAUTH HTML Tile Test", namespace: "pstuart", author: "Patrick Stuart") {
		capability "Button"
		capability "Polling"
		capability "Refresh"
        
        command "getWeather"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles (scale:2){
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 3) {
        	state "battery", label:'${currentValue}% battery', unit:""
    	}/*
        valueTile("battery2", "device.battery", decoration: "flat", inactiveLabel: false) {
        	state "battery2", label:'${currentValue}% battery', unit:""
    	}*/
    	standardTile("refresh", "getWeather", inactiveLabel: false, decoration: "flat") {
        	state "default", action:"getWeather", icon:"st.secondary.refresh"
    	}
        htmlTile(name: "tileHtml", action: "getHtml", width:6, height: 5, whitelist:
["https://code.jquery.com"])
        
        main(["battery"])
    details(["tileHtml"]) //"battery", "battery2", "refresh", 
	}
}

mappings {
	path("/getHtml") {
    	action: [GET: "getHtml"]
    }
    path("/response") {
    	action: [GET: "getResponse", POST: "getResponse"]
    }
    path("/getInitialData") {
		 action:[GET: "getInitialData" ]
    }
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'button' attribute

}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
	// TODO: handle 'poll' command
}

def refresh() {
	log.debug "Executing 'refresh'"
	// TODO: handle 'refresh' command
}

def getWeather() {
	def weather = getWeatherFeature("conditions", "55311")
	log.debug weather	
    state.weather = weather.current_observation
}

def getWeatherDetail() {
	def details = "<table>"
    state.weather.each { key,item ->
    	details = details + "<tr><td>$key :</td><td>$item</td></tr>"
    }
    details = details + "</table>"
    log.debug details
    return details
}
def getHtml() {
	//todo generate html
    //<form action="${buildRedirectUrl("response")}">
    
    getWeather()
    renderHTML {
            head {
				"""
                <script src="https://code.jquery.com/jquery-2.1.4.min.js"></script>
                <style type="text/css">
                	#header { font-size: 1.5em; font-weight: bold }
                    #weather { font-size: 1em }
                    #square{
 margin-top:50px;
 width: 20%;
 height: 0;
 padding-bottom: 20%;
 border:solid 1px #666;
 border-radius: 50%;
 background: #eee;
 }
 /*This is used to style the button div*/
 #button{
 width: 90%;
 height: 0;
 padding-bottom: 90%;
 border:solid 1px #000;
 border-radius: 50%;
 margin:3.75%;
26
 background:#fff;
 }
 /*When the switch is turned on, we'll apply this class to the button to
turn it green.*/
 .on{
 background:#79b821 !important;
 }
 /*The div for our status text that displays the current state of the
switch.*/
 #current-state{
 font-size:20px;
 color:#666;
 position: absolute;
 top: 62%;
 left: 12%;
 transform: translateX(-50%) translateY(-50%);
 -webkit-transform: translateX(-50%) translateY(-50%);
 width:auto;
 }
 body {
 background-color: #A0A0A0;
 font-family: arial;
 -webkit-tap-highlight-color: rgba(0,0,0,0);
 }
 #beta {
 background-color: #FFFFFF;
 -moz-border-radius: 25px;
 -webkit-border-radius: 25px;
 padding: 1px 10px;
 color: #A0A0A0;
 font-family: arial;
 }
 .beta-div {
 width: 20%;
 float: left;
 padding-top: 3%;
 padding-left: 2%
 }
 .word-div {
 width: 75%;
 float: right;
 margin-left: 2px;
 font-size: 85%;
 }
 .full-div {
 width: 100%;
27
 display: inline;
 }
 .white {
 color: #FFFFFF;
 }
                </style>
                <script>
 				\$(document).ready(function() { 
 						\$("#jqueryVersion").text("jQuery version: " + \$.fn.jquery);
 });
 function eventReceived(evt) {
 console.log(evt);
 APP.eventReceiver(evt);
 }
 </script>
                """
            }
            body { 
            """<div id="header">Weather 2.0 now with HTML</div>
            <div id="path">The path is: <script>document.write(window.location.href);
            var req = new XMLHttpRequest();
			req.open('GET', document.location, false);
			req.send(null);
			var headers = req.getAllResponseHeaders().toLowerCase();
            document.write(headers);
            document.write(document.documentURI);
            document.write(document.URL);
            </script></div>
            <div id="inputform">
            	<form action="">
                	<input type="submit" value="Submit">
                </form>
            <div id='weather'>
            	Wind Gust: ${state.weather.wind_gust_mph} mph <br/>
                Current Condition: ${state.weather.weather}<br/>
                ${getWeatherDetail()}
            </div>
            <div class="word-div white" id="jqueryVersion">
            <div id="square">
             <div id="button">
             <div id="current-state"></div>
             </div>
             </div>
            <script>
 			var APP = {
 						langs: {
                             "en-us":{
                             "on": "On",
                             "off": "Off"
                             }
                         },
                         currentLang: null,
                         init: function() {
                         document.addEventListener("touchstart", function(){}, true);
                         APP.getLang();
                         ST.request("getInitialData")
                         .success(function(data){
                         APP.render(data.currentState);
                         APP.addBindings();
                         })
                         .GET();
                         },
                         getLang: function() {
                         for (var key in APP.langs) {
                         if (APP.langs.hasOwnProperty(key)) {
                         if(navigator.language == key){
                         APP.currentLang = key;
                         return;
                         }
                         }
                         }
                         APP.currentLang = "en-us";
                         },
                         render: function(state){
                         if (state == "on"){
                         \$("#button").addClass("on");
                         } else{
                         \$("#button").removeClass("on");
                         }
                         \$("#current-state").text(APP.langs[APP.currentLang][state]);
                         },
                         addBindings: function(){
                         \$("#button").on("touchend", function(){
                         var isOn = \$("#button").hasClass("on");
                         if(isOn) {
                         ST.action("off");
                         } else {
                         ST.action("on");
                         }
                         });
                         },
                        29
                         eventReceiver: function(evt){
                         switch(evt.name){
                         case "switch":
                         APP.render(evt.value);
                         break;
                         }
                         }
                         }
                         APP.init();
                         
                         </script>
            
            """
            }
        }
}

def buildRedirectUrl(endPoint) {
	log.debug "In buildRedirectUrl"
    log.debug "apiServerURL: ${apiServerUrl("/response")}"
    
    //def accessToken = createAccessToken()
    //log.debug accessToken
    //log.debug getServerUrl() + "/api/token/${state?.accessToken}/devices/installations/${app.id}/${endPoint}"
    //return getServerUrl() + "/api/token/${state?.accessToken}/devices/installations/${app.id}/${endPoint}"
    return "https://graph.api.smartthings.com/api/devices/installations/447114cb-ae53-4492-881e-89ec11a92349"
}

def getResponse(evt) {
	log.debug evt
    renderHTML {
    	head {}
        body { 
        	"""got response ${evt}, cool!"""
        }
    }
}