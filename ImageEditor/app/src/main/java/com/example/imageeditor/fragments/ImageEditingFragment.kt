package com.example.imageeditor.fragments

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.example.imageeditor.NativeMethodsProvider
import com.example.imageeditor.R

class ImageEditingFragment(seekImageBitmap: Bitmap) : Fragment() {

    private lateinit var imageView: ImageView
    private var srcBitmap: Bitmap

    init {
        srcBitmap = seekImageBitmap
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image_editing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageView = requireView().findViewById(R.id.imageView)
        imageView.setImageBitmap(srcBitmap)

        val cancelButton: Button = requireView().findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener{
            val mainFragment: MainFragment = MainFragment.newInstance()

            val fragmentManager = activity?.supportFragmentManager
            val transaction = fragmentManager?.beginTransaction()
            transaction?.replace(R.id.fragment_container, mainFragment)
            transaction?.commit()
        }

        val processImageButton: Button = requireView().findViewById(R.id.processImageButton)
        processImageButton.setOnClickListener{
            //NativeMethodsProvider.cannyEdgeDetection(srcBitmap, srcBitmap)
        }
    }

    companion object {
        fun newInstance(bitmap: Bitmap): ImageEditingFragment {
            return ImageEditingFragment(bitmap)
        }
    }
}