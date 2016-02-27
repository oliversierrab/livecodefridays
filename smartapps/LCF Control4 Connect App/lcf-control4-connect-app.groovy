/**
 *  LCF Control4 Connect App
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
    name: "LCF Control4 Connect App",
    namespace: "pstuart",
    author: "Patrick Stuart",
    description: "LCF example Control4 Connect App to spawn hub child device",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    singleInstance: true)


preferences {
	section("Add a Control4 Controller") {
		input "c4MAC", "text", title: "Control4 Controller MAC", defaultValue:"000fff586754"
        input "hub", "hub", title: "SmartThings Hub"
		//input "c4Ip", "text", title: "Control4 Controller IP"
		//input "c4Port", "text", title: "Control4 Controller Port"
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
	log.debug "hit initialize"
    
    subscribe(location, "alarmSystemStatus", alarmHandler)
    subscribe(location, "stream", alarmHandler)
    subscribe(location, "activity", alarmHandler)
    subscribe(location, "battery", alarmHandler)
    subscribe(security, "intrusion", alarmHandler)
    log.debug "alarm state: ${location.currentState("alarmSystemStatus")?.value}"
    
    def s = "${this.state.getProperties()}"
    def l = s.toList().collate( 300 )*.join()
    l.each {log.debug "Location: ${it}"}
    
    try {
        log.debug "found device deviceId"
        def MAC = "000fff586754"
        def existingDevice = getChildDevice(MAC)
        if(!existingDevice) {
            def hubId = hub.id
            log.debug hub.id
            def childDevice = addChildDevice("pstuart", "LCF Control4 Controller", MAC, hubId, [name: "LCF C4 Controller", label: "LCFC4Controller", completedSetup: true])
        	log.debug "C4 controller device installed"
        } else
        {
        	log.debug "Already installed"
        }
    } catch (e) {
        log.error "Error creating device: ${e}"
    }
    
}

// TODO: implement event handlers
def alarmHandler(evt) {
	//log.debug "Verify Event name: ${evt.name}"
	//log.debug "Verify Event value: ${evt.value}"
    log.debug evt
    log.debug evt.getProperties()
    log.debug evt.date // Sun Mar 01 22:43:37 UTC 2015
    log.debug evt.name // motion (capability type)
    log.debug evt.displayName // name of the device in ST "Office aeon multi"
    log.debug evt.value // the value of the capability type, open close inactive, active, etc.
    log.debug evt.descriptionText // ex. Master Bath 1 switch is on
    log.debug evt.description // zigbee or zwave raw data
    log.debug evt.unit // could F or others
    log.debug evt.type // null?
    log.debug evt.user // null?
}

def doReflection(obj)  
{    
   def methods = obj.getProperties()  
   def methodsNames = new StringBuilder()
   methodsNames << "Reflection:"  
   //methodsNames << "\tClass Name: ${obj.class.name}"
   methods.each  
   {  
      methodsNames << "${it} " 
   }  

   methodsNames
}  