package ch.so.agi.gretl.util;

/**
 * Created by bjsvwsch on 18.05.17.
 */
public class NotAllowedSqlExpressionException extends GretlException {

    public NotAllowedSqlExpressionException() {
    }

    public NotAllowedSqlExpressionException(String message) {
        super(message);
    }

    public NotAllowedSqlExpressionException(String message, Throwable cause) {
        super(message, cause);
    }

}
