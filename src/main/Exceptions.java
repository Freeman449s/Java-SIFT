package main;

class TooManyArgumentsException extends RuntimeException {
    public TooManyArgumentsException() {
        super();
    }

    public TooManyArgumentsException(String message) {
        super(message);
    }
}