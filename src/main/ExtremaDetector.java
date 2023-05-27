package main;

import org.opencv.core.*;

import java.util.*;
import java.util.concurrent.TimeoutException;

import static org.opencv.imgproc.Imgproc.*;

/**
 * 尺度空间极值检测（论文第3章）
 */
public class ExtremaDetector {
    private static final int MIN_SIDE_LEN = 64; // 图像短边的最短长度

    public Mat baseImage = null;
    public ArrayList<Octave> octaves = null;
    public ArrayList<KeyPoint> keyPoints = null;

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
        GaussianBlur(baseImage, baseImage, new Size(), GlobalParam.SIGMA);
        return baseImage;
    }

    /**
     * 生成octaves
     *
     * @param baseImage 基础图像
     * @return octaves
     */
    private static ArrayList<Octave> generateOctaves(Mat baseImage) {
        ArrayList<Octave> octaves = new ArrayList<>();
        while (Util.min(baseImage.size()) > MIN_SIDE_LEN) {
            Octave octave = new Octave(baseImage, GlobalParam.SIGMA, GlobalParam.S);
            octaves.add(octave);
            // 取栈中倒数第3张图像作为下一个octave的栈底图像，这张图像的scale恰为2*SIGMA
            baseImage = octave.gaussianImages.get(octave.gaussianImages.size() - 3).image;
            resize(baseImage, baseImage, new Size(), 0.5, 0.5);
        }
        return octaves;
    }

    /**
     * 极值点检测。如果一个点的响应不大于或不小于周围26个点，那么该点将被认为是极值点。
     *
     * @param octaves octaves
     * @return 检测到的极值点
     * @throws InterruptedException 如果线程池在等待线程运行完毕时被中断，将抛出此异常
     * @throws TimeoutException     如果线程池未能在规定时间（1小时）内完成任务，将抛出此异常
     */
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
                        double scale = GlobalParam.SIGMA * Math.pow(2, finalI * 1.0 / GlobalParam.S) * Math.pow(2, finalOctaveNo);
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
