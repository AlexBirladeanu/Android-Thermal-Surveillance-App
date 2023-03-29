#pragma once

#include <opencv2/core.hpp>

using namespace cv;

Mat color2Grayscale(Mat src);

Mat enhanceContrast(Mat src);

Mat background_segmentation(Mat frame, int method, bool enableReset);

Mat dbScanCluster(Mat src);

std::vector<Mat> getClusters(Mat src);