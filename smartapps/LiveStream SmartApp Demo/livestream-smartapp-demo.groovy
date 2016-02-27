/**
 *  LiveStream SmartApp Demo
 *
 *  Copyright 2015 Patrick Stuart
 *	Versions
 *	1.0 - Initial Release 7/31/2015
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
    name: "LiveStream SmartApp Demo",
    namespace: "pstuart",
    author: "Patrick Stuart",
    description: "Overview of SmartApps capabilities",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

{
	appSetting "SMSNumber"
}

preferences {
	section("Notify Me When...") {
		// TODO: put inputs here
        input "startTime", "time"
        
        input "presence", "capability.presenceSensor", title: "Presence Sensors", required: false, multiple: true
        input "motion", "capability.motionSensor", title: "Motion Sensors", required: false, multiple: true
        input "doors", "capability.contactSensor", title: "Doors", required: false, multiple: true
        input "bulbs", "capability.switchLevel", title: "Bulbs", required: false, multiple: true
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
	// TODO: subscribe to attributes, devices, locations, etc.
    
    //Subscribes to the app event and fires function appTouch
    /*
    *
    * This is a multiline comment
    *
    */
    subscribe(app,appTouch)
    subscribe(presence, "presence", areYouHere)
    subscribe(doors, "contact.open", doorCheck)
}

// TODO: implement event handlers


def appTouch(evt) {

	log.debug "App button touched"
    withinTime()
}

def areYouHere(evt) {
	
    def home = false
    if(!state.home) { state.home = true } //NOTE: this is the default state of the code, if no presences are found
    
    presence?.each {
		//log.debug it.displayName
    	//log.debug it.currentValue("presence")
        
        if (it.currentValue("presence") == "present") { home = true }        
    }
    
    if (home) { state.home = true } else { state.home = false }
    log.debug "The value of home is ${state.home}"
    //log.debug "2 + 2 is ${twoPlusTwo()}"
    //If everyone is home, set the state.home to true, else if everyone isn't home, set state.home to false, otherwise set state.home to true
    
}

def twoPlusTwo() {
	return 4 //return the value of 2 + 2
}

def doorCheck(evt) {  //will always be an open action, don't worry about the closed state
	log.debug "hit doorCheck and state.home is ${state.home}"
	if(!state.home) {
    	log.debug "Door ${evt.displayName} is ${evt.value}"
        def phone = appSettings.SMSNumber
        def msg = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890 and one" //141 chars
        log.debug msg.length()
        
        sendSms(phone, msg)
        
    }
}

def withinTime() {
	//def startTime = "9:00am"
    log.debug startTime
    def endTime = "5:00pm"
    def nowDate = new Date()
    log.debug nowDate
    
    def t0 = now()
    log.debug t0.toString()
    
/*def modeStartTime = new Date(state.modeStartTime)
def timeZone = location.timeZone ?: timeZone(timeOfDay)
def startTime = timeTodayAfter(modeStartTime, timeOfDay, timeZone)
def endTime = timeTodayAfter(startTime, endTime ?: "24:00", timeZone)
log.debug "startTime: $startTime, endTime: $endTime, t0: ${new Date(t0)}, modeStartTime: ${modeStartTime},  actionTakenOn: $state.actionTakenOn, currentMode: $location.mode, newMode: $newMode "
*/
    
    def lowerTime = timeToday(startTime, location.timeZone)
    log.debug lowerTime >  nowDate
}