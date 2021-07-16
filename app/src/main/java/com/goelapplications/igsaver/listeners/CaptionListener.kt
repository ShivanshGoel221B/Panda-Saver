package com.goelapplications.igsaver.listeners

interface CaptionListener {
    fun onCaptionRetrieved(caption: String)
    fun onError()
}