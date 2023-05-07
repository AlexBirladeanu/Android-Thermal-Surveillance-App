package com.example.imageeditor.fragments

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.imageeditor.R
import com.example.imageeditor.classification.ImageClassifier
import com.example.imageeditor.databinding.FragmentCameraBinding
import com.example.imageeditor.ui.theme.ThiefBusterTheme
import com.example.imageeditor.ui.views.CameraView
import com.example.imageeditor.utils.AppSettingsProvider
import com.example.imageeditor.utils.NativeMethodsProvider
import com.example.imageeditor.viewModels.CameraViewModel
import com.thermal.seekware.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class CameraFragment : Fragment(),
    SeekImageReader.OnImageAvailableListener {

    private lateinit var seekCamera: SeekCamera
    private lateinit var seekImageReader: SeekImageReader
    private lateinit var seekImage: SeekImage
    private lateinit var seekPreview: SeekPreview
    private lateinit var seekImageView: ImageView

    private lateinit var dstBitmap: Bitmap

    private lateinit var mediaPlayer: MediaPlayer

    private var enableBackgroundSegmentationReset = false
    private var frameIndex = 0
    private var colorPaletteIndex = 0
    private var lastVibrationTimestamp = 0L

    private lateinit var binding: FragmentCameraBinding

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
    ): View {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val isDetectionOn by viewModel.inDetectionMode.collectAsState()
                val isAutoStartOn by viewModel.isAutoStartOn.collectAsState()
                ThiefBusterTheme {
                    CameraView(
                        isAutoStartOn = isAutoStartOn,
                        isDetectionOn = isDetectionOn,
                        onClick = {
                            if (isDetectionOn) {
                                stopDetectionMode()
                            } else {
                                startDetectionMode()
                            }
                        }
                    )
                }
            }
        }
        return binding.root
    }

    override fun onImageAvailable(p0: SeekImage?) {
        lifecycleScope.launch {
            seekImage = p0!!
            if (viewModel.inDetectionMode.value) {
                dstBitmap = seekImage.colorBitmap
                seekImageView.setImageBitmap(dstBitmap)
                enableBackgroundSegmentationReset = false
                classifyFrame()
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
        val toolbar: androidx.appcompat.widget.Toolbar = binding.toolbar
        toolbar.title = "Camera"
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        mediaPlayer = MediaPlayer.create(requireActivity(), R.raw.notification_alert)

        setupCamera()
    }

    private fun setupCamera() {
        seekPreview = binding.seekPreview
        seekImageView = binding.seekImageView
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
        seekPreview.visibility = View.GONE
        seekImageView.visibility = View.VISIBLE
        viewModel.startRecording()
    }

    private fun stopDetectionMode() {
        seekPreview.visibility = View.VISIBLE
        seekImageView.visibility = View.GONE
        viewModel.stopRecording()
    }

    private fun classifyFrame() {
        frameIndex++
        if (AppSettingsProvider.getDetectPeople()) {
            detectPeople()
        } else {
            detectMotionOnly()
        }
    }

    private fun detectPeople() {
        val movementBitmap = dstBitmap.copy(dstBitmap.config, true)
        val isMovement = NativeMethodsProvider.backgroundSegmentation(
            movementBitmap,
            2,
            enableBackgroundSegmentationReset,
            movementBitmap
        )
        if (isMovement) {
            NativeMethodsProvider.enhanceContrast(movementBitmap, movementBitmap)
            val classifier = ImageClassifier(
                i = 0,
                bitmap = movementBitmap,
                fragmentActivity = requireActivity(),
                frameIndex = frameIndex,
                drawBitmap = { bitmap, frameIndex, message, log ->
                    drawRectangle(bitmap, frameIndex, message, log)
                }
            )
            classifier.classify()

        } else {
            var newCluster: Bitmap = dstBitmap.copy(dstBitmap.config, true)
            val clustersNr = NativeMethodsProvider.getClusters(
                newCluster,
                newCluster,
                true,
                AppSettingsProvider.isBodyMergeEnabled()
            )
            val clusterList: MutableList<Bitmap> = mutableListOf()
            for (i in 0 until clustersNr) {
                newCluster = dstBitmap.copy(dstBitmap.config, true)
                NativeMethodsProvider.getClusters(newCluster, newCluster, false, false)
                clusterList.add(newCluster)
            }

//        clusterList.forEach {
//            drawPerson(it, frameIndex, "")
//        }

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
                            drawBitmap = { bitmap, frameIndex, message, log ->
                                drawRectangle(bitmap, frameIndex, message, log)
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
    }

    private fun detectMotionOnly() {
        val movementBitmap = dstBitmap.copy(dstBitmap.config, true)
        val isMovement = NativeMethodsProvider.backgroundSegmentation(
            movementBitmap,
            1,
            enableBackgroundSegmentationReset,
            movementBitmap
        )
        if (isMovement) {
            drawRectangle(movementBitmap, frameIndex, "Movement", "")
        }

    }

    private var lastFrameIndex = -1
    private var peopleDetected = 0
    private lateinit var bitmapToSave: Bitmap

    private fun drawRectangle(
        personCluster: Bitmap,
        frameIndex: Int,
        message: String,
        log: String
    ) {

        NativeMethodsProvider.drawRectangle(
            dstBitmap,
            personCluster,
            message,
            dstBitmap
        )
        if (lastFrameIndex == -1) {
            lastFrameIndex = frameIndex
        }
        if (lastFrameIndex != frameIndex) {
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
                @Suppress("DEPRECATION")
                requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect =
                    VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
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