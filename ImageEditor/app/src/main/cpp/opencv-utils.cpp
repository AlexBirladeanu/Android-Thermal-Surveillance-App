#include "opencv-utils.h"
#include <opencv2/imgproc.hpp>
#include <opencv2/core.hpp>
#include <queue>
#include <random>
#include <android/log.h>

using namespace cv;
using namespace std;

Mat enhanceContrast(Mat src) {
    Mat dst;
    cvtColor(src, src, COLOR_BGR2GRAY);
    Mat dst2;
    equalizeHist(src, dst2);
    GaussianBlur(dst2, dst, Size(5, 5), 0);
    return dst;
}

Mat color2Grayscale(Mat src) {
    Mat grayscale;
    cvtColor(src, grayscale, COLOR_BGR2GRAY);
    return grayscale;
}

Mat crop(Mat src, int xMin, int xMax, int yMin, int yMax) {
    Mat dst = src.clone();

    for (int i = 0; i < dst.rows; i++) {
        for (int j = 0; j < dst.cols; j++) {
            if (j < xMin || j > xMax || i < yMin || i > yMax) {
                dst.at<uchar>(i, j) = 0;
            }
        }
    }

    return dst;
}

Mat drawRectangle(Mat src, Point topLeft, Point bottomRight, char *message) {
    Mat dst = src.clone();
    int thickness = 3;
    rectangle(dst, topLeft, bottomRight, Scalar(255, 0, 0), thickness, LINE_8);
    putText(dst, message, Point(topLeft.x, bottomRight.y), FONT_HERSHEY_SIMPLEX, 0.5, Scalar(255, 255, 255), 1, LINE_AA);
    return dst;
}

