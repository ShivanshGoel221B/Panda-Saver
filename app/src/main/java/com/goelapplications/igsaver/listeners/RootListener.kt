package com.goelapplications.igsaver.listeners

import org.json.JSONObject

interface RootListener {
    fun jsonRetrieved(jsonObject: JSONObject)
    fun onError()
}