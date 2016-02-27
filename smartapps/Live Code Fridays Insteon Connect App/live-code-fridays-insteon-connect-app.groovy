/**
 *  Live Code Fridays Insteon Connect App
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
    name: "Live Code Fridays Insteon Connect App",
    namespace: "pstuart",
    author: "Patrick Stuart",
    description: "Live Code Fridays Insteon Connect App",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page(name: "mainPage")
}


def mainPage() {
    dynamicPage(name: "mainPage", uninstall: true) {
    	section {
        	paragraph "Parsed Results go here from buffstatus.xml $status"
            input "username", "text", title: "username", description: "enter username"
            input "password", "password", title: "password", description: "enter password"
            }
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
    
	subscribe(location, null, locationHandler, [filterEvents:false])
    
	def status = findStatus()
}

def parse(evt) {
	log.debug "Parse hit $evt"
    log.debug evt?.body
    log.debug evt?.body.decodeBase64()
    log.debug evt?.body.decodeBase64().substring(14,114)
}

def locationHandler(evt) {
	log.debug "locationHandler hit $evt"
    
    log.debug evt.description
    def parsedEvent = parseLanMessage(evt.description)
    def descMap = parseDescriptionAsMap(evt.description)
    log.debug parsedEvent.body //.decodeBase64()
    log.debug parsedEvent.body.substring(14,114)
    def buff = parsedEvent.body.substring(14,114)
    def indexCount = buff.indexOf("0250")
    while(indexCount >= 0) {
    indexCount = buff.indexOf("0250",indexCount+1)
    def device = buff.substring(indexCount+4,indexCount+10)
    def status = buff.substring(indexCount+15,indexCount+16)
    // either 01 dry 02 wet
    log.debug "I found an index at $indexCount and first device is $device and status is $status"
    }
    
    
    
}

// TODO: implement event handlers
def findStatus() {
	def buff = "01CF110102502DFAEB000001CF110102502DFAEB2CB5EC45110102502DFAEB110101CF060002502DFAEB110101CF06000000"
    //def updates = buff.tokenize('0250')
    
    def ip = "192.168.101.193:25105"
    def userpassascii = "${username}:${password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    def cmd = "/buffstatus.xml"
    sendHubCommand(new physicalgraph.device.HubAction([
		method: "GET",
		path: cmd,
		headers: [
			HOST: ip,
            Authorization: userpass
		]] 
        ))
	//x.options = [type:"LAN_TYPE_TCPCLIENT"]
	//sendHubCommand(x)
    
    def indexCount = buff.indexOf("0250")
    def device = buff.substring(indexCount+4,indexCount+10)
    def status = buff.substring(indexCount+15,indexCount+16) // either 01 dry 02 wet
    
    
    log.debug "There are $indexCount responses and first device is $device and status is $status"
    return "Status"
}


private String convertIPToHex(ipAddress) {
	return Long.toHexString(converIntToLong(ipAddress));
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}
private String convertHexToIP(hex) {
log.debug("Convert hex to ip: $hex") //	a0 00 01 6
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
	def parts = device.deviceNetworkId.split(":")
    log.debug device.deviceNetworkId
	def ip = convertHexToIP(parts[0])
	def port = convertHexToInt(parts[1])
	return ip + ":" + port
}

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
	def nameAndValue = param.split(":")
	map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
}
}