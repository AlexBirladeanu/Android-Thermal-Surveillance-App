#pragma once

#include <opencv2/core.hpp>

using namespace cv;

Mat color2Grayscale(Mat src);

Mat enhanceContrast(Mat src);

Mat background_segmentation(Mat frame, int method, bool enableReset);

std::vector<Mat> mergeBodyClusters(std::vector<Mat> originalClusters, Mat originalFrame);

std::vector<Mat> getClusters(Mat src, bool enableMerge);

Mat drawClusterRectangle(Mat src, Mat cluster, bool isFaceCluster);