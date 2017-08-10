package ch.so.agi.gretl.util;

public class EmptyListException extends GretlException {
    public EmptyListException() {
    }

    public EmptyListException(String message) {
        super(message);
    }

    public EmptyListException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmptyListException(Throwable cause) {
        super(cause);
    }

    public EmptyListException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
