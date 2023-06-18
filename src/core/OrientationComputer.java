package core;

import flib.MathX;
import org.jblas.FloatMatrix;
import org.opencv.core.KeyPoint;

import java.util.ArrayList;

/**
 * 计算关键点朝向（论文第5章）
 */
public class OrientationComputer {

    private static final int N_BIN = 36;            // 朝向直方图的堆栈数量
    private static final float PEAK_RATIO = 0.8f;   // 峰值比例；如果朝向直方图中某个堆栈的值大于最大值的一定比例，则会在此堆栈对应的方向上也建立一个关键点

    public ArrayList<KeyPoint> run(ArrayList<KeyPoint> keyPoints, ArrayList<Octave> octaves) {
        System.out.print("Computing orientations...");
        ArrayList<KeyPoint> keyPointsWithOrientations = new ArrayList<>();
        for (KeyPoint keyPoint : keyPoints) {
            FloatMatrix hist = computeOrientationHist(keyPoint, octaves);
            ArrayList<Float> orientations = computeOrientations(hist);
            for (Float orientation : orientations) {
                KeyPoint keyPointWithOrientation = Util.keyPointDeepCopy(keyPoint);
                keyPointWithOrientation.angle = orientation;
                keyPointsWithOrientations.add(keyPointWithOrientation);
            }
        }
        System.out.println("DONE");
        return keyPointsWithOrientations;
    }

    /**
     * 在以关键点为中心的区域内计算朝向直方图
     *
     * @param keyPoint 关键点
     * @param octaves  octaves
     * @return 以keyPoint为中心的区域内的朝向直方图
     */
    private static FloatMatrix computeOrientationHist(KeyPoint keyPoint, ArrayList<Octave> octaves) {
        GaussianImage gaussianImage = octaves.get(keyPoint.octave).gaussianImages.get(Util.getLocalGaussianImageId(keyPoint));
        int centerX = (int) Math.round(keyPoint.pt.x), centerY = (int) Math.round(keyPoint.pt.y);
        float localScale = Util.global2LocalScale(keyPoint.size, keyPoint.octave);
        float sigma = 1.5f * localScale; // 高斯加权的标准差
        int radius = Math.round(3 * sigma); // 采样像素的半径
        FloatMatrix hist = FloatMatrix.zeros(N_BIN);
        float binWidth = 2 * (float) Math.PI / N_BIN;

        for (int i = -radius; i <= radius; i++) {
            int x = centerX + i;
            if (x < 1 || x >= gaussianImage.image.width() - 1) continue; // 计算梯度需要左右两个像素的值，因此边界检查需要留出1的边距
            for (int j = -radius; j <= radius; j++) {
                int y = centerY + j;
                if (y < 1 || y >= gaussianImage.image.height() - 1) continue;

                // 计算采样点的梯度和朝向
                float dx = (float) (gaussianImage.image.get(y, x + 1)[0] - gaussianImage.image.get(y, x - 1)[0]);
                float dy = (float) (gaussianImage.image.get(y + 1, x)[0] - gaussianImage.image.get(y - 1, x)[0]);
                float magnitude = (float) (Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2)));
                float theta = (float) (Math.atan2(dy, dx) + Math.PI);

                // 将加权幅值加入合适的bin
                int bin = (int) Math.floor(theta / binWidth);
                if (bin < 0) bin = 0;
                if (bin > N_BIN - 1) bin = N_BIN - 1;
                float weight = (float) MathX.gauss(Math.sqrt(i * i + j * j), sigma);
                hist.put(bin, hist.get(bin) + weight * magnitude);
            }
        }

        return hist;
    }

    /**
     * 基于朝向直方图，计算关键点的朝向。一个关键点可能有多个朝向，与周围像素的梯度方向有关。
     *
     * @param hist 朝向直方图
     * @return 关键点的朝向
     */
    private static ArrayList<Float> computeOrientations(FloatMatrix hist) {
        // 寻找最大值
        float maxVal = hist.get(0);
        for (int i = 1; i < hist.length; i++) {
            if (hist.get(i) > maxVal) maxVal = hist.get(i);
        }
        float threshold = PEAK_RATIO * maxVal;
        float binWidth = 360f / N_BIN;

        // 确定感兴趣的朝向，并插值获得更精确的位置
        ArrayList<Float> orientations = new ArrayList<>();
        for (int i = 0; i < hist.length; i++) {
            int left = (i - 1 + hist.length) % hist.length, right = (i + 1) % hist.length;
            float val = hist.get(i), leftVal = hist.get(left), rightVal = hist.get(right);
            if (val > threshold && val > leftVal && val > rightVal) { // 幅值达到阈值，并且是局部极值
                float p = 0.5f * (leftVal - rightVal) / (leftVal - 2 * val + rightVal); // Ref: https://ccrma.stanford.edu/~jos/sasp/Quadratic_Interpolation_Spectral_Peaks.html
                float interpolatedIdx = (i + p + hist.length) % hist.length;
                float orientation = interpolatedIdx * binWidth + binWidth / 2; // (0,360°)
                orientations.add(orientation);
            }
        }
        return orientations;
    }
}
