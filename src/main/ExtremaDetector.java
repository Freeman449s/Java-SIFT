package main;

import org.opencv.core.*;

import java.util.*;
import java.util.concurrent.TimeoutException;

import static org.opencv.imgproc.Imgproc.*;

/**
 * 尺度空间极值检测（论文第3章）
 */
public class ExtremaDetector {
    private static final double SIGMA = 1.6;    // octave中栈底图像的sigma
    private static final int S = 3;             // octave中高斯图像的interval数量
    private static final int MIN_SIDE_LEN = 64; // 图像短边的最短长度

    Mat baseImage = null;
    ArrayList<Octave> octaves = null;
    ArrayList<KeyPoint> keyPoints = null;

    public ArrayList<KeyPoint> run(Mat grayFloat) throws InterruptedException, TimeoutException {
        baseImage = prepareBaseImage(grayFloat);
        ArrayList<Octave> octaves = generateOctaves(baseImage);
        return detect(octaves);
    }

    /**
     * 将输入图像放大到2倍并进行高斯滤波，作为基准图像
     *
     * @param grayFloat 原始的灰度图像
     * @return 放大并经过滤波的图像
     */
    private static Mat prepareBaseImage(Mat grayFloat) {
        Mat baseImage = new Mat();
        resize(grayFloat, baseImage, new Size(), 2, 2, INTER_CUBIC);
        GaussianBlur(baseImage, baseImage, new Size(), SIGMA);
        return baseImage;
    }

    private static ArrayList<Octave> generateOctaves(Mat baseImage) {
        ArrayList<Octave> octaves = new ArrayList<>();
        while (Util.min(baseImage.size()) > MIN_SIDE_LEN) {
            Octave octave = new Octave(baseImage, SIGMA, S);
            octaves.add(octave);
            // 取栈中倒数第3张图像作为下一个octave的栈底图像，这张图像的scale恰为2*SIGMA
            baseImage = octave.gaussianImages.get(octave.gaussianImages.size() - 3).image;
            resize(baseImage, baseImage, new Size(), 0.5, 0.5);
        }
        return octaves;
    }

    private static ArrayList<KeyPoint> detect(ArrayList<Octave> octaves) throws InterruptedException, TimeoutException {
        ArrayList<KeyPoint> keyPoints = new ArrayList<>();
        for (int octaveNo = 0; octaveNo < octaves.size(); octaveNo++) {
            Octave octave = octaves.get(octaveNo);
            ArrayList<Mat> dogImages = octave.dogImages;
            for (int i = 1; i <= dogImages.size() - 2; i++) {
                Mat prev = dogImages.get(i - 1), curr = dogImages.get(i), next = dogImages.get(i + 1);
                final int finalOctaveNo = octaveNo, finalI = i; // 用于Lambda的临时final变量
                Util.foreachPixelParallelDo(curr.width(), curr.height(), 1, 1, (x, y) -> {
                    double val = curr.get(y, x)[0];
                    boolean minFlag = true, maxFlag = true;
                    // 检查中心点是否为极值点
                    for (int a = -1; a <= 1; a++) {
                        for (int b = -1; b <= 1; b++) {
                            if (prev.get(y + a, x + b)[0] < val ||
                                    curr.get(y + a, x + b)[0] < val ||
                                    next.get(y + a, x + b)[0] < val) minFlag = false;
                            if (prev.get(y + a, x + b)[0] > val ||
                                    curr.get(y + a, x + b)[0] > val ||
                                    next.get(y + a, x + b)[0] > val) maxFlag = false;
                        }
                    }

                    if (minFlag || maxFlag) {
                        double scale = SIGMA * Math.pow(2, finalI * 1.0 / S) * Math.pow(2, finalOctaveNo);
                        KeyPoint keyPoint = new KeyPoint(x, y, (float) scale, -1, 0, finalOctaveNo);
                        synchronized (keyPoints) {
                            keyPoints.add(keyPoint);
                        }
                    }
                });
            }
        }
        return keyPoints;
    }
}
