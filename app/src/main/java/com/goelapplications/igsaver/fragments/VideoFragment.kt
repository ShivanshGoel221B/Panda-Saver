package com.goelapplications.igsaver.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import com.bumptech.glide.Glide
import com.goelapplications.igsaver.R

class VideoFragment : Fragment() {
    private lateinit var videoUrl: String
    private lateinit var videoHolder: VideoView
    private lateinit var closeButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_video, container, false)
        rootView.setOnClickListener { return@setOnClickListener }
        videoHolder = rootView.findViewById(R.id.video_view)
        closeButton = rootView.findViewById(R.id.closeButton)
        closeButton.setOnClickListener { requireActivity().onBackPressed() }
        videoHolder.setVideoURI(Uri.parse(videoUrl))
        videoHolder.setMediaController(MediaController(requireContext(), false))
        videoHolder.start()
        return rootView
    }

    companion object {
        @JvmStatic
        fun newInstance(videoUrl: String) =
            VideoFragment().apply { this.videoUrl = videoUrl }
    }
}