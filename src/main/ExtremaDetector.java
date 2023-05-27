package main;

import org.opencv.core.*;

import java.util.*;

import static org.opencv.imgproc.Imgproc.*;

public class ExtremaDetector {
    private static final double SIGMA = 1.6;    // octave中栈底图像的sigma
    private static final int S = 3;             // octave中高斯图像的interval数量
    private static final int MIN_SIDE_LEN = 64; // 图像短边的最短长度

    static ArrayList<Point> run(Mat grayFloat) {
        Mat baseImage = prepareBaseImage(grayFloat);
        ArrayList<Octave> octaves = generateOctaves(baseImage);
        return null; // FIXME
    }

    /**
     * 将输入图像放大到2倍并进行高斯滤波，作为基准图像
     *
     * @param grayFloat 原始的灰度图像
     * @return 放大并经过滤波的图像
     */
    static Mat prepareBaseImage(Mat grayFloat) {
        Mat baseImage = new Mat();
        resize(grayFloat, baseImage, new Size(), 2, 2, INTER_CUBIC);
        GaussianBlur(baseImage, baseImage, new Size(), SIGMA);
        return baseImage;
    }

    static ArrayList<Octave> generateOctaves(Mat baseImage) {
        ArrayList<Octave> octaves = new ArrayList<>();
        while (Util.min(baseImage.size()) > MIN_SIDE_LEN) {
            Octave octave = new Octave(baseImage, SIGMA, S);
            octaves.add(octave);
            // 取栈中倒数第3张图像作为下一个octave的栈底图像，这张图像的scale恰为2*SIGMA
            baseImage = octave.gaussianImages.get(octave.gaussianImages.size() - 3).image;
            resize(baseImage, baseImage, new Size(), 0.5);
        }
        return octaves;
    }

    static ArrayList<Point> detect(ArrayList<Octave> octaves) {
        ArrayList<Point> keyPoints = new ArrayList<>();
        for (Octave octave : octaves) {
            ArrayList<Mat> dogImages = octave.dogImages;
            for (int i = 1; i <= dogImages.size() - 2; i++) {
                Mat prev = dogImages.get(i - 1), curr = dogImages.get(i), next = dogImages.get(i + 1);
                for (int y = 1; y <= curr.size().height - 2; y++) {
                    for (int x = 1; x <= curr.size().width - 2; x++) {
                        // TODO
                    }
                }
                Util.foreachPixelDo(curr.width(), curr.height(), 1, 1, (x, y) -> {
                    System.out.printf(String.valueOf(curr.get(x, y)[0]));
                });
            }
        }
        return null; // FIXME
    }
}
