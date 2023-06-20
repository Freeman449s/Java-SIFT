package align;

/**
 * 元素过少异常。当集合中的元素过少，以至于无法进行某种操作时，抛出此异常。
 */
class TooFewElementsException extends RuntimeException {
    public TooFewElementsException() {
        super();
    }

    public TooFewElementsException(String message) {
        super(message);
    }
}
