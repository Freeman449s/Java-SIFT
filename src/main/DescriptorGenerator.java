package main;

import flib.MathX;
import org.jblas.FloatMatrix;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;

import java.util.ArrayList;

import static org.opencv.core.CvType.*;

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
        float keyPointRad = (float) MathX.deg2Rad(keyPoint.angle); // 中心像素的朝向（弧度制）
        FloatMatrix spatialBinCenters = new FloatMatrix(new float[]{-2, -1, 0, 1, 2}); // 空间堆栈的中心位置
        float spatialBinWidth = 1;
        FloatMatrix orientationsBinCenters = new FloatMatrix(N_BIN); // 朝向堆栈的中心位置
        float orientationBinWidth = 2 * (float) Math.PI / N_BIN;
        for (int i = 0; i < N_BIN; i++)
            orientationsBinCenters.put(i, i * orientationBinWidth + orientationBinWidth / 2);

        FloatMatrix descriptor = FloatMatrix.zeros(D * D * N_BIN);
        Mat orientationTensor = Mat.zeros(new int[]{D, D, N_BIN}, CV_32F);

        // 采样
        for (int i = -radius; i <= radius; i++) {
            int x = Math.round((float) keyPoint.pt.x + i);
            if (x < 1 || x >= width - 1) continue; // 计算梯度需要左右两个像素的值，因此边界检查需要留出1的边距
            for (int j = -radius; j <= radius; j++) {
                int y = Math.round((float) keyPoint.pt.y + j);
                if (y < 1 || y >= height - 1) continue;

                // 坐标转换到与朝向相关的局部坐标系
                float yLocal = x * (float) Math.sin(keyPointRad) + y * (float) Math.cos(keyPointRad);
                float xLocal = x * (float) Math.cos(keyPointRad) - y * (float) Math.sin(keyPointRad);
                float yNorm = yLocal / subregionWidth, xNorm = xLocal / subregionWidth; // 归一化并约束在(-(D+1)/2,(D+1)/2)范围，越界的点跳过
                if (yNorm < -(D + 1) / 2.0 || y > (D + 1) / 2.0 || xNorm < -(D + 1) / 2.0 || xNorm > (D + 1) / 2.0)
                    continue;

                FloatMatrix gradient = Util.computeGradient(x, y, gaussianImage.image);
                float magnitude = gradient.norm2();
                float weight = (float) MathX.gauss(Math.sqrt(yNorm * yNorm + xNorm * xNorm), 0.5 * D);
                float weightedMagnitude = weight * magnitude;
                float orientation = (float) (Math.atan2(gradient.get(1), gradient.get(0)) + Math.PI);
                float orientationLocal = (orientation - keyPointRad + 2 * (float) Math.PI) % (2 * (float) Math.PI);

                // 计算相邻的bin序号
                int[] xAdjBinIds = getAdjacentBinIds(xNorm, spatialBinCenters), yAdjBinIds = getAdjacentBinIds(yNorm, spatialBinCenters),
                        orientationAdjBinIds = getAdjacentBinIds(orientationLocal, orientationsBinCenters);
                // 计算在x，y，角度维度，在左侧bin上的权重 TODO 找一种简洁的方式实现
                float xFraction, yFraction, orientationFraction;
                if (xAdjBinIds[0] < xAdjBinIds[1])  // 非边界情况
                    xFraction = (spatialBinCenters.get(xAdjBinIds[1]) - xNorm) / spatialBinWidth; // 权重与另一侧的距离正相关
                else { // 边界情况
                    if (xNorm < 0) // 左边界
                        xFraction = (spatialBinCenters.get(xAdjBinIds[1]) - xNorm) / spatialBinWidth;
                    else { // 右边界
                        xFraction = (xNorm - spatialBinCenters.get(xAdjBinIds[0])) / spatialBinWidth;
                        xFraction = 1 - xFraction;
                    }
                }
                // TODO 计算剩下两个维度上的权重
            }
        }
    }

    /**
     * 计算与x相邻的两个bin的序号。如果binCenters[i] <= x < binCenters[i+1]，将返回{i,i+1}。
     * 如果x < binCenters[0]，或x >= binCenters[n-1]，将返回{n-1,0}。
     * 函数不会对binCenters做检查，调用者需要保证binCenters中的元素是严格升序的。
     *
     * @param x
     * @param binCenters 各个bin中心的值。
     * @return 与x相邻的两个bin的序号
     */
    private int[] getAdjacentBinIds(float x, FloatMatrix binCenters) {
        if (x < binCenters.get(0) || x >= binCenters.get(binCenters.length - 1))
            return new int[]{binCenters.length - 1, 0};
        for (int i = 0; i < binCenters.length - 1; i++) {
            if (binCenters.get(i) <= x && binCenters.get(i + 1) > x) return new int[]{i, i + 1};
        }
        throw new RuntimeException("Unable to identify adjacent bins.");
    }
}
