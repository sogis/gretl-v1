package ch.so.agi.gretl.util;

import org.gradle.api.GradleException;

/**
 * Utility Class used in the Tasks to Convert a thrown Exception
 * into a GradleException to halt the Execution of the gradle build.
 *
 * GretlException intances are converted, all other Exceptions are
 * wrapped (nested)
 */
public class ExConverter {
    public static GradleException toGradleException(Exception ex){
        GradleException res = null;

        String exClassName = ex.getClass().toString();
        String gretlClassName = GretlException.class.toString();

        if(exClassName.equals(gretlClassName)){ //can't use instanceof as must return false for GretlException subclasses.
            res = new GradleException(ex.getMessage());
        }
        else {
            res = new GradleException("Inner Exception Message: " + ex.getMessage(), ex);
        }
        return res;
    }
}
