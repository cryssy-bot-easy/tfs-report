package com.incuventure.utilities

import java.util.Map;
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.JSON

class WebAPIService {

	def grailsApplication
	
    def dummySendQuery(Map param, action, String... object) {

        if (param == null) {
            param = [:]
        } 

        StringBuilder sb = new StringBuilder()
        sb.append("${grailsApplication.config.tfs.core.api.webapi.url}")
		
        for (int idx = 0; idx < object.length; idx++) {
            sb.append(object[idx])
        }

        def tfscore = new RESTClient(sb.toString())

        def resp = tfscore.get(path: action,
                requestContentType: JSON,
                query: param)

        // convert from JSON to Map
        Gson gson = new Gson()
        Map returnData = gson.fromJson(resp.data.toString(), Map.class)

        return returnData

    }
	
	def dummySendCommand(Map param, action, String... object) {
		if (param == null) {
			param = [:]
		}

		StringBuilder sb = new StringBuilder()
		sb.append("${grailsApplication.config.tfs.core.api.webapi.url}")
		
		for (int idx = 0; idx < object.length; idx++) {
			sb.append(object[idx])
		}

		def tfscore = new RESTClient(sb.toString())

		def resp = tfscore.post(path: action,
				requestContentType: JSON,
				body: param)

		// convert from JSON to Map
		Gson gson = new Gson()
		Map returnData = gson.fromJson(resp.data.toString(), Map.class)

		return returnData
	}
	
}
