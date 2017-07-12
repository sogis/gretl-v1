package ch.so.agi.gretl.logging;

import java.util.logging.Level;

/**
 * Holds the global logging factory used by the Step
 * and helper classes to get a logger instance.
 * Holds either a logging factory for standalone use
 * of the steps classes as in unit tests or the gradle
 * logging environment for integrated use of the steps
 * in gradle.
 *
 * Created by bjsvwjek on 12.07.17.
 */
public class LogEnvironment {
    private static LogFactory currentLogFactory;

    public static void initGradleIntegrated() {
        if(currentLogFactory == null || currentLogFactory instanceof CoreJavaLogFactory)
            currentLogFactory = new GradleLogFactory();
    }

    public static void initStandalone(){
        initStandalone(Level.ALL);
    }

    public static void initStandalone(Level logLevel) {
        if(currentLogFactory == null || currentLogFactory instanceof GradleLogFactory)
            currentLogFactory = new CoreJavaLogFactory(logLevel);
    }

    public static GretlLogger getLogger(Class logSource) {

        if(currentLogFactory == null)
            throw new IllegalArgumentException("The LogEnvironment must be initialized with one of the init* methods before calling getLogger");

        return currentLogFactory.getLogger(logSource);
    }
}

