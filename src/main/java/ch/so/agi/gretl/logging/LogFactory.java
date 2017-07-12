package ch.so.agi.gretl.logging;

/**
 * Returns a Logger instance
 * Created by bjsvwjek on 12.07.17.
 */
public interface LogFactory {
    public GretlLogger getLogger(Class logSource);
}