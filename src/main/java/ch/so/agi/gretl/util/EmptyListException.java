package ch.so.agi.gretl.util;

/**
 * Created by bjsvwsch on 18.07.17.
 */
public class EmptyListException extends RuntimeException {
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
