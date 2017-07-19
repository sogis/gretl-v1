package ch.so.agi.gretl.logging;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.logging.Logger;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;


/**
 * Test-Class for OldLogger-Class
 */
public class LoggerTest {

    private GretlLogger log;

    public LoggerTest() {
        LogEnvironment.initStandalone(Level.DEBUG);
        this.log = LogEnvironment.getLogger(this.getClass());
    }


    /*Not working with ./gradlew test : The question is where will the Infolog be written? It seems not on System.err*/
    @Test
    public void logInfoTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        //  Save the old System.err
        PrintStream oldSystem = System.err;
        // Tell Java to use your special stream
        System.setErr(ps);

        log.info("Info-OldLogger-Test");

        // Put things back
        System.err.flush();
        System.setErr(oldSystem);


        String LogMessage = baos.toString();
        String[] ArrayLogMessage = LogMessage.split("\\\n");


        if (ArrayLogMessage[1].equals("INFO: Info-OldLogger-Test")) {

        } else {
            assertFalse("OldLogger is not working properly: " + baos.toString(), true);
        }
    }

    @Test
    public void testOutputsLogSource() throws Exception
    {
        Logger jlog = ((CoreJavaLogAdaptor)log).getInnerLogger();
        assertEquals("The logSource must be equal to the name of this test class", jlog.getName(), this.getClass().getName());
    }

    @Test
    public void logErrorTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        //  Save the old System.err
        PrintStream oldSystem = System.err;
        // Tell Java to use your special stream
        System.setErr(ps);

        log.error("Error-OldLogger-Test");

        // Put things back
        System.err.flush();
        System.setErr(oldSystem);


        String LogMessage = baos.toString();
        String[] ArrayLogMessage = LogMessage.split("\\\n");


        if (ArrayLogMessage[1].equals("SEVERE: Error-OldLogger-Test")) {

        } else {
            assertFalse("OldLogger is not working properly: " + baos.toString(), true);
        }

    }

    /* actually not working, but should somehow be tested
    @Test
    public void debugInfoTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        //  Save the old System.err
        PrintStream oldSystem = System.err;
        // Tell Java to use your special stream
        System.setErr(ps);

        log.debug("Debug-OldLogger-Test");

        // Put things back
        System.err.flush();
        System.setErr(oldSystem);


        String LogMessage = baos.toString();
        String[] ArrayLogMessage = LogMessage.split("\\\n");


        if (ArrayLogMessage[0].equals("DEBUG: Debug-OldLogger-Test")) {

        } else {
            assertFalse("OldLogger is not working properly: " + baos.toString(), true);
        }

    }
    */


}

