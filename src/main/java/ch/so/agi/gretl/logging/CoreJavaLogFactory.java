package ch.so.agi.gretl.logging;

import java.util.logging.Level;

/**
 * Created by bjsvwjek on 12.07.17.
 */
public class CoreJavaLogFactory implements LogFactory {

    private Level globalLogLevel;

    CoreJavaLogFactory(Level globalLogLevel){
        this.globalLogLevel = globalLogLevel;
    }

    public GretlLogger getLogger(Class logSource){
        return new CoreJavaLogAdaptor(logSource, globalLogLevel);
    }
}