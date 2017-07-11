package ch.so.agi.gretl.util;

/**
 * Created by bjsvwsch on 18.05.17.
 */
public class NotAllowedSqlExpressionException extends Exception {
    public NotAllowedSqlExpressionException() {
    }

    public NotAllowedSqlExpressionException(String message) {
        super(message);
    }

    public NotAllowedSqlExpressionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotAllowedSqlExpressionException(Throwable cause) {
        super(cause);
    }

    public NotAllowedSqlExpressionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
