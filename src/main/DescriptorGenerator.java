package main;

import flib.MathX;
import org.jblas.FloatMatrix;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.opencv.core.CvType.*;

/**
 * 生成特征描述子（论文第6章）
 */
public class DescriptorGenerator {

    private static final int D = 4;                         // 一条轴上的子区域数量
    private static final int N_BIN = 8;                     // 每张直方图的堆栈数量
    private static final float DESCRIPTOR_MAX_VAL = 0.2f;   // 描述子中元素允许的最大值

    public ArrayList<KeyPointX> keyPointsWithDescriptor;

    public ArrayList<FloatMatrix> run(ArrayList<KeyPoint> keyPoints, ArrayList<Octave> octaves) throws InterruptedException, TimeoutException {
        return run(keyPoints, octaves, false); // TODO 测试通过后改为true
    }

    public ArrayList<FloatMatrix> run(ArrayList<KeyPoint> keyPoints, ArrayList<Octave> octaves, boolean parallel) throws InterruptedException, TimeoutException {
        System.out.print("Generating descriptors...");
        ArrayList<FloatMatrix> descriptors = new ArrayList<>(keyPoints.size());
        keyPointsWithDescriptor = new ArrayList<>(keyPoints.size());

        int nCore = Runtime.getRuntime().availableProcessors();
        if (keyPoints.size() < nCore) parallel = false;

        if (!parallel) {
            for (KeyPoint keyPoint : keyPoints) {
                FloatMatrix descriptor = generate(keyPoint, octaves);
                descriptors.add(descriptor);
                keyPointsWithDescriptor.add(new KeyPointX(keyPoint, descriptor));
            }
        } else {
            // 任务划分
            int //TODO
            ExecutorService executorService = Executors.newCachedThreadPool();

            executorService.shutdown();
            long timeout = 3600;
            if (!executorService.awaitTermination(timeout, TimeUnit.SECONDS)) {
                throw new TimeoutException("Parallel pixel operations failed to finish within " + timeout + " seconds.");
            }
        }

        System.out.println("DONE");
        return descriptors;
    }

    @SuppressWarnings("DuplicatedCode")
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

        Mat tensor = Mat.zeros(new int[]{D + 2, D + 2, N_BIN}, CV_32F); // 由于是在5×5的网格内采样的，因此会有6×6个网格顶点，边缘的2行2列会被舍弃

