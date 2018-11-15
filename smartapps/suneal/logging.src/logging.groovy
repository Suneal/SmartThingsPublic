definition(
    name: "Logging",
    namespace: "Suneal",
    author: "Sunil Manandhar",
    description: "Logs everything!",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    section("Motion Detector") {
        input "themotion", "capability.motionSensor", required: true, title: "Where?"
    }
    section("Turn off when there's been no movement for") {
        input "minutes", "number", required: true, title: "Minutes?"
    }
    section("Switch") {
        input "theswitch", "capability.switch", required: true
    }
   section("Bulb") {
       input "thebulb", "capability.switch", required: true
       input "thebulb", "capability.color", required: true
  }
    section("Arrival") {
        input "thearrival", "capability.presenceSensor", required: true 
    }
    
    section("MultiPurpose") {
        input "thecontact", "capability.contactSensor", required: true
    }
    
    section("Lock") {
        input "thelock", "capability.lock", required: true
    }
    
    
}

def installed() {
	log.debug "installed with settings: $settings"
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(themotion, "motion", motionDetectedHandler)
    subscribe(theswitch, "switch", switchHandler)
    subscribe(thearrival, "presence", arrivalHandler)
    subscribe(thecontact, "contact", contactHandler)
    subscribe(thebulb, "switch", bulbHandler)
  //  subscribe(thebulb, "color", bulbHandler)
    subscribe(thelock, "lock", lockHandler)
    subscribe(location, "routineExecuted", routineChanged)
}

def routineChanged(evt) {
    log.debug "routineChanged: $evt"

    sendPostRequest(app.getId().toString(), app.getName().toString(), evt, "routineChanged", "Routine", location.mode, settings.toString(),evt.isoDate)

    // name will be "routineExecuted"
    log.debug "evt name: ${evt.name}"

    // value will be the ID of the SmartApp that created this event
    log.debug "evt value: ${evt.value}"

    // displayName will be the name of the routine
    // e.g., "I'm Back!" or "Goodbye!"
    log.debug "evt displayName: ${evt.displayName}"

    // descriptionText will be the name of the routine, followed by the action
    // e.g., "I'm Back! was executed" or "Goodbye! was executed"
    log.debug "evt descriptionText: ${evt.descriptionText}"
}


def motionDetectedHandler(evt) {
	log.debug("AALOO" + app.getId() + app.getName())
    sendPostRequest(app.getId().toString(), app.getName().toString(), evt.name +"."+ evt.value, "motionDetectedHandler", themotion.displayName, location.mode, settings.toString(),evt.isoDate)
	if(evt.value == "active"){
    	log.debug "Sunil: motionDetectedHandler called: $evt"
    }
    if(evt.value == "inactive"){
    	log.debug "Sunil: motionStopped called: $evt"
    }
}


def switchHandler(evt) {
    log.debug "The installed SmartApp id associated with this event: ${evt.installedSmartAppId}"
    sendPostRequest(app.getId().toString(), app.getName().toString(), evt.name +"."+ evt.value, "switchHandler", theswitch.displayName, location.mode, settings.toString(),evt.isoDate)

    def result = null
    if(evt.value == "on"){
    	log.debug "Sunil: Switch On called: $evt"
		result = "on"
	    }
    if(evt.value == "off"){
    	log.debug "Sunil: Switch Off called: $evt"
        result = "off"
    	}
	
}



def bulbHandler(evt) {
    sendPostRequest(app.getId().toString(), app.getName().toString(), evt.name +"."+ evt.value, "bulbHandler", thebulb.displayName, location.mode, settings.toString(),evt.isoDate)
	if(evt.value == "on"){
    	log.debug "Sunil: Bulb On called: $evt"
    }
    if(evt.value == "off"){
    	log.debug "Sunil: Bulb Off called: $evt"
    }
}


def lockHandler(evt) {
    log.debug "The installed SmartApp id associated with this event: ${evt.installedSmartAppId}"
    log.debug "The installed Desceription id associated with this event: ${evt.description}"
    sendPostRequest(app.getId().toString(), app.getName().toString(), evt.name +"."+ evt.value, "lockHandler", thelock.displayName, location.mode, settings.toString(),evt.isoDate)
}



def arrivalHandler(evt) {
	sendPostRequest(app.getId().toString(), app.getName().toString(), evt.name +"."+ evt.value, "arrivalHandler", thearrival.displayName, location.mode, settings.toString(),evt.isoDate)
    log.debug(evt.value)
	if(evt.value == "present"){
    	log.debug "Sunil: Present called: $evt"
    }
    if(evt.value == "not present"){
    	log.debug "Sunil: Not Present called: $evt"
    }
}


def contactHandler(evt) {
	sendPostRequest(app.getId().toString(), app.getName().toString(), evt.name +"."+ evt.value, "contactHandler", thecontact.displayName, location.mode, settings.toString(), evt.isoDate)
    log.debug(evt.value)
	if(evt.value == "closed"){
    	log.debug "Sunil: Contact Closed called: $evt"
    }
    if(evt.value == "open"){
    	log.debug "Sunil: Contact Opened called: $evt"
    }
}



String getAppStates() {
	def appStates = '{'
	state.each {key, val ->
    	appStates = appStates + "$key:$val,"
	}
    //Remove Last Element
    def lengthMinus2 = appStates.length() - 2
    appStates = appStates.getAt(0..lengthMinus2) +"}"
    return appStates
}

def sendPostRequest(appId, appName, command, funcName, displayName, currentMode,settings, currentTime){
	def appStates = getAppStates()
    // def currentDate = new Date()
    // def currentTime = currentDate.format("yyyy-MM-dd'T'HH:mm:00XX", TimeZone.getTimeZone('UTC')).toString()
	def params = [
    uri: "http://173.255.245.197:9000/all_events/create/",
    body: [
        appId: appId,
		name: appName,
        timestamp: currentTime,
        command: command,
        funcName: funcName,
        displayName: displayName,
        currentMode: currentMode,
        settings: settings,
        appStates: appStates,
    	]
	]
	try {
    httpPost(params) { resp ->
       // log.debug "response data: ${resp.data}"
        //log.debug "response contentType: ${resp.contentType}"
    	}
	} catch (e) {
    log.debug "something went wrong: $e"
	}
}


//Useless for now:


private getEventDesc(event) {
	log.debug("*****************" + event.descriptionText + " -" + event.installedSmartAppId + "--"+ event.source)
	if (settings?.useValueUnitDesc != false) {
		return "${event.value}" + (event.unit ? " ${event.unit}" : "")
	}
	else {
		def desc = "${event?.descriptionText}"
		if (desc.contains("{")) {
			desc = replaceToken(desc, "linkText", event.displayName)
			desc = replaceToken(desc, "displayName", event.displayName)
			desc = replaceToken(desc, "name", event.name)
			desc = replaceToken(desc, "value", event.value)
			desc = replaceToken(desc, "unit", event.unit)
		}
		return desc
	}
}

private replaceToken(desc, token, value) {
	desc = "$desc".replace("{{", "|").replace("}}", "|")
	return desc.replace("| ${token} |", "$value")
}