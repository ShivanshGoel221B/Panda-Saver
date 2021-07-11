package com.goelapplications.igsaver.utils

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.goelapplications.igsaver.constants.Constants
import com.goelapplications.igsaver.constants.Constants.URL_TOKEN
import com.goelapplications.igsaver.constants.Keys
import com.goelapplications.igsaver.constants.Keys.CHILDREN
import com.goelapplications.igsaver.constants.Keys.DISPLAY_URL
import com.goelapplications.igsaver.constants.Keys.EDGES
import com.goelapplications.igsaver.constants.Keys.NODE
import com.goelapplications.igsaver.constants.Keys.TYPE
import com.goelapplications.igsaver.constants.Keys.VIDEO_URL
import com.goelapplications.igsaver.constants.MediaType
import com.goelapplications.igsaver.listeners.MediaTypeListener
import com.goelapplications.igsaver.listeners.RootListener
import com.goelapplications.igsaver.listeners.SideCarListener
import com.goelapplications.igsaver.listeners.UrlListener
import com.goelapplications.igsaver.models.MediaModel
import org.json.JSONException
import org.json.JSONObject

class UrlParser(context: Context, url: String) {

    private val requestQueue: RequestQueue = Volley.newRequestQueue(context)
    private val jsonUrl = url.split('?')[0] + URL_TOKEN

    fun getRoot(listener: RootListener) {
        val jsonListener = Response.Listener<JSONObject> {
            try {
                listener.jsonRetrieved(it.getJSONObject(Keys.ROOT).getJSONObject(Keys.MEDIA))
            } catch (e: JSONException) {
                listener.onError()
            }
        }
        val errorListener = Response.ErrorListener {
            listener.onError()
        }

        val request = JsonObjectRequest(
            Request.Method.GET, jsonUrl,
            null, jsonListener, errorListener)
        requestQueue.add(request)
    }

    fun findMediaType(jsonObject: JSONObject, listener: MediaTypeListener) {
        try {
            when (jsonObject.getString(TYPE)) {
                Constants.TYPE_IMAGE -> listener.mediaTypeFound(MediaType.TYPE_IMAGE)
                Constants.TYPE_VIDEO -> listener.mediaTypeFound(MediaType.TYPE_VIDEO)
                Constants.TYPE_MULTIPLE -> listener.mediaTypeFound(MediaType.TYPE_SIDECAR)
                else -> { listener.onError() }
            }
        } catch (e: JSONException) {
            listener.onError()
        }
    }

    fun getDownloadUrl(jsonObject: JSONObject, type: MediaType, listener: UrlListener) {
        val url: String
        when (type) {
            MediaType.TYPE_IMAGE -> {
                url = jsonObject.getString(DISPLAY_URL)
                listener.urlRetrieved(url)
            }
            MediaType.TYPE_VIDEO -> {
                url = jsonObject.getString(VIDEO_URL)
                listener.urlRetrieved(url)
            }
            else -> {
                return
            }
        }
    }

    fun getMediaList(jsonObject: JSONObject, listener: SideCarListener) {
        val objectsNode = jsonObject.getJSONObject(CHILDREN).getJSONArray(EDGES)
        val count = objectsNode.length()
        listener.onCountComplete(count)
        for (i in 0 until count) {
            val node = objectsNode.getJSONObject(i).getJSONObject(NODE)
            findMediaType(node, object : MediaTypeListener {
                override fun mediaTypeFound(type: MediaType) {
                    val model = MediaModel(mediaType = type)
                    getDownloadUrl(node, type, object : UrlListener {
                        override fun urlRetrieved(url: String) {
                            model.downloadUrl = url
                            getThumbnailUrl(node, model, listener)
                        }
                        override fun onError() { listener.onError() }
                    })
                }
                override fun onError() { listener.onError() }
            })
        }
    }

    private fun getThumbnailUrl(jsonObject: JSONObject,
                                model: MediaModel,
                                listener: SideCarListener) {
        model.thumbnailUrl = jsonObject.getString(DISPLAY_URL)
        listener.onMediaRetrieved(model)
    }
}