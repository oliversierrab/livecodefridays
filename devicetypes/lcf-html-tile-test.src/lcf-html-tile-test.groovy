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
	definition (name: "LCF HTML Tile Test", namespace: "pstuart", author: "Patrick Stuart") {
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
        htmlTile(name: "tileHtml", action: "getHtml", width:6, height: 5)
        
        main(["battery"])
    details(["tileHtml"]) //"battery", "battery2", "refresh", 
	}
}

mappings {
	path("/getHtml") {
    	action: [GET: "getHtml"]
    }
    path("/response") {
    	action: [GET: "getResponse"]
        action: [POST: "getResponse"]
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
    getWeather()
    renderHTML {
            head {
				"""
                <style type="text/css">
                	#header { font-size: 1.5em; font-weight: bold }
                    #weather { font-size: 1em }
                </style>
                """
            }
            body {
            """<div id="header">Weather 2.0 now with HTML</div>
            <div id="inputform">
            	<form action="/response">
                	<input type="submit" value="Submit">
                </form>
            <div id='weather'>
            	Wind Gust: ${state.weather.wind_gust_mph} mph <br/>
                Current Condition: ${state.weather.weather}<br/>
                ${getWeatherDetail()}
            </div>"""
            }
        }
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