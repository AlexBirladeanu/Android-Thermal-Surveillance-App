#pragma once

#include <opencv2/core.hpp>

using namespace cv;

void myFlip(Mat src);

Mat cannyEdgeDetection(Mat src);

Mat color2BW(Mat src);

Mat grayscale_segmentation(Mat src);

Mat region_growing_segmentation(int x, int y, Mat src);