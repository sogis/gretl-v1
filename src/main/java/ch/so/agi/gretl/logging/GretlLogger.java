package ch.so.agi.gretl.logging;

/**
 * Created by bjsvwjek on 12.07.17.
 */
public interface GretlLogger {

    public void info(String msg);

    public void debug(String msg);

    public void error(String msg);
}
