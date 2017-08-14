package ch.so.agi.gretl.logging;

import org.junit.*;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.logging.Logger;




/**
 * Test-Class for Logger-Class
 */
public class LoggerTest {

    private GretlLogger log;
    private static ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private static PrintStream oldSystemErr = System.err;
    private static PrintStream oldSystemOut = System.out;



    public LoggerTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());

    }

    @BeforeClass
    public static void initialise() {

        PrintStream ps;
        ps = new PrintStream(baos);

        System.setErr(ps);
        System.setOut(ps);

    }

    @Before
    public void cleanPrintStream(){
        baos.reset();
    }


    @Test
    public void logInfoTest() throws Exception {

        try {

            log.info("$Info-Logger-Test$");

            String LogMessage = baos.toString();

            Assert.assertTrue(LogMessage.contains("$Info-Logger-Test$"));

        } finally {
            resetSystemOutAndErr();
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

        try {
            log.error("$Error-Logger-Test$", new RuntimeException("Test Exception"));

            String LogMessage = baos.toString();
            
            Assert.assertTrue(LogMessage.contains("$Error-Logger-Test$"));

        } finally {
            resetSystemOutAndErr();
        }

    }


    @Test
    public void logDebugTest() throws Exception {

        try{
            log.debug("$Debug-Logger-Test$");

            String LogMessage = baos.toString();

            Assert.assertTrue(LogMessage.contains("$Debug-Logger-Test$"));

        } finally {
            resetSystemOutAndErr();
        }

    }



    private void resetSystemOutAndErr () {
        System.err.flush();
        System.setErr(oldSystemErr);
        System.setOut(oldSystemOut);
    }


}

