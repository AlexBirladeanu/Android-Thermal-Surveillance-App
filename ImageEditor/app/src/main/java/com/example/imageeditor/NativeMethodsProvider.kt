package com.example.imageeditor

import android.graphics.Bitmap

object NativeMethodsProvider {

    external fun color2Grayscale(bitmapIn: Bitmap, bitmapOut: Bitmap)

    external fun enhanceContrast(bitmapIn: Bitmap, bitmapOut: Bitmap)

    external fun backgroundSegmentation(bitmapIn: Bitmap, method: Int, enableReset: Boolean, bitmapOut: Bitmap)

    external fun getClusters(bitmapIn: Bitmap, bitmapOut: Bitmap, getClusterSizeOnly: Boolean): Int

    external fun dbScanCluster(bitmapIn: Bitmap, bitmapOut: Bitmap)

}