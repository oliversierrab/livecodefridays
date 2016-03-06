/**
 *  Virtual motion Sensor Relay
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
definition(
    name: "Virtual motion Sensor Relay",
    namespace: "lcf",
    author: "Patrick Stuart",
    description: "simple relay from contact to motion",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Sensors") {
		input "motionSensors", "capability.motionSensor", title: "Which Motion Sensors?", multiple: true, required: false
		input "contactSensors", "capability.contactSensor", title: "Which Contact Sensors?", multiple: true, required: false
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
    subscribe(contactSensors, "contact", doSomething)
}

// TODO: implement event handlers
def doSomething(evt) {
	log.debug "in Contact Handler ${evt.data}"
    
    def contactState = contactSensors.contactState //active or inactive
    log.debug contactState
    if(contactState == "open") {
    	motionSensors.motionActive()
        //unschedule()
        //schedule(500, scheduledEvent)
    } else {
    	motionSensors.motionInactive()
    }
}

def scheduledEvent() {
	def contactState = contactSensors.currentValue 
	if(contactState == "closed") {
    	motionSensors.motionInActive()
        }
}