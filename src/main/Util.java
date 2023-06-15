package main;

import flib.MathX;
import org.jblas.FloatMatrix;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class Util {
    public static float EPS = 1e-6f;

    public static double min(Size size) {
        return Math.min(size.height, size.width);
    }

    /**
     * 取2D矩阵中的最小值
     *
     * @param mat2D 2D矩阵
     * @return mat2D中的最小值
     */
    public static double min(Mat mat2D) {
        double min = Double.MAX_VALUE;
        for (int x = 0; x < mat2D.width(); x++) {
            for (int y = 0; y < mat2D.height(); y++) {
                if (mat2D.get(y, x)[0] < min) min = mat2D.get(y, x)[0];
            }
        }
        return min;
    }

    /**
     * 取2D矩阵中的最大值
     *
     * @param mat2D 2D矩阵
     * @return mat2D中的最大值
     */
    public static double max(Mat mat2D) {
        double max = Double.MIN_VALUE;
        for (int x = 0; x < mat2D.width(); x++) {
            for (int y = 0; y < mat2D.height(); y++) {
                if (mat2D.get(y, x)[0] > max) max = mat2D.get(y, x)[0];
            }
        }
        return max;
    }

    public static void foreachPixelDo(int width, int height, int borderX, int borderY, PixelOperation operation) {
        for (int x = borderX; x < width - borderX; x++) {
            for (int y = borderY; y < height - borderY; y++) {
                operation.func(x, y);
            }
        }
    }

    public static void foreachPixelDo(int width, int height, PixelOperation operation) {
        foreachPixelDo(width, height, 0, 0, operation);
    }

    /**
     * 并行地对每个像素进行重复的操作。此方法并不会做任何并发控制；如果存在潜在的读写冲突问题，需要调用者进行适当的处理。
     *
     * @param width     图像宽度
     * @param height    图像高度
     * @param borderX   横轴上留出的边缘，函数不会对边缘像素进行操作
     * @param borderY   纵轴上留出的边缘，函数不会对边缘像素进行操作
     * @param operation 需要在每个像素上进行的操作
     * @throws InterruptedException 如果线程池在等待线程运行完毕时被中断，将抛出此异常
     * @throws TimeoutException     如果线程池未能在规定时间（1小时）内完成任务，将抛出此异常
     */
    public static void foreachPixelParallelDo(int width, int height, int borderX, int borderY, PixelOperation operation) throws InterruptedException, TimeoutException {
        int nCore = Runtime.getRuntime().availableProcessors();
        int nSideBlock = (int) Math.floor(Math.sqrt(nCore)) + 1; // 每边上可以切分成几段，创建略多于核心数的分块
        if (width - 2 * borderX < nSideBlock || height - 2 * borderY < nSideBlock)
            foreachPixelDo(width, height, borderX, borderY, operation);

        // 块切分
        int xBlockLen = (width - 2 * borderX) / nSideBlock, yBlockLen = (height - 2 * borderY) / nSideBlock;
        ArrayList<Integer> xBoundaries = new ArrayList<>(), yBoundaries = new ArrayList<>(); // 每一块的坐标范围，左闭右开
        int xBoundary = borderX, yBoundary = borderY;
        for (int i = 0; i < nSideBlock; i++) {
            xBoundaries.add(xBoundary);
            yBoundaries.add(yBoundary);
            xBoundary += xBlockLen;
            yBoundary += yBlockLen;
        }
        xBoundaries.add(width - borderX);
        yBoundaries.add(height - borderY);

        // 并行化
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 1; i <= nSideBlock; i++) {
            for (int j = 1; j <= nSideBlock; j++) {
                int xStart = xBoundaries.get(i - 1), xEnd = xBoundaries.get(i);
                int yStart = yBoundaries.get(j - 1), yEnd = yBoundaries.get(j);
                executorService.execute(() -> {
                    for (int x = xStart; x < xEnd; x++) {
                        for (int y = yStart; y < yEnd; y++) {
                            operation.func(x, y);
                        }
                    }
                });
            }
        }
        executorService.shutdown();
        long timeout = 3600;
        if (!executorService.awaitTermination(timeout, TimeUnit.SECONDS)) {
            throw new TimeoutException("Parallel pixel operations failed to finish within " + timeout + " seconds.");
        }
    }

    /**
     * 并行地对每个像素进行重复的操作。此方法并不会做任何并发控制；如果存在潜在的读写冲突问题，需要调用者进行适当的处理。
     *
     * @param width     图像宽度
     * @param height    图像高度
     * @param operation 需要在每个像素上进行的操作
     */
    public static void foreachPixelParallelDo(int width, int height, PixelOperation operation) throws InterruptedException, TimeoutException {
        foreachPixelParallelDo(width, height, 0, 0, operation);
    }

    public static KeyPoint keyPointDeepCopy(KeyPoint keyPoint) {
        return new KeyPoint((float) keyPoint.pt.x, (float) keyPoint.pt.y, keyPoint.size, keyPoint.angle,
                keyPoint.response, keyPoint.octave, keyPoint.class_id);
    }

    /**
     * 局部尺度向全局尺度的转换。globalScale = localScale * 2^(octaveNo).
     *
     * @param localScale 局部尺度
     * @param octaveNo   octave序号，从0开始
     * @return 全局尺度
     */
    public static float local2GlobalScale(float localScale, int octaveNo) {
        return localScale * (float) Math.pow(2, octaveNo);
    }

    /**
     * 全局尺度向局部尺度的转换。localScale = globalScale / 2^(octaveNo).
     *
     * @param globalScale 全局尺度
     * @param octaveNo    octave序号，从0开始
     * @return 本octave内的尺度
     */
    public static float global2LocalScale(float globalScale, int octaveNo) {
        return globalScale / (float) Math.pow(2, octaveNo);
    }

    /**
     * 取关键点在所在octave内的局部尺度
     *
     * @param keyPoint 关键点
     * @return keyPoint在所在octave内的局部尺度
     */
    public static float getLocalScale(KeyPoint keyPoint) {
        return global2LocalScale(keyPoint.size, keyPoint.octave);
    }

    /**
     * 获取与关键点最接近的高斯图像的局部序号。此方法同样可以用于获取DoG图像的序号，但是需要注意，一个octave中DoG图像的数量会比高斯图像少1.
     *
     * @param keyPoint 关键点
     * @return 与关键点最接近的高斯图像的局部序号
     */
    public static int getLocalGaussianImageId(KeyPoint keyPoint) {
        return (int) Math.round(MathX.log2(
                keyPoint.size / GlobalParam.SIGMA / Math.pow(2, keyPoint.octave))
                * GlobalParam.S); // 本octave中的图像Id
    }

    /**
     * 使用有限微分计算(x,y)位置梯度的O(h^2)阶近似值。f'(x) = (f(x + 1) - f(x - 1)) / 2
     *
     * @param x     计算梯度的点的横坐标（列指标）
     * @param y     计算梯度的点的纵坐标（行指标）
     * @param image 计算梯度的图像
     * @return (x, y)位置梯度的近似值
     */
    public static FloatMatrix computeGradient(int x, int y, Mat image) {
        float dy = (float) (image.get(y + 1, x)[0] - image.get(y - 1, x)[0]) / 2;
        float dx = (float) (image.get(y, x + 1)[0] - image.get(y, x - 1)[0]) / 2;
        return new FloatMatrix(new float[]{dx, dy});
    }

    /**
     * 对三维张量的指定元素进行自增操作。
     *
     * @param mat3d 三维张量
     * @param row   行指标
     * @param col   列指标
     * @param z     深度指标
     * @param delta 增量
     */
    public static void autoIncrement(Mat mat3d, int row, int col, int z, double delta) {
        double originVal = mat3d.get(new int[]{row, col, z})[0];
        mat3d.put(new int[]{row, col, z}, originVal + delta);
    }

    /**
     * 将double类型数组转为float类型数组（可能导致精度损失）
     *
     * @param dblArray double类型数组
     * @return 由dblArray转换而来的float类型数组
     */
    public static float[] toFloatArray(double[] dblArray) {
        float[] fltArray = new float[dblArray.length];
        for (int i = 0; i < dblArray.length; i++)
            fltArray[i] = (float) dblArray[i];
        return fltArray;
    }

    /**
     * 将矩阵归一化。归一化后，矩阵的Frobenius范数为1. 该方法同样可用于向量的归一化。归一化后，向量的第二范数为1.
     *
     * @param matrix 矩阵
     * @return 归一化后的矩阵
     */
    public static FloatMatrix normalize(FloatMatrix matrix) {
        double norm = Math.max(matrix.norm2(), EPS);
        FloatMatrix ret = new FloatMatrix(matrix.rows, matrix.columns);
        for (int i = 0; i < matrix.rows; i++) {
            for (int j = 0; j < matrix.columns; j++) {
                ret.put(i, j, (float) (matrix.get(i, j) / norm));
            }
        }
        return ret;
    }

    /**
     * 将张量中idx位置的子张量展平为向量
     *
     * @param mat 矩阵
     * @param idx 需要展平的位置
     * @return 张量mat的idx处的子张量展平得到的向量
     */
    public static ArrayList<Double> flatten(Mat mat, int... idx) {
        if (idx.length == mat.dims()) { // 递归终点
            return DoubleStream.of(mat.get(idx)).boxed().collect(Collectors.toCollection(ArrayList::new));
        }

        int[] newIdx = new int[idx.length + 1];
        System.arraycopy(idx, 0, newIdx, 0, idx.length);
        ArrayList<Double> ret = new ArrayList<>();
        for (int i = 0; i < mat.size(idx.length); i++) {
            newIdx[newIdx.length - 1] = i;
            ret.addAll(flatten(mat, newIdx));
        }
        return ret;
    }
}