        // 采样
        for (int i = -radius; i <= radius; i++) {
            int x = Math.round((float) keyPoint.pt.x + i); // 像素坐标
            if (x < 1 || x >= width - 1) continue; // 计算梯度需要左右两个像素的值，因此边界检查需要留出1的边距
            for (int j = -radius; j <= radius; j++) {
                int y = Math.round((float) keyPoint.pt.y + j);
                if (y < 1 || y >= height - 1) continue;

                // 局部采样坐标转换到与朝向相关的局部坐标系
                float yLocal = j * (float) Math.sin(keyPointRad) + i * (float) Math.cos(keyPointRad);
                float xLocal = j * (float) Math.cos(keyPointRad) - i * (float) Math.sin(keyPointRad);
                float yNorm = yLocal / subregionWidth, xNorm = xLocal / subregionWidth; // 归一化并约束在(-(D+1)/2,(D+1)/2)范围，越界的点跳过
                if (yNorm <= -(D + 1) / 2.0f || yNorm >= (D + 1) / 2.0f || xNorm <= -(D + 1) / 2.0f || xNorm >= (D + 1) / 2.0f)
                    continue;

                // 计算梯度、权重和朝向
                FloatMatrix gradient = Util.computeGradient(x, y, gaussianImage.image);
                float magnitude = gradient.norm2();
                float weight = (float) MathX.gauss(Math.sqrt(yNorm * yNorm + xNorm * xNorm), 0.5 * D);
                float weightedMagnitude = weight * magnitude;
                float orientation = (float) (Math.atan2(gradient.get(1), gradient.get(0)) + Math.PI);
                float orientationLocal = (orientation - keyPointRad + 2 * (float) Math.PI) % (2 * (float) Math.PI); // [0,2Pi)

                // 计算相邻的bin序号
                float xBin = xNorm + D / 2.0f + 0.5f, yBin = yNorm + D / 2.0f + 0.5f; // (0,D+1)
                int xBinLeft = (int) Math.floor(xBin), yBinLeft = (int) Math.floor(yBin); // [0,D]
                int xBinRight = xBinLeft + 1, yBinRight = yBinLeft + 1;
                float orientationBin = orientationLocal / orientationBinWidth; // [0,N_BIN)
                int orientationBinLeft = (int) Math.floor(orientationBin), // [0,N_BIN-1]
                        orientationBinRight = (orientationBinLeft + 1) % N_BIN; // [1,N_BIN-1] ∪ {0}
                if (xBinLeft < 0 || xBinLeft > D || yBinLeft < 0 || yBinLeft > D || orientationBinLeft < 0 || orientationBinLeft > N_BIN - 1)
                    continue; // 越界检查

                // 计算在x，y，角度维度，在左侧bin上的权重
                float xFraction, yFraction, orientationFraction;
                xFraction = xBinRight - xBin; // 权重与另一侧的距离正相关
                yFraction = yBinRight - yBin;
                if (orientationBinRight < orientationBin) {
                    orientationFraction = orientationBin - orientationBinLeft;
                    orientationFraction = 1 - orientationFraction;
                } else {
                    orientationFraction = orientationBinRight - orientationBin;
                }

                // 逆向三线性插值
                // 记号参考 https://en.wikipedia.org/wiki/Trilinear_interpolation
                float c0 = xFraction * weightedMagnitude;
                float c1 = (1 - xFraction) * weightedMagnitude;
                float c00 = c0 * yFraction;
                float c01 = c0 * (1 - yFraction);
                float c10 = c1 * yFraction;
                float c11 = c1 * (1 - yFraction);
                float c000 = c00 * orientationFraction;
                float c001 = c00 * (1 - orientationFraction);
                float c010 = c01 * orientationFraction;
                float c011 = c01 * (1 - orientationFraction);
                float c100 = c10 * orientationFraction;
                float c101 = c10 * (1 - orientationFraction);
                float c110 = c11 * orientationFraction;
                float c111 = c11 * (1 - orientationFraction);

                // 更新张量
                /*System.out.printf("xBinLeft: %d, yBinLeft: %d, orientationBinLeft: %d, tensor size: {%d, %d, %d}\n", xBinLeft, yBinLeft, orientationBinLeft,
                        D + 2, D + 2, N_BIN);*/
                Util.autoIncrement(tensor, yBinLeft, xBinLeft, orientationBinLeft, c000);
                Util.autoIncrement(tensor, yBinLeft, xBinLeft, orientationBinRight, c001);
                Util.autoIncrement(tensor, yBinLeft, xBinRight, orientationBinLeft, c100); // x轴对应cxxx的第1维
                Util.autoIncrement(tensor, yBinLeft, xBinRight, orientationBinRight, c101);
                Util.autoIncrement(tensor, yBinRight, xBinLeft, orientationBinLeft, c010);
                Util.autoIncrement(tensor, yBinRight, xBinLeft, orientationBinRight, c011);
                Util.autoIncrement(tensor, yBinRight, xBinRight, orientationBinLeft, c110);
                Util.autoIncrement(tensor, yBinRight, xBinRight, orientationBinRight, c111);
            }
        }

        FloatMatrix descriptor = new FloatMatrix(0);
        for (int tRow = 1; tRow <= D; tRow++) { // 边缘2行2列舍弃
            for (int tCol = 1; tCol <= D; tCol++) {
                ArrayList<Double> histArray = Util.flatten(tensor, tRow, tCol);
                FloatMatrix histJblas = new FloatMatrix(histArray.size());
                for (int i = 0; i < histArray.size(); i++) {
                    Double elem = histArray.get(i);
                    float flt = Float.parseFloat(String.valueOf(elem));
                    histJblas.put(i, flt);
                }
                descriptor = FloatMatrix.concatVertically(descriptor, histJblas);
            }
        }

        descriptor = postProcess(descriptor);

        return descriptor;
    }

    /**
     * 对描述子进行后处理：
     * 1. 将传入的描述子归一化到单位长度；
     * 2. 对任何超过允许的最大值（[Lowe 04]中规定为0.2）的元素，截断到允许的最大值；
     * 3. 对向量重新进行归一化。
     *
     * @param descriptor 描述子
     * @return 后处理后的描述子
     */
    private FloatMatrix postProcess(FloatMatrix descriptor) {
        FloatMatrix normalized = Util.normalize(descriptor);
        for (int i = 0; i < descriptor.length; i++) {
            float val = descriptor.get(i);
            if (val > DESCRIPTOR_MAX_VAL) val = DESCRIPTOR_MAX_VAL;
            normalized.put(i, val);
        }
        normalized = Util.normalize(normalized);
        return normalized;
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
