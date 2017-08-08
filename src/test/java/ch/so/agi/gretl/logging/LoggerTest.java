package ch.so.agi.gretl.logging;

import org.junit.*;

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
    private static ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private static PrintStream oldSystem = System.err;
    private static PrintStream ps;


    public LoggerTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());

    }

    @BeforeClass
    public static void initialise() {

        PrintStream ps = new PrintStream(baos);
        System.setErr(ps);
        //todo System.setOut auch notwendig
        //todo Es ist kritisch dass die Streams nach einem Test in jedem fall zurückgesetzt werden --> im finally der jeweiligen Testmethode,
        // nicht im  @AfterClass der ganzen Testklasse....

    }

    @Before
    public void cleanPrintStream(){
        baos.reset();
    }

    @AfterClass
    public static void finalise() {

        System.err.flush();
        System.setErr(oldSystem);

    }


    // Tell Java to use your special stream

    @Test
    public void logInfoTest() throws Exception {

        log.info("Info-Logger-Test");

        String LogMessage = baos.toString();
        String[] ArrayLogMessage = LogMessage.split(" -> ");

        //todo direkt Assert.assertEquals verwenden....
        if (!ArrayLogMessage[1].equals("Info-Logger-Test\n")) {
            assertFalse("Logger is not working properly: " + baos.toString(), true);
        }
    }

    @Test
    public void loggerOutputsCallingClassAsLogSource() throws Exception
    {
        Logger jlog = ((CoreJavaLogAdaptor)log).getInnerLogger();
        assertEquals("The logSource must be equal to the name of this test class", jlog.getName(), this.getClass().getName());
    }

    @Test
    public void logErrorTest() throws Exception {

        log.error("Error-Logger-Test", new RuntimeException("Test Exception"));

        String LogMessage = baos.toString();
        String[] ArrayLogMessage = LogMessage.split(" -> ");

        String[] ArrayMessage = ArrayLogMessage[1].split("\n");


        if (!ArrayMessage[0].equals("Error-Logger-Test")) {
            assertFalse("Logger is not working properly: " + baos.toString(), true);
        }

    }


    @Test
    public void logDebugTest() throws Exception {

        log.debug("Debug-Logger-Test");

        String LogMessage = baos.toString();
        String[] ArrayLogMessage = LogMessage.split(" -> ");

        if (!ArrayLogMessage[1].equals("Debug-Logger-Test\n")) {
            assertFalse("Logger is not working properly: " + baos.toString(), true);
        }

    }


}

