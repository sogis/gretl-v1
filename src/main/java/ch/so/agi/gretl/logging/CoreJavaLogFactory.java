package ch.so.agi.gretl.logging;

public class CoreJavaLogFactory implements LogFactory {

    private Level globalLogLevel;

    CoreJavaLogFactory(Level globalLogLevel){
        this.globalLogLevel = globalLogLevel;
    }

    public GretlLogger getLogger(Class logSource){
        //System.out.println("logSource in CoreJavaLogFactory = "+logSource.getName());
        return new CoreJavaLogAdaptor(logSource, globalLogLevel);
    }
}