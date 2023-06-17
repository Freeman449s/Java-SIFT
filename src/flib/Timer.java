package flib;

public class Timer {
    private Long startTime = null;

    public Timer(boolean startImmediately) {
        if (startImmediately) startTime = System.currentTimeMillis();
    }

    /**
     * 构造Clock对象，并立即开始计时。
     */
    public Timer() {
        this(true);
    }

    /**
     * 开始计时。
     *
     * @param restart 设置为true时，如果此Timer对象已经在计时，将从调用时刻开始重新计时。
     * @return Timer对象开始计时的时间（自UTC 1970/1/1 00:00开始计算的时间，以毫秒为单位）。
     */
    public long start(boolean restart) {
        if (startTime != null) {
            if (restart) startTime = System.currentTimeMillis();
        }
        else
            startTime = System.currentTimeMillis();
        return startTime;
    }

    /**
     * 开始计时。如果此Timer对象已经在计时，将从调用时刻开始重新计时。
     *
     * @return Timer对象开始计时的时间（自UTC 1970/1/1 00:00开始计算的时间，以毫秒为单位）。
     */
    public long start() {
        return start(true);
    }

    /**
     * 终止计时。
     *
     * @return 计时器记录的时间。如果在没有计时的情况下调用此方法，将返回-1.
     */
    public long end() {
        if (startTime == null) {
            Log.warning("Ending a timer while it's not ticking.");
            return -1;
        }
        long duration = System.currentTimeMillis() - startTime;
        startTime = null;
        return duration;
    }

    /**
     * 终止计时并打印时间。
     *
     * @param content 额外的提示信息
     * @return 计时器记录的时间。如果在没有计时的情况下调用此方法，将返回-1.
     */
    public long endAndPrint(String content) {
        long durationMillis = end();
        System.out.println(content + ": " + durationMillis / 1000.0 + "s.");
        return durationMillis;
    }
}
