package main;

import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Util {
    public static double min(Size size) {
        return Math.min(size.height, size.width);
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
                int yStart = yBoundaries.get(i - 1), yEnd = yBoundaries.get(i);
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
}

