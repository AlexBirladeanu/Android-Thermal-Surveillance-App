package com.example.imageeditor.fragments

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.*
import android.provider.MediaStore.Audio.Media
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.imageeditor.R
import com.example.imageeditor.classification.ImageClassifier
import com.example.imageeditor.classification.ImageClassifierHelper
import com.example.imageeditor.utils.AppSettingsProvider
import com.example.imageeditor.utils.NativeMethodsProvider
import com.example.imageeditor.viewModels.CameraViewModel
import com.thermal.seekware.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.tensorflow.lite.task.vision.classifier.Classifications


class CameraFragment : Fragment(),
    SeekImageReader.OnImageAvailableListener,
    ImageClassifierHelper.ClassifierListener {

    private lateinit var seekCamera: SeekCamera
    private lateinit var seekImageReader: SeekImageReader
    private lateinit var seekImage: SeekImage
    private lateinit var seekPreview: SeekPreview
    private lateinit var seekImageView: ImageView

    private lateinit var dstBitmap: Bitmap
    private lateinit var imageClassifierHelper: ImageClassifierHelper

    private lateinit var logTextView: TextView
    private lateinit var startButton: Button
    private lateinit var chronometer: Chronometer
    private lateinit var mediaPlayer: MediaPlayer

    private var inDetectionMode = false
    private var enableBackgroundSegmentationReset = false
    private var frameIndex = 0
    private var colorPaletteIndex = 0
    private var lastVibrationTimestamp = 0L

    //    private val viewModel = ViewModelProvider(this)[CameraViewModel::class.java]
    private val viewModel = CameraViewModel()

    private val stateCallback: SeekCamera.StateCallback =
        object : SeekCamera.StateCallbackAdapter() {
            override fun onOpened(p0: SeekCamera?) {
                seekCamera = p0!!
                onCameraConnected()
            }

            override fun onClosed(p0: SeekCamera?) {
                onCameraDisconnected()
            }
        }

    private fun onCameraConnected() {
        seekCamera.colorPalette = colorPaletteList[0]
        seekCamera.createSeekCameraCaptureSession(seekImageReader, seekPreview)
        seekPreview.visibility = View.VISIBLE
    }

    private fun onCameraDisconnected() {
        stopDetectionMode()
        seekPreview.visibility = View.INVISIBLE
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
            if (inDetectionMode) {
                dstBitmap = seekImage.colorBitmap

                classifyFrame()

                seekImageView.setImageBitmap(dstBitmap)
            } else {
                seekImageView.setImageBitmap(seekImage.colorBitmap)
                enableBackgroundSegmentationReset = true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        setupCamera()

        imageClassifierHelper =
            ImageClassifierHelper(context = requireContext(), imageClassifierListener = this)

        val toolbar: androidx.appcompat.widget.Toolbar = requireView().findViewById(R.id.toolbar)
        toolbar.title = "Camera"
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        logTextView = requireView().findViewById(R.id.log)
        chronometer = requireView().findViewById(R.id.chronometer)
        chronometer.visibility = View.GONE
        mediaPlayer = MediaPlayer.create(requireActivity(), R.raw.notification_alert)
        startButton = requireView().findViewById(R.id.startButton)
        startButton.setOnClickListener {
            if (inDetectionMode) {
                stopDetectionMode()
            } else {
                startDetectionMode()
            }
        }
    }

    private fun setupCamera() {
        seekPreview = requireView().findViewById(R.id.seek_preview)
        seekImageView = requireView().findViewById(R.id.seek_image_view)
        seekImageReader = SeekImageReader()
        seekImageReader.setOnImageAvailableListener(this)
        seekPreview.initialize(false)
        SeekCameraManager(activity, null, stateCallback)
    }

    override fun onResume() {
        super.onResume()
        seekPreview.initialize(false)
    }

    private fun startDetectionMode() {
        inDetectionMode = true
        startButton.text = "Stop"
        seekPreview.visibility = View.GONE
        seekImageView.visibility = View.VISIBLE
        chronometer.visibility = View.VISIBLE
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
        viewModel.startRecording()
    }

    private fun stopDetectionMode() {
        inDetectionMode = false
        startButton.text = "Start"
        seekPreview.visibility = View.VISIBLE
        seekImageView.visibility = View.GONE
        chronometer.stop()
        chronometer.visibility = View.GONE
        viewModel.stopRecording()
    }

    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
        activity?.runOnUiThread {
            val detected: MutableList<String> = mutableListOf()
            results?.forEach { classifications ->
                classifications.categories.forEach {
                    //if (it.label == "body" && it.score > 0.55) {
                    //detected.add(it.label.toString() + " " + it.score.toString())
                    //}
                    //if (it.label == "face" && it.score > 0.83) {
                    detected.add(it.label.toString() + " " + it.score.toString())
                    //}
                }
            }
            logTextView.text = detected.toString()
        }
    }

    private fun classifyFrame() {
        //NativeMethodsProvider.backgroundSegmentation(dstBitmap, 3, enableBackgroundSegmentationReset, dstBitmap)
        //NativeMethodsProvider.color2Grayscale(dstBitmap, dstBitmap)
        //NativeMethodsProvider.enhanceContrast(dstBitmap, dstBitmap)
        //imageClassifierHelper.classify(dstBitmap, -90)


        var newCluster: Bitmap = dstBitmap.copy(dstBitmap.config, true)
        val clustersNr = NativeMethodsProvider.getClusters(newCluster, newCluster, true, AppSettingsProvider.isBodyMergeEnabled())
        val clusterList: MutableList<Bitmap> = mutableListOf()
        for (i in 0 until clustersNr) {
            newCluster = dstBitmap.copy(dstBitmap.config, true)
            NativeMethodsProvider.getClusters(newCluster, newCluster, false, false)
            clusterList.add(newCluster)
        }
        frameIndex++

        Log.w("Clustere", "frame $frameIndex has listSize=${clusterList.size}")

//        clusterList.forEach {
//            drawPerson(it, frameIndex, "")
//        }


        //
//        if (clusterList.isNotEmpty()) {
//            dstBitmap = clusterList.first()
//            NativeMethodsProvider.enhanceContrast(dstBitmap, dstBitmap)
//            clusterList.forEach {
//                imageClassifierHelper.classify(it, -90)
//            }
//        } else {
//            logTextView.text = "false"
//        }


        //Classify clusters
        runBlocking {
            val jobs = mutableListOf<Job>()
            clusterList.forEachIndexed { index, bitmap ->
                val job = launch {
                    NativeMethodsProvider.enhanceContrast(bitmap, bitmap)
                    val classifier = ImageClassifier(
                        i = index,
                        bitmap = bitmap,
                        fragmentActivity = requireActivity(),
                        frameIndex = frameIndex,
                        drawBitmap = { bitmap, frameIndex, isFace, msg ->
                            drawPerson(bitmap, frameIndex, isFace, msg)
                        }
                    )
                    classifier.classify()
                }
                jobs.add(job)
            }
            jobs.forEach {
                it.join()
            }
        }
    }

    private var lastFrameIndex = -1
    private var peopleDetected = 0
    private lateinit var bitmapToSave: Bitmap

    private fun drawPerson(personCluster: Bitmap, frameIndex: Int, isFace: Boolean, message: String) {
        val newLog = logTextView.text.toString() + message
        logTextView.text = newLog

        NativeMethodsProvider.drawPerson(
            dstBitmap,
            personCluster,
            isFace,
            dstBitmap
        )
        if (lastFrameIndex == -1) {
            lastFrameIndex = frameIndex
        }
        if (lastFrameIndex != frameIndex) {
            logTextView.text = ""

            viewModel.insertPhoto(bitmapToSave, peopleDetected)
            peopleDetected = 0
            if (AppSettingsProvider.getVibrations()) {
                vibrate()
            }
            if (AppSettingsProvider.getSoundNotifications()) {
                startAlarmNotification()
            }
        }
        lastFrameIndex = frameIndex
        bitmapToSave = dstBitmap
        peopleDetected++
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

    private fun vibrate() {
        val timeBetweenVibrations = System.currentTimeMillis() - lastVibrationTimestamp
        if (timeBetweenVibrations > MIN_TIME_BETWEEN_VIBRATIONS) {
            val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    requireActivity().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect =
                    VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                vibrator.vibrate(1000)
            }

            lastVibrationTimestamp = System.currentTimeMillis()
        }
    }

    private fun startAlarmNotification() {
        mediaPlayer.start()
    }

    companion object {
        private const val SEEK_PREVIEW_HEIGHT = 2160
        private const val SEEK_PREVIEW_WIDTH = 1440
        private const val RESOLUTION_HEIGHT = 156
        private const val RESOLUTION_WIDTH = 206

        private const val MIN_TIME_BETWEEN_VIBRATIONS = 2000L

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