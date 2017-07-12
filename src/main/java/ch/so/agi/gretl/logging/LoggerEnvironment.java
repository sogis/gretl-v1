package ch.so.agi.gretl.logging;

/**
 * Created by bjsvwsch on 12.07.17.
 */
public class LoggerEnvironment {

    private static LoggerEnvironment currentEnvironment;

    public static void initForStepsWithGradle() {
        currentEnvironment =
    }
    public static void initForStepsWithoutGradle() {

    }
    public static LoggerInterface getLogger(Class logSource) {

        return null;
    }
}
