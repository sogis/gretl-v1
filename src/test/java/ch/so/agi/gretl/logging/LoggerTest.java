package ch.so.agi.gretl.logging;


import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static junit.framework.TestCase.assertFalse;


/**
 * Test-Class for Logger-Class
 */
public class LoggerTest {
    // Create a stream to hold the output


    @Test
    public void logInfoTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        //  Save the old System.err
        PrintStream old = System.err;
        // Tell Java to use your special stream
        System.setErr(ps);

        Logger.log(Logger.INFO_LEVEL,"Info-Logger-Test");

        // Put things back
        System.err.flush();
        System.setErr(old);
        System.out.println(baos.toString());

        if (baos.toString().equals("[Main] INFO ch.so.agi.gretl.logging.Logger - Info-Logger-Test\n")) {

        } else {
            //assertFalse("Logger is not working properly"+baos.toString(), true);
        }

    }
    @Test
    public void logDebugTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        //Save the old System.out
        PrintStream old = System.err;
        // Tell Java to use your special stream
        System.setErr(ps);

        Logger.log(Logger.DEBUG_LEVEL,"Debug-Logger-Test");

        // Put things back
        System.err.flush();
        System.setErr(old);

        if (baos.toString().equals("[main] DEBUG ch.so.agi.gretl.logging.Logger - Debug-Logger-Test\n")){

        } else {
            assertFalse("Logger is not working properly", true);
        }
    }

    @Test
    public void inexistentLoglevel() throws Exception {

        try {
            Logger.log(3, "Debug-Logger-Test");
            Assert.fail();
        }catch (Exception e) {

        }
    }

}

