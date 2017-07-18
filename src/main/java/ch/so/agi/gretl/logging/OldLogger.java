package ch.so.agi.gretl.logging;

import org.slf4j.LoggerFactory;

/**
 * OldLogger
 *
 */
public class OldLogger {

    private static final String INFO_NAME = "INFO: ";
    private static final String DEBUG_NAME = "DEBUG: ";
    public static final int INFO_LEVEL = 1;
    public static final int DEBUG_LEVEL = 2;

    private OldLogger() {}

    /**
     * Logs the given message string.
     *
     *
     * @param LogLevel  Loglevel used for logging (INFO_LEVEL / DEBUG_LEVEL)
     * @param message   message which will be logged
     */

    static public void log(int LogLevel, String message) {
        org.slf4j.Logger logger = LoggerFactory.getLogger(OldLogger.class);
        if (LogLevel== INFO_LEVEL) {
            /** INFO **/
            /**Write to a file**/
            logger.info(message);

        }
        else if (LogLevel== DEBUG_LEVEL) {
            /** DEBUG **/
            /**Write to another file**/
            logger.debug(message);

        }
        else {
            /**UNEXPECTED LOGLEVEL**/
            /**Throws Exception**/
            throw new RuntimeException("Failure with logging");
        }


    }


    /**
     *
     * Logs the given exception
     *
     * @param LogLevel  Loglevel used for logging (INFO_LEVEL / DEBUG_LEVEL)
     * @param e         exception which will be logged
     */
    static public void log(int LogLevel, Exception e) {
        org.slf4j.Logger logger = LoggerFactory.getLogger(OldLogger.class);
        if (LogLevel==INFO_LEVEL) {
            logger.info("", e);

        } else if (LogLevel==DEBUG_LEVEL) {
            logger.debug("", e);

        } else {
            throw new RuntimeException("Failure with logging");
        }
    }
}