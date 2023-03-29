package com.example.imageeditor

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.tensorflow.lite.task.vision.classifier.Classifications

class ClusterImageClassifier(
    i: Int,
    bitmap: Bitmap,
    fragmentActivity: FragmentActivity
) : ImageClassifierHelper.ClassifierListener {

    private val index: Int
    private val activity: FragmentActivity
    val resultSharedFlow: MutableSharedFlow<Pair<Int, Boolean>> = MutableSharedFlow(replay = 0)

    init {
        index = i
        activity = fragmentActivity
        val imageClassifierHelper =
            ImageClassifierHelper(context = activity, imageClassifierListener = this)
        imageClassifierHelper.classify(bitmap, -90)
    }

    override fun onError(error: String) {
        activity.runOnUiThread {
            Toast.makeText(activity, error, Toast.LENGTH_SHORT).show()
            Log.w("Clustere", "index $index error")
        }
    }

    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
        GlobalScope.launch(Dispatchers.IO) {
            results?.forEach { classifications ->
                resultSharedFlow.emit(Pair(index, !classifications.categories.isNullOrEmpty()))
            }
        }
    }
}