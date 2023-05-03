package com.example.imageeditor.utils

import android.graphics.Bitmap

object NativeMethodsProvider {

    external fun color2Grayscale(bitmapIn: Bitmap, bitmapOut: Bitmap)

    external fun enhanceContrast(bitmapIn: Bitmap, bitmapOut: Bitmap)

    external fun backgroundSegmentation(bitmapIn: Bitmap, method: Int, enableReset: Boolean, bitmapOut: Bitmap)

    external fun getClusters(bitmapIn: Bitmap, bitmapOut: Bitmap, getClusterSizeOnly: Boolean = false, enableClusterMerge: Boolean): Int

    external fun drawPerson(bitmapIn: Bitmap, cluster: Bitmap, bitmapOut: Bitmap)
}