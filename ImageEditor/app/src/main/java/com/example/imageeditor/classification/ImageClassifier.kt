package com.example.imageeditor.classification

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import org.tensorflow.lite.task.vision.classifier.Classifications

class ImageClassifier(
    i: Int,
    val bitmap: Bitmap,
    fragmentActivity: FragmentActivity,
    val frameIndex: Int,
    val drawBitmap: (Bitmap, Int, String, String) -> Unit
) : ImageClassifierHelper.ClassifierListener {

    private val index: Int
    private val activity: FragmentActivity

    private var imageClassifierHelper: ImageClassifierHelper? = null

    init {
        index = i
        activity = fragmentActivity
        imageClassifierHelper =
            ImageClassifierHelper(context = activity, imageClassifierListener = this)
    }

    fun classify() {
        imageClassifierHelper?.classify(bitmap, -90)

    }

    override fun onError(error: String) {
        activity.runOnUiThread {
            Toast.makeText(activity, error, Toast.LENGTH_SHORT).show()
            Log.w("Clustere", "index $index error")
        }
    }

    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
        activity.runOnUiThread {
            var log = ""
            var message = ""
            var personFound = false
            results?.forEach { classifications ->
                classifications.categories.forEach {
                    if (it.label == "body" && it.score > 0.54) {
                        personFound = true
                        message = "Body"
                    }
                    if (it.label == "face" && it.score > 0.80) {
                        personFound = true
                        message = "Face"
                    }


                    log += it.label.toString() + " " + it.score.toString()

                }
            }
            Log.w(
                "Clustere",
                "personFound=$personFound at frame " + frameIndex + " at cluster " + index
            )
            if (personFound) {
                drawBitmap(bitmap, frameIndex, message, log)
            }
        }
    }
}