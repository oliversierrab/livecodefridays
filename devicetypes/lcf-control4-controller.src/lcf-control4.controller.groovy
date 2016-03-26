/**
 *  LCF Control4 Controller
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
	definition (name: "LCF Control4 Controller", namespace: "pstuart", author: "Patrick Stuart") {
		capability "Button"
		capability "Polling"
		capability "Refresh"
        
        command "armSystem"
        command "armSystemToStay"
        command "disarmSystem"
        command "getAlarmStatus"
        
        attribute "alarmStatus", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale:2) {
    	valueTile("blank", "blank", decoration: "flat", width: 1, height:2) {
			state "default", label:''
		}
        valueTile("alarmStatus", "device.alarmStatus", decoration: "flat", width: 4, height:2) {
			state "default", label:'${currentValue}'
		}
        standardTile("armSystem", "device.button", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
        	state "default", action:"armSystem", label:"Arm To Away" //icon:"st.Home.home3"  
    	}
        standardTile("armSystemToStay", "device.button", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
        	state "default", action:"armSystemToStay", label:"Arm To Stay" //icon:"st.Home.home3"  
    	}
        standardTile("disarmSystem", "device.button", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
        	state "default", action:"disarmSystem", label:"Disarm"
    	}
        standardTile("getAlarmStatus", "device.button", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
            state "default", action:"getAlarmStatus", label:"Alarm Status?"
        }
        main "alarmStatus"
        details([ "blank","alarmStatus", "blank", "armSystem", "armSystemToStay", "disarmSystem" , "getAlarmStatus" ])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
    def msg = parseLanMessage(description)
    //log.debug msg
    //log.debug "XML response is $msg.xml"
    //log.debug msg.body
	// TODO: handle 'button' attribute.
    if(msg.xml) {
    	def xml = msg.xml
        log.debug xml.response
        log.debug xml.command
        //log.debug xml.getProperties()
        if ( xml.command == "getAlarmStatus") {
            //log.debug xml.values.displayText
            def displayText = xml.values.displayText
            log.debug displayText
            sendEvent(name: "alarmStatus", value: displayText)
        } else {
        	switch(xml.command) {
            case "armSystem" : 
            	sendEvent(name: "alarmStatus", value: "Armed to Away")
            	break
            case "disarmSystem" :
            	sendEvent(name: "alarmStatus", value: "System Disarmed")
            	break
            case "armSystemToStay" : 
            	sendEvent(name: "alarmStatus", value: "Armed to Stay")
            	break
            
            }
        	//return [getAlarmStatus()]
        }
    }
}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
	// TODO: handle 'poll' command
    refresh()
}

def refresh() {
	log.debug "Executing 'refresh'"
	// TODO: handle 'refresh' command
    getAlarmStatus()
    //sendEvent(name: "alarmStatus", value: "Alarm State is Unknown") //, descriptionText: "Alarm State is Unknown")
}

def getAlarmStatus() {
	//TODO implement alarm status as a return value
    def headers = [:]
    headers.put("HOST", "192.168.101.165:8085")
    headers.put("Accept", "text/xml")
    def hubAction = new physicalgraph.device.HubAction(
            method : "GET",
            path : "/getAlarmStatus",
            headers: headers
        )
        log.debug hubAction
        hubAction
}

def armSystem() {
    def headers = [:]
    headers.put("HOST", "192.168.101.165:8085")
    headers.put("Accept", "text/html")
    def hubAction = new physicalgraph.device.HubAction(
            method : "GET",
            path : "/armSystem",
            headers: headers
        )
        log.debug hubAction
        hubAction
}

def armSystemToStay() {

    def headers = [:]
    headers.put("HOST", "192.168.101.165:8085")
    headers.put("Accept", "text/html")
    def hubAction = new physicalgraph.device.HubAction(
            method : "GET",
            path : "/armSystemToStay",
            headers: headers
        )
        log.debug hubAction
        hubAction
}

def disarmSystem() {
    def headers = [:]
    headers.put("HOST", "192.168.101.165:8085")
    headers.put("Accept", "text/html")
    def hubAction = new physicalgraph.device.HubAction(
            method : "GET",
            path : "/disarmSystem",
            headers: headers
        )
        log.debug hubAction
        hubAction
}
