package ch.so.agi.gretl.logging;

/**
 * Created by bjsvwjek on 12.07.17.
 */
public class GradleLogFactory implements LogFactory {

    public GretlLogger getLogger(Class logSource){
        return new GradleLogAdaptor(logSource);
    }

}
