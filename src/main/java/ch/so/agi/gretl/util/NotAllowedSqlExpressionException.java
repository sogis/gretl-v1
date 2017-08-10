package ch.so.agi.gretl.util;

public class NotAllowedSqlExpressionException extends GretlException {

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
