/**
 *  mPower Controller
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
	definition (name: "mPower Controller", namespace: "lcf", author: "Patrick Stuart") {
		capability "Button"
		capability "Energy Meter"
		capability "Polling"
		capability "Power Meter"
		capability "Refresh"
		capability "Switch"
        
        command "getDevices"
        command "doLogin"
        command "doLogout"
        
        command "on"
        command "off"
        
        attribute "status", "string"
        
        (1..8).each { n ->
        	attribute "switch$n", "enum", ["off", "on"]
            attribute "power$n", "number"
            attribute "energy$n", "number"
            attribute "current$n", "number"
            attribute "monthly$n", "number" //TODO add last month as well?
            command "on$n"
            command "off$n"
            
        }
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles (scale:2) {
		valueTile("status", "device.status", decoration: "flat", width:2, height:1) {
        	state "default", label:'${currentValue}'
        }
        standardTile("getDevices", "device.button", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
        	state "default", action:"getDevices", label:"Get Devices"
        }
        standardTile("doLogin", "device.button", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "default", action:"doLogin", label:"Do Login"
        }
        valueTile("switch", "switch", decoration: "flat", width: 2, height: 1) {
            state "default", label:'Outlet'
        }
        valueTile("power", "power", decoration: "flat", width: 1, height: 1) {
            state "default", label:'Watts'
        }
        valueTile("energy", "energy", decoration: "flat", width: 1, height: 1) {
            state "default", label:'Voltage'
        }
        valueTile("current", "current", decoration: "flat", width: 1, height: 1) {
            state "default", label:'Amps'
        }
        valueTile("monthly", "monthly", decoration: "flat", width: 1, height: 1) {
            state "default", label:'Monthly'
        }
            
        (1..8).each { n ->
            standardTile("switch$n", "switch$n", width: 2, height: 1) {
                state "on", label:'${name}', action:"off$n", icon:"st.switches.switch.off", backgroundColor:"#79b821", nextState:"turningOff"
                state "off", label:'${name}', action:"on$n", icon:"st.switches.switch.on", backgroundColor:"#ffffff", nextState:"turningOn"
                state "turningOn", label:'${name}', action:"off$n", icon:"st.switches.switch.off", backgroundColor:"#79b821", nextState:"turningOff"
                state "turningOff", label:'${name}', action:"on$n", icon:"st.switches.switch.on", backgroundColor:"#ffffff", nextState:"turningOn"
                state "offline", label:'${name}', icon:"st.switches.switch.off", backgroundColor:"#ff0000", defaultState: true
            }
            valueTile("power$n", "power$n", decoration: "flat", width: 1, height: 1) {
            	state "default", label:'${currentValue}'
            }
            valueTile("energy$n", "energy$n", decoration: "flat", width: 1, height: 1) {
            	state "default", label:'${currentValue}'
            }
            valueTile("current$n", "current$n", decoration: "flat", width: 1, height: 1) {
            	state "default", label:'${currentValue}'
            }
            valueTile("monthly$n", "monthly$n", decoration: "flat", width: 1, height: 1) {
            	state "default", label:'${currentValue}'
            }
        }
        main "switch1"
        
        def yourDetails = []
        yourDetails << "status"
        yourDetails << "getDevices"
        yourDetails << "doLogin" 
        yourDetails << "switch"
        yourDetails << "power"
        yourDetails << "energy"
        yourDetails << "current"
        yourDetails << "monthly" 
        (1..8).each { n -> 
        	yourDetails << "switch$n"
            yourDetails << "power$n"
            yourDetails << "energy$n"
            yourDetails << "current$n"
            yourDetails << "monthly$n" 
        }
        details(yourDetails)
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
    def msg = parseLanMessage(description, true)
    log.debug msg
    log.debug "Login State is $state.login"
    if (state.login == "pending") {
    	//check for a login parse
        //no body means login success
        //html body means login failed
        if(msg.body) {
        	state.login = "failed"
            log.debug "Login Failed"
        } else {
        	state.login = "loggedIn"
            log.debug "Login Successful"
        }
    }
    
    log.debug msg.data
    if(msg.data) {
    	if (state.pendingEvent) {
        	log.debug state.pendingEvent
            
            def events = []
            
            if (state.pendingEvent.device == "refresh") {
            	//need to parse data object for all device statuses
                msg.data.sensors?.each {
                	def val = "off"
                    if(it.output == 1) { val = "on" }
                    parent.updateVirtual("mPowerVirtualSwitch${it.port}", val)
                    def watts = Math.round(it.power * 100) / 100
                    def current = Math.round(it.current * 100) / 100
                    def volts = Math.round(it.voltage * 100) / 100
                    def monthly = Math.round(it.thismonth * 100) / 100
                    
                    log.debug "Watts is $watts and port is ${it.port}"
                	events << createEvent(name: "power${it.port}", value: watts)
                	events << createEvent(name: "energy${it.port}", value: volts)
                	events << createEvent(name: "current${it.port}", value: current)
                	events << createEvent(name: "monthly${it.port}", value: monthly)
                	events << createEvent(name: "switch${it.port}", value: val, display:true, isStateChange: true)
                }
                events << createEvent(name: "status", value: "refreshed")
            }
            else {
            	log.debug "not a refresh" + state.pendingEvent //["device":id, "state": "on"]
                createEvent(name: "switch${state.pendingEvent.device}", value : state.pendingEvent.state)
                parent.updateVirtual("mPowerVirtualSwitch${state.pendingEvent.device}", state.pendingEvent.state)
            }
            
        }
        
        /*
        if(state.pendingEvent.device) {
        	createEvent(name: "switch", value: state.pendingEvent?.state.toString())
            state.pendingEvent =  null
        }
        */
    }
    

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

