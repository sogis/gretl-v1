package ch.so.agi.gretl.logging;

import org.gradle.api.logging.*;

/**
 * Created by bjsvwjek on 12.07.17.
 */
public class GradleLogAdaptor implements GretlLogger {

    private org.gradle.api.logging.Logger logger;

    GradleLogAdaptor(Class logSource){
        this.logger = Logging.getLogger(logSource);
    }

    public void info(String msg){
        logger.info(msg);
    }

    public void debug(String msg){
        logger.debug(msg);
    }

    public void error(String msg){
        logger.error(msg);
    }
}
