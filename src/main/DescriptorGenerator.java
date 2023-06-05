package main;

import org.jblas.FloatMatrix;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;

import java.util.ArrayList;

/**
 * 生成特征描述子（论文第6章）
 */
public class DescriptorGenerator {

    private static final int D = 4;     // 一条轴上的子区域数量

    private static final int N_BIN = 8; // 每张直方图的堆栈数量

    public FloatMatrix generate(KeyPoint keyPoint, ArrayList<Octave> octaves) {
        int localGaussianIdx = Util.getLocalGaussianImageId(keyPoint);
        float localScale = Util.getLocalScale(keyPoint);
        GaussianImage gaussianImage = octaves.get(keyPoint.octave).gaussianImages.get(localGaussianIdx);
        int width = gaussianImage.image.width(), height = gaussianImage.image.height();
        float subregionWidth = 3 * localScale; // 子区域的半径
        int radius = Math.round((float) Math.sqrt(2) / 2 * subregionWidth * (D + 1)); // 采样半径

        FloatMatrix descriptor = FloatMatrix.zeros(D * D * N_BIN);

        // 采样
        for (int i = -radius; i <= radius; i++) {
            int x = Math.round((float) keyPoint.pt.x + i);
            if (x < 0 || x >= width) continue;
            for (int j = -radius; j <= radius; j++) {
                int y = Math.round((float) keyPoint.pt.y + j);
                if (y < 0 || y >= height) continue;

                // TODO 建立朝向张量
            }
        }
    }
}
