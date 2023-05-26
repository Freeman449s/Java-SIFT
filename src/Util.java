import org.opencv.core.Size;

import java.util.function.Function;

public class Util {
    static double min(Size size) {
        return Math.min(size.height, size.width);
    }

    static void foreachPixelDo(int borderX, int borderY, Function<Void, Void> func) {
        // TODO
    }

    static void foreachPixelParallelDo(int borderX, int borderY, Function<Void, Void> func) {
        // TODO
    }
}
