package main;

import flib.MathX;
import org.jblas.FloatMatrix;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;

import java.util.ArrayList;

/**
 * 计算关键点朝向（论文第5章）
 */
public class OrientationComputer {

    private static final int N_BIN = 36;

    public ArrayList<KeyPoint> run(ArrayList<KeyPoint> keyPoints, ArrayList<Octave> octaves) {

    }

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

    private static ArrayList<Float> computeOrientations(FloatMatrix hist) {

    }
}
