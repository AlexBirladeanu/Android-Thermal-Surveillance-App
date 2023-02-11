#include <jni.h>
#include <string>
#include "opencv2/core.hpp"
#include "opencv2/imgproc.hpp"
#include "opencv-utils.h"
#include "android/bitmap.h"

void bitmapToMat(JNIEnv *env, jobject bitmap, Mat& dst, jboolean needUnPremultiplyAlpha)
{
    AndroidBitmapInfo  info;
    void*              pixels = 0;

    try {
        CV_Assert( AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
        CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                   info.format == ANDROID_BITMAP_FORMAT_RGB_565 );
        CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
        CV_Assert( pixels );
        dst.create(info.height, info.width, CV_8UC4);
        if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
        {
            Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(needUnPremultiplyAlpha) cvtColor(tmp, dst, COLOR_mRGBA2RGBA);
            else tmp.copyTo(dst);
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            Mat tmp(info.height, info.width, CV_8UC2, pixels);
            cvtColor(tmp, dst, COLOR_BGR5652RGBA);
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch(const cv::Exception& e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nBitmapToMat}");
        return;
    }
}

void matToBitmap(JNIEnv* env, Mat src, jobject bitmap, jboolean needPremultiplyAlpha)
{
    AndroidBitmapInfo  info;
    void*              pixels = 0;

    try {
        CV_Assert( AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
        CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                   info.format == ANDROID_BITMAP_FORMAT_RGB_565 );
        CV_Assert( src.dims == 2 && info.height == (uint32_t)src.rows && info.width == (uint32_t)src.cols );
        CV_Assert( src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4 );
        CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
        CV_Assert( pixels );
        if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
        {
            Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(src.type() == CV_8UC1)
            {
                cvtColor(src, tmp, COLOR_GRAY2RGBA);
            } else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, COLOR_RGB2RGBA);
            } else if(src.type() == CV_8UC4){
                if(needPremultiplyAlpha) cvtColor(src, tmp, COLOR_RGBA2mRGBA);
                else src.copyTo(tmp);
            }
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            Mat tmp(info.height, info.width, CV_8UC2, pixels);
            if(src.type() == CV_8UC1)
            {
                cvtColor(src, tmp, COLOR_GRAY2BGR565);
            } else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, COLOR_RGB2BGR565);
            } else if(src.type() == CV_8UC4){
                cvtColor(src, tmp, COLOR_RGBA2BGR565);
            }
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch(const cv::Exception& e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
        return;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_imageeditor_NativeMethodsProvider_flip(
        JNIEnv* env,
        jobject p_this,
        jobject bitmapIn,
        jobject bitmapOut) {
    Mat src;
    bitmapToMat(env, bitmapIn, src, false);
    myFlip(src);
    matToBitmap(env, src, bitmapOut, false);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_imageeditor_NativeMethodsProvider_cannyEdgeDetection(
        JNIEnv* env,
        jobject p_this,
        jobject bitmapIn,
        jobject bitmapOut) {
    Mat src;
    bitmapToMat(env, bitmapIn, src, false);
    Mat dst;
    dst = cannyEdgeDetection(src);
    matToBitmap(env, dst, bitmapOut, false);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_imageeditor_NativeMethodsProvider_color2BW(
        JNIEnv* env,
        jobject p_this,
        jobject bitmapIn,
        jobject bitmapOut) {
    Mat src;
    bitmapToMat(env, bitmapIn, src, false);
    Mat dst;
    dst = color2BW(src);
    matToBitmap(env, dst, bitmapOut, false);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_imageeditor_NativeMethodsProvider_grayscaleSegmentation(
        JNIEnv* env,
        jobject p_this,
        jobject bitmapIn,
        jobject bitmapOut) {
    Mat src;
    bitmapToMat(env, bitmapIn, src, false);
    Mat dst;
    dst = grayscale_segmentation(src);
    matToBitmap(env, dst, bitmapOut, false);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_imageeditor_NativeMethodsProvider_regionGrowingSegmentation(
        JNIEnv* env,
        jobject p_this,
        int seedPointX,
        int seedPointY,
        jobject bitmapIn,
        jobject bitmapOut) {
    Mat src;
    bitmapToMat(env, bitmapIn, src, false);
    Mat dst;
    dst = region_growing_segmentation(seedPointX, seedPointY, src);
    matToBitmap(env, dst, bitmapOut, false);
}

