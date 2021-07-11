package com.goelapplications.igsaver.listeners

import com.goelapplications.igsaver.constants.MediaType

interface MediaTypeListener {
    fun mediaTypeFound(type: MediaType)
    fun onError()
}