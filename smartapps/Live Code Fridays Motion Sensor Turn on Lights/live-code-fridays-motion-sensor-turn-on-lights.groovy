/**
 *  Live Code Fridays Motion Sensor Turn on Lights
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
 *  Choose a motion sensor that on motion triggers lights to turn on, and after X minutes, turn off if no motion
 */
 
definition(
    name: "Live Code Fridays Motion Sensor Turn on Lights",
    namespace: "pstuart",
    author: "Patrick Stuart",
    description: "A motion sensor turns on lights",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("SmartApp On/Off") {
    	input "smartapp", "bool", title: "Turn SmartApp On?"
    }
	section("Motion Sensors") {
		input "motionSensors", "capability.motionSensor",
            title: "Motion sensors?", multiple: true
	}
    section("Lights") {
		input "lights", "capability.switchLevel",
            title: "Lights?", multiple: true
	}
    section("Duration Lights Stay On, in Minutes") {
    	input "duration", "enum", defaultValue : 2, title: "Duration", options: [["1":"1 Minute"],"2","3","4","5","6","7","8","9","10"]
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
    if (smartapp) {
    	subscribe(app, testFunction)
    	subscribe(motionSensors, "motion", testFunction)
        }
}

// TODO: implement event handlers
def testFunction(evt) {
	if (smartapp) {
        log.debug "testFunction Hit. $evt.value"
        log.debug "Duration is set to $duration in minutes that is in seconds:"
        log.debug duration.toInteger() * 60
        /*
        if (evt.value == "touch") 
        {
            log.debug lights.currentValue("switch")
            //lights.on()
            def lighton = false
            lights.each {
                log.debug it.currentValue("switch")

                if (it.currentValue("switch") == "on") {
                    lighton = true
                }
            }
            if (lighton) 
                { lights.off() } else { lights.on() }
        }
        */

        // If motion is detected turn on the lights
        if (evt.value == "active")
        {
            log.debug "Motion detected, time to turn on the lights"
            lights.on()

            runIn(duration.toInteger() * 60, lightsOff("timer"))

        }

        // If motion is no longer detected, turn off lights

        if (evt.value == "inactive")
        {
            log.debug "Motion no longer detected, time to turn off the lights"

            lightsOff("inactive")
        }
    }
}

def lightsOff(motion) {
    if (smartapp) {
    //check to see if there is motion, if so, reset timer, otherwise turn off lights
        log.debug "Timer lights off was hit with motion sensor state is $motion"

        if(motion == "timer") {
            motionSensors.each {
                if (it.currentValue("motion") == "active") { 
                        lights.on() 
                        unschedule(lightsOff)
                        runIn(duration.toInteger() * 60, lightsOff("timer"))
                    }
            }
        }
        if(motion == "inactive") {
            lights.off()
        }
    }
}