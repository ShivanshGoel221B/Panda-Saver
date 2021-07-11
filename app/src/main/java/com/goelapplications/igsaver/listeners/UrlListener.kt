package com.goelapplications.igsaver.listeners

interface UrlListener {
    fun urlRetrieved(url: String)
    fun onError()
}