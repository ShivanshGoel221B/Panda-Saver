package com.goelapplications.igsaver.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.goelapplications.igsaver.R
import com.goelapplications.igsaver.constants.MediaType

class DownloadUtil(private val context: Context) {

    fun startDownload(url: String, mediaType: MediaType) {
        val request = DownloadManager.Request(Uri.parse(url))
        request.setTitle(context.getString(R.string.app_name))
        val fileName = System.currentTimeMillis().toString()+
                if (mediaType == MediaType.TYPE_IMAGE) ".jpg"
                else ".mp4"
        val filePath = "PandaSaver/$fileName"
        request.setDescription("Downloading Media")
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filePath)

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }

}