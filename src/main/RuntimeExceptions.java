package main;

class TooManyArgumentsException extends RuntimeException {
    public TooManyArgumentsException() {
        super();
    }

    public TooManyArgumentsException(String message) {
        super(message);
    }
}

/**
 * 过早调用异常，表明在一些必要的方法被调用前，调用了此方法。例如，在所需的数据准备完成之前，调用了相应的getter。
 */
class EarlyCallException extends RuntimeException {
    public EarlyCallException() {
        super();
    }

    public EarlyCallException(String message) {
        super(message);
    }
}