package com.goelapplications.igsaver

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.goelapplications.igsaver.adapters.SideCarAdapter
import com.goelapplications.igsaver.constants.MediaType
import com.goelapplications.igsaver.databinding.ActivityMainBinding
import com.goelapplications.igsaver.fragments.ImageFragment
import com.goelapplications.igsaver.fragments.VideoFragment
import com.goelapplications.igsaver.listeners.MediaTypeListener
import com.goelapplications.igsaver.listeners.RootListener
import com.goelapplications.igsaver.listeners.SideCarListener
import com.goelapplications.igsaver.listeners.UrlListener
import com.goelapplications.igsaver.models.MediaModel
import com.goelapplications.igsaver.utils.DownloadUtil
import com.goelapplications.igsaver.utils.UrlParser
import org.json.JSONObject

class MainActivity : AppCompatActivity(), SideCarAdapter.ClickListeners {

    private lateinit var binding: ActivityMainBinding
    private lateinit var parser: UrlParser
    private lateinit var rootObject: JSONObject
    private lateinit var model: MediaModel
    private lateinit var downloadUtil: DownloadUtil
    private lateinit var modelList: ArrayList<MediaModel>
    private lateinit var adapter: SideCarAdapter
    private lateinit var downloadUrl: String
    private lateinit var mediaType: MediaType
    private var editable: Editable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideContent()
        editable = binding.editText.text
        binding.goButton.setOnClickListener {
            if (editable != null && editable.toString().isNotEmpty()) {
                hideContent()
                loadJsonObject()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.progressBar.visibility = View.GONE
    }

    private fun hideContent() {
        binding.imageView.visibility = View.GONE
        binding.videoView.visibility = View.GONE
        binding.sidecar.root.visibility = View.GONE
        binding.downloadButton.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun showContent(type: MediaType) {
        binding.progressBar.visibility = View.GONE
        when (type) {
            MediaType.TYPE_IMAGE -> {
                binding.imageView.visibility = View.VISIBLE
                binding.downloadButton.visibility = View.VISIBLE
            }
            MediaType.TYPE_VIDEO -> {
                binding.videoView.visibility = View.VISIBLE
                binding.downloadButton.visibility = View.VISIBLE
            }
            else -> {
                binding.sidecar.root.visibility = View.VISIBLE
            }
        }
    }

    private fun loadJsonObject() {
        val rawUrl = editable.toString()
        if (editable != null && rawUrl.isNotEmpty()) {
            parser = UrlParser(this, rawUrl)
            parser.getRoot(object : RootListener {
                override fun jsonRetrieved(jsonObject: JSONObject) {
                    rootObject = jsonObject
                    findMediaType()
                }

                override fun onError() {
                    showToast(R.string.url_error)
                    binding.editText.error = getString(R.string.url_error)
                    hideContent()
                    binding.progressBar.visibility = View.GONE
                }
            })
        }
    }

    private fun findMediaType() {
        parser.findMediaType(rootObject, object : MediaTypeListener {
            override fun mediaTypeFound(type: MediaType) {
                model = MediaModel(mediaType = type)
                if (type == MediaType.TYPE_IMAGE || type == MediaType.TYPE_VIDEO)
                    findMediaUrl()
                else
                    createMediaFragment()
            }

            override fun onError() {
                showToast(R.string.media_type_error)
                hideContent()
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    private fun findMediaUrl() {
        parser.getDownloadUrl(rootObject, model.mediaType, object : UrlListener {
            override fun urlRetrieved(url: String) {
                model.downloadUrl = url
                loadMedia()
            }

            override fun onError() {
                showToast(R.string.media_error)
                hideContent()
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    private fun createMediaFragment() {
        modelList = ArrayList()
        adapter = SideCarAdapter(this, modelList, this)
        binding.sidecar.recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.sidecar.recyclerView.layoutManager = layoutManager
        parser.getMediaList(rootObject, object : SideCarListener {
            override fun onCountComplete(count: Int) {
                binding.sidecar.mediaCounter.text = resources
                    .getQuantityString(R.plurals.files_counter, count, count)
            }

            override fun onMediaRetrieved(model: MediaModel) {
                modelList.add(model)
                adapter.notifyDataSetChanged()
            }

            override fun onError() {
                showToast(R.string.media_error)
            }
        })
        showContent(MediaType.TYPE_SIDECAR)
        binding.sidecar.downloadAllButton.setOnClickListener {
            modelList.forEach { mediaModel ->
                downloadUrl = mediaModel.downloadUrl!!
                mediaType = mediaModel.mediaType
                initializeDownload()
            }
        }
    }

    private fun loadMedia() {
        if (model.mediaType == MediaType.TYPE_IMAGE)
            Glide.with(this).load(model.downloadUrl).into(binding.imageView)
        else if (model.mediaType == MediaType.TYPE_VIDEO) {
            val view = binding.videoView
            view.setVideoURI(Uri.parse(model.downloadUrl))
            view.start()
            view.setMediaController(MediaController(this, false))
        }
        showContent(model.mediaType)
        binding.downloadButton.setOnClickListener {
            downloadUrl = model.downloadUrl!!
            mediaType = model.mediaType
            initializeDownload()
        }
    }

    private fun initializeDownload() {
        downloadUtil = DownloadUtil(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED
            ) {
                val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permissions, 100)
            } else {
                startDownload()
            }
        } else {
            startDownload()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startDownload()
        } else {
            showToast(R.string.storage_permission_error)
        }
    }

    private fun startDownload() {
        downloadUtil.startDownload(downloadUrl, mediaType)
        showToast(R.string.download_message)
    }

    private fun showToast(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }

    override fun preview(model: MediaModel) {
        model.downloadUrl?.let {
            val fragment = if (model.mediaType == MediaType.TYPE_IMAGE) {
                ImageFragment.newInstance(it)
            } else {
                VideoFragment.newInstance(it)
            }
            binding.frameContainer.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_container, fragment)
                .addToBackStack("Image")
                .commit()
        }
    }

    override fun download(model: MediaModel) {
        downloadUrl = model.downloadUrl!!
        mediaType = model.mediaType
        initializeDownload()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        binding.frameContainer.visibility = View.GONE
    }
}