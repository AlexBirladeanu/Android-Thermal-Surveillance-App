#include "opencv-utils.h"
#include <opencv2/imgproc.hpp>
#include <queue>
#include <random>
#include <android/log.h>

Mat enhanceContrast(Mat src) {
    Mat dst;
    cvtColor(src, src, COLOR_BGR2GRAY);
    equalizeHist(src, dst);
    return dst;
}

Mat color2Grayscale(Mat src) {
    Mat grayscale;
    cvtColor(src, grayscale, COLOR_BGR2GRAY);
    return grayscale;
}


Mat drawRectangle(Mat src, Point topLeft, Point bottomRight) {
    Mat dst = src.clone();
    int thickness = 3;
    rectangle(dst, topLeft, bottomRight, Scalar(255, 0, 0), thickness, LINE_8);
    return dst;
}

Mat background_segmentation(Mat frame, int method, bool enableReset) {

    Mat gray; //current frame: original and gray
    static Mat backgnd; // background model
    static Mat diff; //difference image: |frame_gray - bacgnd|
    static Mat dst; //output image/frame
    static int frameNum = -1; //current frame counter
    if (enableReset) {
        frameNum = -1;
    }
    // method =
    // 1 - frame difference
    // 2 - running average
    // 3 - running average with selectivity
    const unsigned char Th = 15;
    const double alpha = 0.05;
    ++frameNum;
    char frameNumString[255];
    sprintf(frameNumString, "%d", frameNum);

    cvtColor(frame, gray, COLOR_BGR2GRAY);
    GaussianBlur(gray, gray, Size(5, 5), 0, 0);
    dst = Mat::zeros(gray.size(), gray.type());

    if (frameNum == 0) {
        backgnd = gray.clone();
        return frame;

    }

    const int channels_gray = gray.channels();
    if (channels_gray > 1)
        return frame;
    if (frameNum > 0) // daca nu este primul cadru
    {
        // Calcul imagine diferenta dintre cadrul current (gray) si fundal (backgnd)
        absdiff(gray, backgnd, diff);

        if (method == 1) {
            backgnd = gray.clone();
        }
        // Se parcurge sistematic matricea diff
        for (int i = 0; i < diff.rows; i++) {
            for (int j = 0; j < diff.cols; j++) {
                //daca valoarea pt.pixelul current diff.at<uchar>(i, j) > Th
                // marcheaza pixelul din imaginea destinatie ca obiect:
                if (diff.at<uchar>(i, j) > Th) {
                    dst.at<uchar>(i, j) = 255;
                } else {
                    // actualizeaza model background (doar pt. metoda 3)
                    if (method == 3) {
                        backgnd.at<uchar>(i, j) = alpha * gray.at<uchar>(i, j) +
                                                  (1.0 - alpha) * backgnd.at<uchar>(i, j);
                    }
                }
            }
        }
        Mat element = getStructuringElement(MORPH_CROSS, Size(3, 3));
        erode(dst, dst, element, Point(-1, -1), 2);
        dilate(dst, dst, element, Point(-1, -1), 4);
        erode(dst, dst, element, Point(-1, -1), 2);
    }
    int xMin = dst.cols - 1, xMax = 1, yMin = dst.rows - 1, yMax = 1;
    for (int i=0; i< dst.rows; i++) {
        for (int j= 0; j< dst.cols; j++) {
            if (dst.at<uchar>(i, j) == 255) {
                if (i < yMin) {
                    yMin = i;
                }
                if (i > yMax) {
                    yMax = i;
                }
                if (j < xMin) {
                    xMin = j;
                }
                if (j > xMax) {
                    xMax = j;
                }
            }
        }
    }
    if (xMin<5) {
        xMin = 5;
    }
    if (yMin<5) {
        yMin = 5;
    }
    if (xMax > 200) {
        xMax = 200;
    }
    if (yMax > 150) {
        yMax = 150;
    }

    Point topLeft(xMin, yMin);
    Point bottomRight(xMax, yMax);


//    __android_log_print(ANDROID_LOG_WARN, "BoundingBox", "xMin=%d", xMin);
//    __android_log_print(ANDROID_LOG_WARN, "BoundingBox", "xMax=%d", xMax);
//    __android_log_print(ANDROID_LOG_WARN, "BoundingBox", "yMin=%d", yMin);
//    __android_log_print(ANDROID_LOG_WARN, "BoundingBox", "yMax=%d", yMax);
//    int thickness = 3;
//    rectangle(dst, topLeft, bottomRight, Scalar(255, 0, 0), thickness, LINE_8);
    //return dst;
    return drawRectangle(frame, topLeft, bottomRight);
}
