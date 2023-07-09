#include <jni.h>
#include <string>
#include <string.h>
#include "opencv2/core.hpp"
#include "opencv2/imgproc.hpp"
#include "opencv-utils.h"
#include "android/bitmap.h"
#include <android/log.h>

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
Java_com_example_imageeditor_utils_NativeMethodsProvider_color2Grayscale(
        JNIEnv* env,
        jobject p_this,
        jobject bitmapIn,
        jobject bitmapOut) {
    Mat src;
    bitmapToMat(env, bitmapIn, src, false);
    Mat dst;
    dst = color2Grayscale(src);
    matToBitmap(env, dst, bitmapOut, false);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_imageeditor_utils_NativeMethodsProvider_enhanceContrast(
        JNIEnv* env,
        jobject p_this,
        jobject bitmapIn,
        jobject bitmapOut) {
    Mat src;
    bitmapToMat(env, bitmapIn, src, false);
    Mat dst;
    dst = enhanceContrast(src);
    matToBitmap(env, dst, bitmapOut, false);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_imageeditor_utils_NativeMethodsProvider_backgroundSegmentation(
        JNIEnv* env,
        jobject p_this,
        jobject bitmapIn,
        int method,
        jboolean enableReset,
        jobject bitmapOut) {
    Mat src;
    bitmapToMat(env, bitmapIn, src, false);
    Mat dst = src.clone();
    bool wasMotionDetected = background_segmentation(src, method, enableReset, dst);
    matToBitmap(env, dst, bitmapOut, false);
    return wasMotionDetected;
}

extern "C" JNIEXPORT int JNICALL
Java_com_example_imageeditor_utils_NativeMethodsProvider_getClusters(
        JNIEnv* env,
        jobject p_this,
        jobject bitmapIn,
        jobject bitmapOut,
        jboolean getClusterSizeOnly,
        jboolean enableClusterMerge
) {
    static std::vector<Mat> clusters;
    static int index;
    if (getClusterSizeOnly == true) {
        Mat src;
        bitmapToMat(env, bitmapIn, src, false);
        bool enableMerge = enableClusterMerge;
        clusters = getClusters(src, enableMerge);
        index = 0;
    } else {
        Mat dst = clusters[index++].clone();
        matToBitmap(env, dst, bitmapOut, false);
    }
    return (int)clusters.size();
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_imageeditor_utils_NativeMethodsProvider_drawRectangle(
        JNIEnv* env,
        jobject p_this,
        jobject bitmapIn,
        jobject clusterBitmap,
        jstring messageString,
        jobject bitmapOut) {
    Mat src, cluster;
    bitmapToMat(env, bitmapIn, src, false);
    bitmapToMat(env, clusterBitmap, cluster, false);
    const char* message = (*env).GetStringUTFChars(messageString, 0);
    Mat dst = drawClusterRectangle(src, cluster, (char*)message);
    matToBitmap(env, dst, bitmapOut, false);
    (*env).ReleaseStringUTFChars(messageString, message);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_imageeditor_utils_NativeMethodsProvider_backgroundSegmentationDebug(
        JNIEnv* env,
        jobject p_this,
        jobject bitmapIn,
        jobject bitmapOut) {
    Mat src;
    bitmapToMat(env, bitmapIn, src, false);
    Mat dst;
    dst = background_segmentation_debug(src);
    matToBitmap(env, dst, bitmapOut, false);
}
