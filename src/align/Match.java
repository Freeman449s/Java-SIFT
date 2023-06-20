package align;

import core.KeyPointX;

public class Match implements Comparable<Match> {
    public static final double BEST_MATCH_RATIO = 0.45; // 当一个关键点与最近点和次近点的距离的比值为该值时，配对正确的概率最大（[Lowe 04]）
    public static final double GOOD_THRESHOLD = 0.8;    // 当一个关键点与最近点和次近点的距离的比值高于该值时，该配对有较大的概率不是正确的配对（[Lowe 04]）

    public KeyPointX queryPt, trainPt1, trainPt2;
    public int queryIdx, trainIdx1, trainIdx2;

    public Match(KeyPointX queryPt, KeyPointX trainPt1, KeyPointX trainPt2, int queryIdx, int trainIdx1, int trainIdx2) {
        this.queryPt = queryPt.deepCopy();
        this.trainPt1 = trainPt1.deepCopy();
        this.trainPt2 = trainPt2.deepCopy();
        this.queryIdx = queryIdx;
        this.trainIdx1 = trainIdx1;
        this.trainIdx2 = trainIdx2;
    }

    public double getDist1() {
        return KeyPointX.featureEuclidDist(queryPt, trainPt1);
    }

    public double getDist2() {
        return KeyPointX.featureEuclidDist(queryPt, trainPt2);
    }

    public double getRatio() {
        double denominator = Math.max(getDist2(), 1e-8);
        return getDist1() / denominator;
    }

    public boolean isGood() {
        return getRatio() <= GOOD_THRESHOLD;
    }

    /**
     * 根据[Lowe 04]，查询点与最近点和次近点的距离比值接近一个定值（大约0.45）时，匹配正确的概率最高。该方法依据论文中的发现对匹配的优劣进行排序。
     *
     * @param o the object to be compared.
     * @return 如果此对象依据[Lowe 04]中的发现优于o，返回-1；劣于o，返回1；与o同等优劣，返回0.
     */
    @Override
    public int compareTo(Match o) {
        return Double.compare(Math.abs(getRatio() - BEST_MATCH_RATIO),
                Math.abs(o.getRatio() - BEST_MATCH_RATIO));
    }
}
