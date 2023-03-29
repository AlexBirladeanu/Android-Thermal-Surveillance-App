package com.example.imageeditor.fragments

import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.imageeditor.ClusterImageClassifier
import com.example.imageeditor.ImageClassifierHelper
import com.example.imageeditor.NativeMethodsProvider
import com.example.imageeditor.R
import com.thermal.seekware.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.tensorflow.lite.task.vision.classifier.Classifications


class MainFragment : Fragment(),
    SeekImageReader.OnImageAvailableListener,
    ImageClassifierHelper.ClassifierListener {

    private lateinit var infoTextView: TextView
    private lateinit var seekCamera: SeekCamera
    private lateinit var seekImageReader: SeekImageReader
    private lateinit var seekImage: SeekImage
    private lateinit var seekPreview: SeekPreview
    private lateinit var seekImageView: ImageView
    private var colorPaletteIndex = 0
    private lateinit var dstBitmap: Bitmap
    private lateinit var imageClassifierHelper: ImageClassifierHelper

    private var inDetectionMode = false
    private lateinit var logTextView: TextView
    private var enableBackgroundSegmentationReset = false

    private var frameNr = 0


    private val stateCallback: SeekCamera.StateCallback =
        object : SeekCamera.StateCallbackAdapter() {
            override fun onOpened(p0: SeekCamera?) {
                seekCamera = p0!!
                seekCamera.colorPalette = colorPaletteList[0]
                seekCamera.createSeekCameraCaptureSession(seekImageReader, seekPreview)
//                seekCamera.createSeekCameraCaptureSession(false, false, true, seekPreview)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onImageAvailable(p0: SeekImage?) {
        lifecycleScope.launch {
            seekImage = p0!!
            seekImageView.setImageBitmap(seekImage.colorBitmap)

            if (inDetectionMode) {
                dstBitmap = seekImage.colorBitmap

                onDetectionModeStarted(dstBitmap)
            } else {
                enableBackgroundSegmentationReset = true
            }
            frameNr++
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        imageClassifierHelper =
            ImageClassifierHelper(context = requireContext(), imageClassifierListener = this)
        infoTextView = requireView().findViewById(R.id.infoTextView)
        seekImageReader = SeekImageReader()
        seekImageReader.setOnImageAvailableListener(this)
        seekPreview = requireView().findViewById(R.id.seek_preview)
        seekPreview.initialize(false)
        SeekCameraManager(activity, null, stateCallback)

        seekImageView = requireView().findViewById(R.id.seek_image_view)

        val takePhotoButton: Button = requireView().findViewById(R.id.takePhotoButton)
        takePhotoButton.setOnClickListener {
//            takePhoto(seekImage.colorBitmap)
            if (inDetectionMode) {
                inDetectionMode = false
                takePhotoButton.text = "Start"
                onSwitchClicked()
            } else {
                inDetectionMode = true
                takePhotoButton.text = "Stop"
                onSwitchClicked()
            }
        }

        logTextView = requireView().findViewById(R.id.log)

        seekPreview.setStateCallback(object : SeekPreview.StateCallbackAdapter() {
            override fun onFrameAvailable(p0: SeekPreview?, p1: SeekImage?) {
                lifecycleScope.launch {
//                    seekImage = p1!!
//                    seekImageView.setImageBitmap(seekImage.colorBitmap)
//
//                    if(isVideoEditingEnabled) {
//                        dstBitmap = seekImage.colorBitmap
//                        nativeMethodsProvider.cannyEdgeDetection(dstBitmap, dstBitmap)
//                    }
                }
            }
        })
        val menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.main_fragment_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.change_palette) {
                    changePalette()
                    return true
                }
                if (menuItem.itemId == R.id.show_details) {
                    showDetailsDialog()
                    return true
                }
                return false
            }
        }
        (requireActivity() as MenuHost).addMenuProvider(menuProvider, viewLifecycleOwner)
    }

    override fun onResume() {
        super.onResume()
        seekPreview.initialize(false)
    }

    private fun takePhoto(bitmap: Bitmap) {
        val imageEditingFragment: ImageEditingFragment = ImageEditingFragment.newInstance(bitmap)

        val fragmentManager = activity?.supportFragmentManager
        val transaction = fragmentManager?.beginTransaction()
        transaction?.replace(R.id.fragment_container, imageEditingFragment)
        transaction?.commit()
    }

    private fun changePalette() {
        if (colorPaletteIndex < colorPaletteList.size - 1) {
            colorPaletteIndex++
        } else {
            colorPaletteIndex = 0
        }
        seekCamera.colorPalette = colorPaletteList[colorPaletteIndex]
    }

    private fun showDetailsDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(
            "$seekCamera\nMin: " + seekImage.thermography.minSpot
                .temperature.toString() +
                    "\nSpot: " + seekImage.thermography.centerSpot.temperature.toString() +
                    "\nMax: " + seekImage.thermography.maxSpot.temperature
                .toString()
        )
        builder.setTitle("Seek Camera Details")
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun onSwitchClicked() {
        if (inDetectionMode) {
            seekPreview.visibility = View.GONE
            seekImageView.visibility = View.VISIBLE
        } else {
            seekPreview.visibility = View.VISIBLE
            seekImageView.visibility = View.GONE
        }
    }

    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
        var detectedObjects = ""
        activity?.runOnUiThread {
            results?.forEach { classifications ->
                classifications.categories.forEach {
                    detectedObjects += it.label.toString() + " " + it.score.toString()
                }
            }
            logTextView.text = detectedObjects
        }
    }

    private fun onDetectionModeStarted(dstBitmap: Bitmap) {
        //NativeMethodsProvider.backgroundSegmentation(dstBitmap, 3, enableBackgroundSegmentationReset, dstBitmap)

//        var newCluster: Bitmap = dstBitmap.copy(dstBitmap.config, true)
//        val clustersNr = NativeMethodsProvider.getClusters(newCluster, newCluster, true)
//        val clusterList: MutableList<Bitmap> = mutableListOf()
//        for (i in 0 until clustersNr) {
//            newCluster = dstBitmap.copy(dstBitmap.config, true)
//            NativeMethodsProvider.getClusters(newCluster, newCluster, false)
//            clusterList.add(newCluster)
//        }
//        Log.w("Clustere", "listSize=${clusterList.size}")
//
//        val sharedFlows = clusterList.mapIndexed { index, bitmap ->
//            val classifier = ClusterImageClassifier(index, bitmap, requireActivity())
//            classifier.resultSharedFlow
//        }
//        val result = runBlocking(Dispatchers.IO) {
//            collectClassifierResults(sharedFlows)
//        }

        NativeMethodsProvider.dbScanCluster(dstBitmap, dstBitmap)


//        activity?.runOnUiThread {
//            seekImageView.setImageBitmap(dstBitmap)
//        }
        //imageClassifierHelper.classify(dstBitmap, -90)
    }

    private suspend fun collectClassifierResults(sharedFlows: List<MutableSharedFlow<Pair<Int, Boolean>>>) {
        sharedFlows.forEach{
            it.collectLatest { pair ->
                Log.w("Clustere", "classifier " + pair.first + " returned " + pair.second)
            }
        }
    }
    companion object {
        private const val SEEK_PREVIEW_HEIGHT = 2160
        private const val SEEK_PREVIEW_WIDTH = 1440
        private const val RESOLUTION_HEIGHT = 156
        private const val RESOLUTION_WIDTH = 206
        fun newInstance(): MainFragment {
            return MainFragment()
        }

        private val colorPaletteList: List<SeekCamera.ColorPalette> = listOf(
            SeekCamera.ColorPalette.TYRIAN,
            SeekCamera.ColorPalette.IRON2,
            SeekCamera.ColorPalette.RECON,
            SeekCamera.ColorPalette.BLACK_RECON,
            SeekCamera.ColorPalette.WHITEHOT,
            SeekCamera.ColorPalette.BLACKHOT,
            SeekCamera.ColorPalette.AMBER,
            SeekCamera.ColorPalette.GREEN,
            SeekCamera.ColorPalette.SPECTRA
        )
    }
}