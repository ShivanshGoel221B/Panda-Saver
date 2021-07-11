package com.goelapplications.igsaver.listeners

import com.goelapplications.igsaver.models.MediaModel

interface SideCarListener {
    fun onCountComplete(count: Int)
    fun onMediaRetrieved(model: MediaModel)
    fun onError()
}