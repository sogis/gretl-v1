package ch.so.agi.gretl.logging;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.logging.Logger;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;


/**
 * Test-Class for Logger-Class
 */
public class LoggerTest {

    private GretlLogger log;

    public LoggerTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());

    }

    @Test
    public void logInfoTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        //  Save the old System.err
        PrintStream oldSystem = System.err;
        // Tell Java to use your special stream
        System.setErr(ps);

        log.info("Info-Logger-Test");

        // Put things back
        System.err.flush();
        System.setErr(oldSystem);


        String LogMessage = baos.toString();
        String[] ArrayLogMessage = LogMessage.split(" -> ");


        if (!ArrayLogMessage[1].equals("Info-Logger-Test\n")) {
            assertFalse("Logger is not working properly: " + baos.toString(), true);
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

        log.error("Error-Logger-Test");

        // Put things back
        System.err.flush();
        System.setErr(oldSystem);


        String LogMessage = baos.toString();
        String[] ArrayLogMessage = LogMessage.split(" -> ");


        if (!ArrayLogMessage[1].equals("Error-Logger-Test\n")) {
            assertFalse("Logger is not working properly: " + baos.toString(), true);
        }

    }


    @Test
    public void logDebugTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        //  Save the old System.err
        PrintStream oldSystem = System.err;
        // Tell Java to use your special stream
        System.setErr(ps);

        log.debug("Debug-Logger-Test");

        // Put things back
        System.err.flush();
        System.setErr(oldSystem);


        String LogMessage = baos.toString();
        String[] ArrayLogMessage = LogMessage.split(" -> ");

        if (!ArrayLogMessage[1].equals("Debug-Logger-Test\n")) {
            assertFalse("Logger is not working properly: " + baos.toString(), true);
        }

    }


}

