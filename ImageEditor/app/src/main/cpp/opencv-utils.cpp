#include "opencv-utils.h"
#include <opencv2/imgproc.hpp>
#include <queue>
#include <random>

void myFlip(Mat src) {
    flip(src, src, 0);
}

bool isOk(int new_x, int new_y, int width, int height)
{
    if (new_x < 0 || new_y < 0 || new_x >= width || new_y >= height)
        return false;

    return true;
}

Mat cannyEdgeDetection(Mat src) {
    Mat gauss, dst;
    int k = 0.4;
    int pH = 50;
    int pL = k * pH;
    Mat grayscale;
    cvtColor(src, grayscale, COLOR_BGR2GRAY);
    GaussianBlur(grayscale, gauss, Size(5, 5), 0.8, 0.8);
    Canny(gauss, dst, pL, pH, 3);
    return dst;
}

Mat color2BW(Mat src) {
    float media1 = 0, media2 = 0, T = 0, T2 = 0;
    int min = 256, max = 0, N1 = 0, N2 = 0;
    int hist[256] = {};
    Mat grayscale;
    cvtColor(src, grayscale, COLOR_BGR2GRAY);
    for (int i = 1; i < grayscale.rows - 1; i++) {
        for (int j = 1; j < grayscale.cols - 1; j++) {
            hist[grayscale.at<uchar>(i, j)]++;
            if (grayscale.at<uchar>(i, j) < min)
                min = grayscale.at<uchar>(i, j);
            if (grayscale.at<uchar>(i, j) > max)
                max = grayscale.at<uchar>(i, j);
        }
    }
    T = (min + max) / 2;
    while (abs(T - T2) >= 0.1) {
        T2 = T;
        media1 = 0;
        media2 = 0;
        N1 = 0;
        N2 = 0;
        for (int i = min; i < T; i++) {
            media1 += i * hist[i];
            N1 += hist[i];
        }
        for (int i = T + 1; i <= max; i++) {
            media2 += i * hist[i];
            N2 += hist[i];
        }
        media1 /= N1;
        media2 /= N2;
        T = (media1 + media2) / 2;
    }
    for (int i = 0; i < grayscale.rows; i++) {
        for (int j = 0; j < grayscale.cols; j++) {
            if (grayscale.at<uchar>(i, j) <= T)
                grayscale.at<uchar>(i, j) = 0;
            else
                grayscale.at<uchar>(i, j) = 255;
        }
    }
    return grayscale;
}

Mat grayscale_segmentation(Mat src) {
    uchar val;
    uchar label = 0;
    int di[8] = {-1, -1, -1, 0, 0, 1, 1, 1};
    int dj[8] = {-1, 0, 1, -1, 1, -1, 0, 1};
    Vec3b culoare;
    Mat grayscale = color2BW(src);
    Mat labels = Mat::zeros(grayscale.rows, grayscale.cols, CV_8UC1);
    Mat dst(grayscale.rows, grayscale.cols, CV_8UC3, Scalar(255, 255, 255));

    for (int i = 1; i < grayscale.rows - 1; i++) {
        for (int j = 1; j < grayscale.cols - 1; j++) {
            val = grayscale.at<uchar>(i, j);
            if ((val == 0) && (labels.at<uchar>(i, j) == 0)) {
                label++;
                std::queue<Point> Q;
                labels.at<uchar>(i, j) = label;
                Q.push({i, j});
                while (!Q.empty()) {
                    Point q = Q.front();
                    Q.pop();
                    for (int k = 0; k < 8; k++) {
                        if ((grayscale.at<uchar>(q.x + di[k], q.y + dj[k]) == 0) &&
                            (labels.at<uchar>(q.x + di[k], q.y + dj[k]) == 0)) {
                            labels.at<uchar>(q.x + di[k], q.y + dj[k]) = label;
                            Q.push({q.x + di[k], q.y + dj[k]});
                        }
                    }
                }
            }
        }
    }

    std::default_random_engine gen;
    std::uniform_int_distribution<int> d(0, 255);

    for (int k = 1; k <= label; k++) {
        uchar red = d(gen);
        uchar blue = d(gen);
        uchar green = d(gen);
        Vec3b color = Vec3b(blue, green, red);
        for (int i = 1; i < grayscale.rows - 1; i++) {
            for (int j = 1; j < grayscale.cols - 1; j++) {
                if (labels.at<uchar>(i, j) == k)
                    dst.at<Vec3b>(i, j) = color;
            }
        }
    }
    return dst;
}

Mat region_growing_segmentation(int x, int y, Mat src) {
    GaussianBlur(src, src, Size(5, 5), 0, 0);
    Mat H = Mat(src.rows, src.cols, CV_8UC1);
    uchar* lpH = H.data;
    Mat hsv;
    cvtColor(src, hsv, COLOR_BGR2HSV);
    uchar* hsvDataPtr = hsv.data;
    for (int i = 0; i < src.rows; i++) {
        for (int j = 0; j < src.cols; j++) {
            int hi = i * src.cols * 3 + j * 3;
            int gi = i * src.cols + j;
            lpH[gi] = hsvDataPtr[hi] * 510 / 360;
        }
    }
    //H is the new src
    Mat labels = Mat::zeros(H.size(), CV_16UC1);
    int		w = 3,
            hue_avg = 0,
            inf_x, sup_x,
            inf_y, sup_y,
            cnt = 0,
            height = H.rows,
            width = H.cols;

    inf_x = (x - w < 0) ? 0 : x - w;
    inf_y = (y - w < 0) ? 0 : y - w;
    sup_x = (x + w >= width) ? (width - 1) : x + w;
    sup_y = (y + w >= height) ? (height - 1) : y + w;
    for (int i = inf_y; i <= sup_y; ++i)
    {
        for (int j = inf_x; j <= sup_x; ++j)
        {
            hue_avg += H.data[i * width + j];
        }
    }
    hue_avg /= (sup_x - inf_x + 1) * (sup_y - inf_y + 1);

    int k = 1, N = 1, hue_std = 10;
    int konst = 3;

    int T = konst * (float)hue_std;

    std::queue<Point> Q;
    Q.push(Point(x, y));

    while (!Q.empty())
    {
        int dx[8] = { -1, 0, 1, 1, 1, 0, -1, -1 };
        int dy[8] = { -1, -1, -1, 0, 1, 1, 1, 0 };

        Point temp = Q.front();
        Q.pop();

        for (int dir = 0; dir < 8; ++dir)
        {
            int new_x = temp.x + dx[dir];
            int new_y = temp.y + dy[dir];

            if (isOk(new_x, new_y, width, height))
            {
                if (labels.at<ushort>(new_y, new_x) == 0)
                {
                    if (abs(H.at<uchar>(new_y, new_x) - hue_avg) < T)
                    {
                        Q.push(Point(new_x, new_y));
                        labels.at<ushort>(new_y, new_x) = k;
                        hue_avg = ((N * hue_avg) + H.at<uchar>(new_y, new_x)) / (N + 1);
                        ++N;
                    }
                }
            }
        }
    }

    Mat dst = H.clone();

    for (int i = 0; i < height; i++)
    {
        for (int j = 0; j < width; j++)
        {
            if (labels.at<ushort>(i, j) == 1)
            {
                dst.at<uchar>(i, j) = 255;
            }
            else
            {
                dst.at<uchar>(i, j) = 0;
            }
        }
    }
    return dst;
}