def off1() { off(1) }
def off2() { off(2) }
def off3() { off(3) }
def off4() { off(4) }
def off5() { off(5) }
def off6() { off(6) }
def off7() { off(7) }
def off8() { off(8) }

def on1() { on(1) }
def on2() { on(2) }
def on3() { on(3) }
def on4() { on(4) }
def on5() { on(5) }
def on6() { on(6) }
def on7() { on(7) }
def on8() { on(8) }

def on(Integer id) {
	log.debug "Executing 'on'"
    state.pendingEvent = ["device":id, "state": "on"]
	def headers = [:]
        def host = parent.getHostAddress()
        headers.put("HOST", host)
        log.debug state.cookie
        headers.put("Cookie", state.cookie)
        headers.put("Content-Type", "application/x-www-form-urlencoded")
        def data = "output=1"

        def hubAction = new physicalgraph.device.HubAction(
                method : "POST",
                path : "/sensors/$id",
                headers: headers,
                body: data
            )
            log.debug hubAction
            hubAction
    
}

def off(Integer id) {
	log.debug "Executing 'off'"
    state.pendingEvent = ["device":id, "state": "off"]
	def headers = [:]
        def host = parent.getHostAddress()
        headers.put("HOST", host)
        log.debug state.cookie
        headers.put("Cookie", state.cookie)
        headers.put("Content-Type", "application/x-www-form-urlencoded")
        def data = "output=0"

        def hubAction = new physicalgraph.device.HubAction(
                method : "POST",
                path : "/sensors/$id",
                headers: headers,
                body: data
            )
            log.debug hubAction
            hubAction
}

def doLogin() {
	log.debug "doing Login"
    def headers = [:]
    def host = parent.getHostAddress()
    headers.put("HOST",host)
    state.cookie = "AIROS_SESSIONID=01234567890123456789012345678901" //TODO Generate random 32 character cookie
    headers.put("Cookie", state.cookie)
    headers.put("Accept", "text/html")
    headers.put("Content-Type", "application/x-www-form-urlencoded")
    def data = "username=${parent.username}&password=${parent.password}"
    state.login = "pending"
    
    def hubAction = new physicalgraph.device.HubAction(
    	method: "POST",
        path: "/login.cgi",
        headers: headers,
        body: data
    )
    log.debug hubAction
    hubAction
}

def getDevices() {
	log.debug "get Devices Pressed"
    
    (1..8).each { n ->
    	sendEvent(name:"switch$n", value:"offline", displayed: false, isStateChange: false)
        sendEvent(name:"energy$n", value:"offline", displayed: false, isStateChange: false)
        sendEvent(name:"power$n", value:"offline", displayed: false, isStateChange: false)
        sendEvent(name:"monthly$n", value:"offline", displayed: false, isStateChange: false)
        sendEvent(name:"current$n", value:"offline", displayed: false, isStateChange: false)
    }
    
    
    state.pendingEvent = ["device":"refresh","state":"refresh"]
    sendEvent(name: "status", value: "refreshing", descriptionText: "refreshing device list", displayed: true)
    
	def headers = [:]
    def host = parent.getHostAddress()
    headers.put("HOST",host)
    state.cookie = "AIROS_SESSIONID=01234567890123456789012345678901" //TODO Generate random 32 character cookie
    headers.put("Cookie", state.cookie)
    headers.put("Accept", "application/json")
    
    def hubAction = new physicalgraph.device.HubAction(
    	method: "GET",
        path: "/sensors",
        headers: headers
    )
    log.debug hubAction
    hubAction
	
}