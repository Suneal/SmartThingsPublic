definition(
    name: "Testing Level Bright",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Turn your lights on when motion is detected.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet@2x.png"
)

preferences {
	section("When there's movement...") {
		input "motion1", "capability.motionSensor", title: "Where?", multiple: true
	}
	section("Turn on a light...") {
		input "switch1", "capability.switch", multiple: true
	}
}

def installed()
{
	subscribe(motion1, "motion.active", motionActiveHandler)

}

def updated()
{
	unsubscribe()
    log.debug(motion1.deviceNetworkId)

    //sendEvent(motion1.deviceNetworkId, [name: "motion", value: "active"])
	subscribe(motion1, "motion.active", motionActiveHandler)
        motion1.active()


}

def motionActiveHandler(evt) {

    //for (i = 0; i <3; i++) {
    //	if (i%2 == 0) {
     //   }
     //  System.out.println("Hello World")
   // }

	switch1.on()
    //switch1.off()
    
	sendPostRequest(app.getId().toString(), app.getName().toString(), "switch1.on()", "motionActiveHandler", switch1.displayName, location.mode, settings.toString())
	log.debug("SUNIL called: motionActiveHandler: : switch1.on() with id: " +app.getId() + " and name: " + app.getName())
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

def sendPostRequest(appId, appName, command, funcName, displayName, currentMode,settings){
	def appStates = getAppStates()
    def currentDate = new Date()
    def currentTime = currentDate.format("yyyy-MM-dd'T'HH:mm:00XX", TimeZone.getTimeZone('UTC')).toString()
	def params = [
    uri: "http://173.255.245.197:9000/events/create/",
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