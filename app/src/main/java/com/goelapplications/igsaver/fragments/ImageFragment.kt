package com.goelapplications.igsaver.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.goelapplications.igsaver.R

class ImageFragment : Fragment() {
    private lateinit var imageUrl: String
    private lateinit var imageHolder: ImageView
    private lateinit var closeButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_image, container, false)
        rootView.setOnClickListener { return@setOnClickListener }
        imageHolder = rootView.findViewById(R.id.imageView)
        closeButton = rootView.findViewById(R.id.closeButton)
        closeButton.setOnClickListener { requireActivity().onBackPressed() }
        Glide.with(requireContext()).load(imageUrl).into(imageHolder)
        return rootView
    }

    companion object {
        @JvmStatic
        fun newInstance(imageUrl: String) =
            ImageFragment().apply { this.imageUrl = imageUrl }
    }
}