Mat drawClusterRectangle(Mat frame, Mat cluster, char* message) {
    Mat gray;
    cvtColor(cluster, gray, COLOR_BGR2GRAY);
    Mat bw;//binary
    threshold(gray, bw, 0, 255, THRESH_BINARY | THRESH_OTSU);

    int xMin = bw.cols - 1;
    int xMax = 1;
    int yMin = bw.rows - 1;
    int yMax = 1;
    for (int i = 0; i < bw.rows; i++) {
        for (int j = 0; j < bw.cols; j++) {
            if (bw.at<uchar>(i, j) == 255) {
                //__android_log_print(ANDROID_LOG_WARN,"letsgoo", "found white at i=%d and j=%d", i, j);
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
    if (xMin < 5) {
        xMin = 5;
    }
    if (yMin < 5) {
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

    //return bw;
    return drawRectangle(frame, topLeft, bottomRight, message);
}

vector<Mat> mergeBodyClusters(vector<Mat> originalClusters, Mat originalFrame) {


    vector<int> xMinVector;
    vector<int> xMaxVector;
    vector<int> yMinVector;
    vector<int> yMaxVector;

    for (auto &cluster: originalClusters) {

        Mat bw;//binary
        threshold(cluster, bw, 0, 255, THRESH_BINARY | THRESH_OTSU);

        int xMin = bw.cols - 1;
        int xMax = 1;
        int yMin = bw.rows - 1;
        int yMax = 1;
        for (int i = 0; i < bw.rows; i++) {
            for (int j = 0; j < bw.cols; j++) {
                if (bw.at<uchar>(i, j) == 255) {
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
        if (xMin < 5) {
            xMin = 5;
        }
        if (yMin < 5) {
            yMin = 5;
        }
        if (xMax > 200) {
            xMax = 200;
        }
        if (yMax > 150) {
            yMax = 150;
        }
        xMinVector.push_back(xMin);
        xMaxVector.push_back(xMax);
        yMinVector.push_back(yMin);
        yMaxVector.push_back(yMax);
    }


    vector<int> alreadyVerifiedIndexes;
    vector<Mat> newClusters;

    Mat grayscaleFrame;
    cvtColor(originalFrame, grayscaleFrame, COLOR_BGR2GRAY);

    for (int i=0; i<originalClusters.size(); i++) {
        bool isAlreadyVerified = false;
        for (auto &index: alreadyVerifiedIndexes) {
            if (index == i) {
                isAlreadyVerified = true;
            }
        }
        if (isAlreadyVerified) {
            continue;
        }

        int mergedClusterXMin = xMinVector[i];
        int mergedClusterXMax = xMaxVector[i];
        int mergedClusterYMin = yMinVector[i];
        int mergedClusterYMax = yMaxVector[i];
        for (int j=0; j<originalClusters.size(); j++) {
            if (i == j) {
                continue;
            }
            //merge lower body cluster with upper body cluster
            //if (yMinVector[j] >= yMinVector[i]) { //second cluster is below the first
                if ((xMinVector[j] >= xMinVector[i] - (xMaxVector[i] - xMinVector[i]) / 2) || (xMaxVector[j] <= xMaxVector[i] + (xMaxVector[i] - xMinVector[i]) / 2)) {
                    if (mergedClusterXMin < xMinVector[j]) {
                        mergedClusterXMin = xMinVector[j];
                    }
                    if (mergedClusterXMax < xMaxVector[j]) {
                        mergedClusterXMax = xMaxVector[j];
                    }
                    if (mergedClusterYMin < yMinVector[j]) {
                        mergedClusterYMin = yMinVector[j];
                    }
                    if (mergedClusterYMax < yMaxVector[j]) {
                        mergedClusterYMax = yMaxVector[j];
                    }
                    alreadyVerifiedIndexes.push_back(j);
                    alreadyVerifiedIndexes.push_back(i);
                }
            }
        //}

        for (auto &index: alreadyVerifiedIndexes) {
            if (index == i) {
                isAlreadyVerified = true;
            }
        }
        if (!isAlreadyVerified) {
            //keep original cluster
            newClusters.push_back(originalClusters[i]);
        } else {
            //add resulting cluster
            newClusters.push_back(crop(grayscaleFrame, mergedClusterXMin, mergedClusterXMax, mergedClusterYMin, mergedClusterYMax));
        }

    }

    return newClusters;
}

vector<Mat> getClusters(Mat src, bool enableMerge) {
    Mat gray;
    cvtColor(src, gray, COLOR_BGR2GRAY);
    Mat bw;//binary
    threshold(gray, bw, 0, 255, THRESH_BINARY | THRESH_OTSU);

    double eps = 5;
    int minPoints = 49;
    // Find contours in the image
    vector<vector<Point>> contours;
    findContours(bw, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

    // Create a vector to hold the cluster labels for each contour
    vector<int> labels(contours.size(), -1);

    // Assign each contour to a cluster based on its size
    int currentLabel = 0;
    for (size_t i = 0; i < contours.size(); i++) {
        if (labels[i] != -1) continue; // skip already labeled contours
        int count = (int) contours[i].size();
        if (count >= minPoints) {
            labels[i] = currentLabel;
            for (size_t j = i + 1; j < contours.size(); j++) {
                if (labels[j] != -1) continue; // skip already labeled contours
                int count2 = (int) contours[j].size();
                if (count2 >= minPoints && norm(contours[i][0] - contours[j][0]) < eps) {
                    labels[j] = currentLabel;
                }
            }
            currentLabel++;
        }
    }

    vector<Mat> clusters;

    vector<int> xMinVector;
    vector<int> xMaxVector;
    vector<int> yMinVector;
    vector<int> yMaxVector;

    for (size_t i = 0; i < contours.size(); i++) {
        int label = labels[i];
        if (label == -1) continue; // skip unlabeled contours

        int xMin = src.cols - 1;
        int xMax = 1;
        int yMin = src.rows - 1;
        int yMax = 1;
        for (auto &point: contours[i]) {
            if (point.x < xMin) {
                xMin = point.x;
            }
            if (point.x > xMax) {
                xMax = point.x;
            }
            if (point.y < yMin) {
                yMin = point.y;
            }
            if (point.y > yMax) {
                yMax = point.y;
            }
        }

        //check that min and max coordinates are inside the image
        if (xMin < 5) {
            xMin = 5;
        }
        if (yMin < 5) {
            yMin = 5;
        }
        if (xMax > 200) {
            xMax = 200;
        }
        if (yMax > 150) {
            yMax = 150;
        }
        if ((xMax - xMin) > (yMax - yMin)) {//check if height is greater than width to verify person
            if (!enableMerge) {
                xMinVector.push_back(xMin);
                xMaxVector.push_back(xMax);
                yMinVector.push_back(yMin);
                yMaxVector.push_back(yMax);
                clusters.push_back(crop(gray, xMin, xMax, yMin, yMax));
            } else {
                bool wasMergedAtLeastOnce = false;
                for (int j=0; j<clusters.size(); j++) {
                    //if ((yMax - yMin) / 2 <= (yMaxVector[j] - yMinVector[j]) / 2) {//if gravity center of current cluster is below gravity center of another cluster
                    int halfOfClusterWidth = (xMaxVector[j] - xMinVector[j]) / 2;
                    if (xMax > xMinVector[j] - halfOfClusterWidth || xMin < xMaxVector[j] + halfOfClusterWidth) {
                        if (xMin < xMinVector[j]) {
                            xMinVector[j] = xMin;
                        }
                        if (xMax > xMaxVector[j]) {
                            xMaxVector[j] = xMax;
                        }
                        if (yMax > yMaxVector[j]) {
                            yMaxVector[j] = yMax;
                        }
                        wasMergedAtLeastOnce = true;
                        clusters[j] = crop(gray, xMinVector[j], xMaxVector[j], yMinVector[j], yMaxVector[j]);
                    }
                    //}
                }
                if (!wasMergedAtLeastOnce) {
                    xMinVector.push_back(xMin);
                    xMaxVector.push_back(xMax);
                    yMinVector.push_back(yMin);
                    yMaxVector.push_back(yMax);
                    clusters.push_back(crop(gray, xMin, xMax, yMin, yMax));
                }
            }
        }
    }
    return clusters;
}

bool background_segmentation(Mat frame, int method, bool enableReset, Mat& result) {
    __android_log_print(ANDROID_LOG_WARN,"Reparatii", "background_segmentation");

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

    cvtColor(frame, gray, COLOR_BGR2GRAY);
    GaussianBlur(gray, gray, Size(5, 5), 0, 0);
    dst = Mat::zeros(gray.size(), gray.type());

    if (frameNum == 0) {
        backgnd = gray.clone();
        //return frame;
        result = frame;
        return false;
    }

    const int channels_gray = gray.channels();
    if (channels_gray > 1) {
        //return frame;
        result = frame;
        return false;
    }
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
    bool isMovement = false;
    for (int i = 0; i < dst.rows; i++) {
        for (int j = 0; j < dst.cols; j++) {
            if (dst.at<uchar>(i, j) == 255) {
                isMovement = true;
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
    if (isMovement) {
        if (xMin < 5) {
            xMin = 5;
        }
        if (yMin < 5) {
            yMin = 5;
        }
        if (xMax > 200) {
            xMax = 200;
        }
        if (yMax > 150) {
            yMax = 150;
        }
    } else {
        xMin = frame.cols;
        xMax = 0;
        yMin = frame.rows;
        yMax = 0;
    }
    //return crop(frame, xMin, xMax, yMin, yMax);
    //return dst;
    result = crop(gray, xMin, xMax, yMin, yMax);
    return isMovement;
}
