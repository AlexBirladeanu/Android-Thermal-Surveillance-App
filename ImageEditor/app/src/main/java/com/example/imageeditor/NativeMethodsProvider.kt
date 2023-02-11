package com.example.imageeditor

import android.graphics.Bitmap

class NativeMethodsProvider {

    external fun flip(bitmapIn: Bitmap, bitmapOut: Bitmap)

    external fun cannyEdgeDetection(bitmapIn: Bitmap, bitmapOut: Bitmap)

    external fun color2BW(bitmapIn: Bitmap, bitmapOut: Bitmap)

    external fun grayscaleSegmentation(bitmapIn: Bitmap, bitmapOut: Bitmap)

    external fun regionGrowingSegmentation(seedPointX: Int, seedPointY: Int, bitmapIn: Bitmap, bitmapOut: Bitmap)

}