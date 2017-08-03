package ch.so.agi.gretl.util;

/**
 * Baseclass for all Exceptions thrown in the Steps code.
 *
 * The Tasks convert pure GretlExceptions to GradleExceptions
 * to avoid wrapping the GretlException in the GradleException,
 * aiming at less confusing Exception nesting.
 */
public class GretlException extends RuntimeException {

    public GretlException(){}

    public GretlException(String message) {
        super(message);
    }

    public GretlException(String message, Throwable cause) {
        super(message, cause);
    }

    public GretlException(Throwable cause) {
        super(cause);
    }

    public GretlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
