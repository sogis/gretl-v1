package ch.so.agi.gretl.logging;

/**
 * Created by bjsvwsch on 12.07.17.
 */
public class LoggerAdapterJava implements LoggerInterface {

    private final static java.util.logging.Logger JavaLogger = java.util.logging.Logger.getLogger(LoggerAdapterJava.class.getName());

    @Override
    public void error(String Message) {

    }

    @Override
    public void info(String Message) {

    }

    @Override
    public void debug(String Message) {

    }
}
