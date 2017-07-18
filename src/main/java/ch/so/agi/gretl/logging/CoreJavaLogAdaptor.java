package ch.so.agi.gretl.logging;

import java.util.logging.*;

/**
 * Created by bjsvwjek on 12.07.17.
 */
public class CoreJavaLogAdaptor implements GretlLogger {

    private java.util.logging.Logger logger;

    CoreJavaLogAdaptor(Class logSource, Level logLevel){
        this.logger = java.util.logging.Logger.getLogger(logSource.getName());
        this.logger.setLevel(logLevel);
    }

    public void info(String msg){
        logger.info(msg);
    }

    public void debug(String msg){
        logger.fine(msg);
    }

    public void error(String msg){
        logger.severe(msg);
    }

    Logger getInnerLogger(){
        return logger;
    }
}
