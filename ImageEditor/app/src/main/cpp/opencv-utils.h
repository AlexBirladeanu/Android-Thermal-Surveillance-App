#pragma once

#include <opencv2/core.hpp>

using namespace cv;

Mat color2Grayscale(Mat src);

Mat enhanceContrast(Mat src);

bool background_segmentation(Mat frame, int method, bool enableReset, Mat& result);

std::vector<Mat> getClusters(Mat src, bool enableMerge);

Mat drawClusterRectangle(Mat src, Mat cluster, char* message);

Mat background_segmentation_debug(Mat frame);