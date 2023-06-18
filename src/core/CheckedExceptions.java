package core;

/**
 * 功能等同于IndexOutOfBoundsException，为提升程序强健性，改为了需要检查的版本
 */
class IndexOutOfBoundsExceptionC extends Exception {

    public IndexOutOfBoundsExceptionC() {
        super();
    }

    public IndexOutOfBoundsExceptionC(String message) {
        super(message);
    }
}
