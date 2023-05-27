package main;

import org.opencv.core.*;

import java.util.*;

import static org.opencv.core.Core.*;
import static org.opencv.imgproc.Imgproc.*;

public class Octave {
    final ArrayList<GaussianImage> gaussianImages = new ArrayList<>();
    final ArrayList<Mat> dogImages = new ArrayList<>();

    public Octave(GaussianImage bottomImage) {
        gaussianImages.add(bottomImage);
    }

    /**
     * 构造函数。将自动构建octave。
     *
     * @param bottomImage octave的栈底图像，必须已经过高斯滤波。
     * @param sigma       栈底图像高斯滤波的标准差。
     * @param s           octave中的间隙数，将决定octave中的图像数量，以及每张高斯图像使用的标准差。
     */
    public Octave(Mat bottomImage, double sigma, int s) {
        this(new GaussianImage(bottomImage, sigma));
        buildOctave(bottomImage, sigma, s);
    }

    public Octave(List<GaussianImage> gaussianImages, List<Mat> dogImages) {
        this.gaussianImages.addAll(gaussianImages);
        this.dogImages.addAll(dogImages);
    }

    /**
     * 构建octave。将对bottomImage进行标准差为kσ, k^2σ,...,2σ, 2kσ, 2k^2σ的高斯滤波，以生成高斯图像；k=2^(1/s)。
     * 之后，相邻的高斯图像将相减，以得到DoG图像。
     *
     * @param bottomImage octave的栈底图像，必须已经过高斯滤波。
     * @param sigma       栈底图像高斯滤波的标准差。
     * @param s           octave中的间隙数，将决定octave中的图像数量，以及每张高斯图像使用的标准差。
     */
    private void buildOctave(Mat bottomImage, double sigma, int s) {
        double k = Math.pow(2, 1. / s);
        for (int i = 1; i <= s + 2; i++) {
            sigma *= k;
            Mat gaussianImage = new Mat();
            GaussianBlur(bottomImage, gaussianImage, new Size(), sigma);
            gaussianImages.add(new GaussianImage(gaussianImage, sigma));
        }

        for (int i = 1; i < gaussianImages.size(); i++) {
            Mat lastImage = gaussianImages.get(i - 1).image, thisImage = gaussianImages.get(i).image;
            Mat dog = new Mat();
            subtract(lastImage, thisImage, dog);
            dogImages.add(dog);
        }
    }
}

class GaussianImage {
    Mat image;
    final double sigma;

    GaussianImage(Mat image, double sigma) {
        this.image = image;
        this.sigma = sigma;
    }
}