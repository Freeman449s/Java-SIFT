package align;

import org.jblas.DoubleMatrix;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Range;

import java.util.ArrayList;

import static org.opencv.core.Core.SVDecomp;
import static org.opencv.core.Core.transpose;
import static org.opencv.core.CvType.*;

public class HomographyEstimator {
    /**
     * 使用简单方法估计单应矩阵。该方法利用全部点，使用解线性最小二乘法的方法估计单应矩阵。
     * FIXME 此方法返回的结果与CV有明显差异
     *
     * @param srcPoints 原始平面上的点
     * @param dstPoints 目标平面上的点
     * @return 原始平面到目标平面的透视变换矩阵
     */
    public static Mat runVanillaEstimation(ArrayList<Point> srcPoints, ArrayList<Point> dstPoints) {
        // 构建线性方程组
        Mat A = new Mat(srcPoints.size() * 2, 8, CV_64F);
        Mat b = new Mat(srcPoints.size() * 2, 1, CV_64F);
        for (int i = 0; i < srcPoints.size(); i++) {
            Point srcPt = srcPoints.get(i), dstPt = dstPoints.get(i);
            double x1 = srcPt.x, y1 = srcPt.y, x2 = dstPt.x, y2 = dstPt.y;
            A.put(i, 0, x1);
            A.put(i, 1, y1);
            A.put(i, 2, 1);
            A.put(i, 6, -x1 * x2);
            A.put(i, 7, -y1 * x2);
            A.put(i + 1, 3, x1);
            A.put(i + 1, 4, y1);
            A.put(i + 1, 5, 1);
            A.put(i + 1, 6, -x1 * y2);
            A.put(i + 1, 7, -y1 * y2);

            b.put(i, 0, x2);
            b.put(i + 1, 0, y2);
        }

        // 解线性方程组
        Mat V = new Mat(), W = new Mat(), U = new Mat();
        SVDecomp(A, W, U, V);
        transpose(V, V);
        Mat sigma = Mat.diag(W);
        Mat Un = new Mat(U, new Range(0, U.height()), new Range(0, 8));
        Mat Un_trans = new Mat();
        transpose(Un, Un_trans);
        Mat h = V.matMul(sigma.inv()).matMul(Un_trans).matMul(b);

        // 组装单应矩阵
        Mat H = new Mat(3, 3, CV_64F);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == 2 && j == 2) {
                    H.put(2, 2, 1);
                    break;
                }
                H.put(i, j, h.get(i * 3 + j, 0)[0]);
            }
        }

        return H;
    }
}
