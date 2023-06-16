package flib;

enum LogColor {
    /**
     * enum具有以下特性：
     * 1. 开头定义的每个成员实际上都是LogColor类对象，隐式地调用了LogColor()初始化
     * 2. 构造函数默认被private修饰，每个成员默认被public static final修饰
     */

    _white(29), // white的取值是推断的，需要谨慎使用
    black(30),
    red(31),
    greed(32),
    yellow(33),
    blue(34),
    purple(35),
    cyan(36),
    gray(37);


    private final int state;

    LogColor(int value) {
        state = value;
    }

    public int getState() {
        return state;
    }
}

enum LogType {
    normal(0),
    bold(1),
    thin(2),
    italic(3),
    underline(4),
    slowFlicker(5),
    fastFlicker(6);

    private final int state;

    LogType(int value) {
        state = value;
    }

    public int getState() {
        return state;
    }
}

public class Log {
    /**
     * 在控制台打印具有指定文字颜色的文本。
     * 注意：在Windows控制台下可能不生效。
     *
     * @param content 需要打印的内容
     * @param color   文字颜色
     */
    public static void println(String content, LogColor color) {
        String styleStr = String.format("\033[%dm%s\033[0m", color.getState(), content);
        System.out.println(styleStr);
    }

    /**
     * 在控制台打印具有指定文字颜色和类型的文本。
     * 注意：在Windows控制台下可能不生效。
     *
     * @param content 需要打印的内容
     * @param color   文字颜色
     * @param type    文字类型
     */
    public static void println(String content, LogColor color, LogType type) {
        String styleStr = String.format("\033[%d;%dm%s\033[0m", color.getState(), type.getState(), content);
        System.out.println(styleStr);
    }

    /**
     * 在控制台打印具有指定文字类型的文本。
     * 注意：在Windows控制台下可能不生效。
     * 注意：此方法使用了LogColor._white值，可能存在问题。
     *
     * @param content 需要打印的内容
     * @param type    文字类型
     */
    public static void println(String content, LogType type) {
        println(content, LogColor._white, type);
    }

    /**
     * 在控制台打印黄色的警告信息。
     *
     * @param content 需要打印的内容
     */
    public static void warning(String content) {
        println(content, LogColor.yellow);
    }

    /**
     * 在控制台打印红色的错误信息。
     *
     * @param content 需要打印的内容
     */
    public static void error(String content) {
        println(content, LogColor.red);
    }
}