package com.example.imageeditor

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.thermal.seekware.*
import kotlinx.coroutines.launch
import org.tensorflow.lite.task.vision.detector.Detection
import java.util.*


class MainFragment : Fragment(),
    SeekImageReader.OnImageAvailableListener,
    ObjectDetectorHelper.DetectorListener {
//class MainFragment : Fragment() {

    private lateinit var infoTextView: TextView
    private lateinit var seekCamera: SeekCamera
    private lateinit var seekImageReader: SeekImageReader
    private lateinit var seekImage: SeekImage
    private lateinit var seekPreview: SeekPreview
    private lateinit var seekImageView: ImageView
    private var colorPaletteIndex = 0
    private val nativeMethodsProvider = NativeMethodsProvider()
    private var isVideoEditingEnabled = false
    private lateinit var dstBitmap: Bitmap
    private lateinit var objectDetectorHelper: ObjectDetectorHelper


    private val stateCallback: SeekCamera.StateCallback =
        object : SeekCamera.StateCallbackAdapter() {
            override fun onOpened(p0: SeekCamera?) {
                seekCamera = p0!!
                seekCamera.colorPalette = SeekCamera.ColorPalette.SPECTRA
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

            if(isVideoEditingEnabled) {
                dstBitmap = seekImage.colorBitmap
                nativeMethodsProvider.grayscaleSegmentation(dstBitmap, dstBitmap)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        infoTextView = requireView().findViewById(R.id.infoTextView)
        seekImageReader = SeekImageReader()
        seekImageReader.setOnImageAvailableListener(this)
        seekPreview = requireView().findViewById(R.id.seek_preview)
        seekPreview.initialize(false)
        SeekCameraManager(activity, null, stateCallback)

        seekImageView = requireView().findViewById(R.id.seek_image_view)
       objectDetectorHelper = ObjectDetectorHelper(context = requireContext(), objectDetectorListener = this)

        val takePhotoButton: Button = requireView().findViewById(R.id.takePhotoButton)
        takePhotoButton.setOnClickListener {
            takePhoto(seekImage.colorBitmap)
        }

        val changePaletteButton: Button = requireView().findViewById(R.id.changePaletteButton)
        changePaletteButton.setOnClickListener {
            changePalette()
        }

        val showDetailsButton: Button = requireView().findViewById(R.id.showDetailsButton)
        showDetailsButton.setOnClickListener {
            showDetailsDialog()
        }

        val liveEditingSwitch: SwitchCompat = requireView().findViewById(R.id.videoEditingSwitch)
        liveEditingSwitch.setOnClickListener{
            onSwitchClicked(liveEditingSwitch)
        }

        seekPreview.setStateCallback(object: SeekPreview.StateCallbackAdapter() {
            override fun onFrameAvailable(p0: SeekPreview?, p1: SeekImage?) {
                lifecycleScope.launch{
//                    seekImage = p1!!
//                    seekImageView.setImageBitmap(seekImage.colorBitmap)
//
//                    if(isVideoEditingEnabled) {
//                        dstBitmap = seekImage.colorBitmap
//                        nativeMethodsProvider.cannyEdgeDetection(dstBitmap, dstBitmap)
//                    }
                }
            }

            override fun onClick(p0: SeekPreview?, p1: MotionEvent?) {
                super.onClick(p0, p1)
                val motionEvent = p1!!
                val seedPointX = (motionEvent.x * RESOLUTION_WIDTH / SEEK_PREVIEW_WIDTH).toInt()
                val seedPointY = (motionEvent.y * RESOLUTION_HEIGHT / SEEK_PREVIEW_HEIGHT).toInt()
                dstBitmap = seekImage.colorBitmap
                nativeMethodsProvider.regionGrowingSegmentation(seedPointX, seedPointY, dstBitmap, dstBitmap)
                takePhoto(dstBitmap)
            }
        })
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

    private fun onSwitchClicked(switch: SwitchCompat) {
        val spacer: Space = requireView().findViewById(R.id.spacer)
        if (switch.isChecked) {
            isVideoEditingEnabled = true
            seekPreview.visibility = View.GONE
            seekImageView.visibility = View.VISIBLE
            spacer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToBottom = seekImageView.id
            }
        } else {
            isVideoEditingEnabled = false
            seekPreview.visibility = View.VISIBLE
            seekImageView.visibility = View.GONE
            spacer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToBottom = seekPreview.id
            }
        }
    }

    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResults(
        results: MutableList<Detection>?,
        inferenceTime: Long,
        imageHeight: Int,
        imageWidth: Int
    ) {
        activity?.runOnUiThread {
            for(detection in results!!) {
                Log.w("Object Detected!", detection.categories[0].label + " " +
                        String.format("%.2f", detection.categories[0].score))
            }

            // call a native method which draws the bounding boxes on detected objects

//            // Pass necessary information to OverlayView for drawing on the canvas
//            fragmentCameraBinding.overlay.setResults(
//                results ?: LinkedList<Detection>(),
//                imageHeight,
//                imageWidth
//            )
//
//            // Force a redraw
//            fragmentCameraBinding.overlay.invalidate()
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