package ch.so.agi.gretl.util;

/**
 * Created by bjsvwsch on 18.07.17.
 */
public class EmptyListException extends GretlException {
    public EmptyListException() {
    }

    public EmptyListException(String message) {
        super(message);
    }

    public EmptyListException(String message, Throwable cause) {
        super(message, cause);
    }

}
