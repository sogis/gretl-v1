package ch.so.agi.gretl.logging;

/**
 * Holds the global logging factory used by the Step
 * and helper classes to get a logger instance.
 * Holds either a logging factory for standalone use
 * of the steps classes as in unit tests or the gradle *
 * logging environment for integrated use of the steps
 * in gradle.
 */
public class LogEnvironment {

    private static LogFactory currentLogFactory;

    public static void initGradleIntegrated() {
        if(currentLogFactory == null || currentLogFactory instanceof CoreJavaLogFactory)
            currentLogFactory = new GradleLogFactory();
    }

    public static void initStandalone(){
        initStandalone(Level.DEBUG);
    }

    public static void initStandalone(Level logLevel) {
        if(currentLogFactory == null || currentLogFactory instanceof GradleLogFactory)
            currentLogFactory = new CoreJavaLogFactory(logLevel);
    }

    public static GretlLogger getLogger(Class logSource) {
        //System.out.println("Class in getLogger = "+logSource.getName());
        if(currentLogFactory == null)
            throw new IllegalArgumentException("The LogEnvironment must be initialized with one of the init* methods before calling getLogger");

        if(logSource == null)
            throw new IllegalArgumentException("The logSource must not be null");

        return currentLogFactory.getLogger(logSource);
    }
}

