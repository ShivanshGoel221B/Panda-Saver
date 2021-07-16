package com.goelapplications.igsaver

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import com.goelapplications.igsaver.listeners.*
import com.goelapplications.igsaver.models.MediaModel
import com.goelapplications.igsaver.utils.DownloadUtil
import com.goelapplications.igsaver.utils.UrlParser
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var parser: UrlParser
    private lateinit var rootObject: JSONObject
    private lateinit var model: MediaModel
    private var editable: Editable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideContent()
        editable = binding.editText.text
        binding.pasteButton.setOnClickListener { pasteLink() }
        binding.goButton.setOnClickListener {
            if (editable != null && editable.toString().isNotEmpty()) {
                hideContent()
                loadJsonObject()
            }
        }
        MobileAds.initialize(this)
        initializeAds()
    }

    private fun initializeAds() {
        val adRequestTop = AdRequest.Builder().build()
        val adRequestBottom = AdRequest.Builder().build()
        binding.adViewTop.loadAd(adRequestTop)
        binding.adViewBottom.loadAd(adRequestBottom)
        val adListener = object: AdListener() {
            override fun onAdFailedToLoad(adError : LoadAdError) { super.onAdFailedToLoad(adError) }
        }
        binding.adViewTop.adListener = adListener
        binding.adViewBottom.adListener = adListener
    }

    private fun pasteLink() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val item = clipboard.primaryClip?.getItemAt(0)
        val pasteUri = item?.text
        if (pasteUri != null) {
            if (pasteUri.startsWith("http") || pasteUri.startsWith("www.instagram.com") || pasteUri.startsWith(
                    "instagram.com"
                )
            ) {
                binding.editText.text.clear()
                binding.editText.text.insert(0, pasteUri)
            } else {
                showToast(R.string.empty_clipboard)
            }
        } else {
            showToast(R.string.empty_clipboard)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.progressBar.visibility = View.GONE
    }

    private fun hideContent() {
        binding.progressBar.visibility = View.VISIBLE
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
                    findMediaCaption()
                else {
                    parser.getCaption(rootObject, object : CaptionListener {
                        override fun onCaptionRetrieved(caption: String) {
                            model.caption = caption
                            ResultActivity.model = model
                            ResultActivity.parser = parser
                            ResultActivity.rootObject = rootObject
                            startActivity(Intent(this@MainActivity, ResultActivity::class.java))
                        }

                        override fun onError() {
                            ResultActivity.model = model
                            ResultActivity.parser = parser
                            ResultActivity.rootObject = rootObject
                            startActivity(Intent(this@MainActivity, ResultActivity::class.java))
                        }
                    })
                }
            }

            override fun onError() {
                showToast(R.string.media_type_error)
                hideContent()
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    private fun findMediaCaption() {
        parser.getCaption(rootObject, object : CaptionListener {
            override fun onCaptionRetrieved(caption: String) {
                model.caption = caption
                findMediaUrl()
            }

            override fun onError() {
                findMediaUrl()
            }
        })
    }

    private fun findMediaUrl() {
        parser.getDownloadUrl(rootObject, model.mediaType, object : UrlListener {
            override fun urlRetrieved(url: String) {
                model.downloadUrl = url
                ResultActivity.model = model
                ResultActivity.parser = parser
                ResultActivity.rootObject = rootObject
                startActivity(Intent(this@MainActivity, ResultActivity::class.java))
            }

            override fun onError() {
                showToast(R.string.media_error)
                hideContent()
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    private fun showToast(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }
}