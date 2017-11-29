package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.TestUtil;
import org.junit.Test;

public class IliValidatorTest {

    @Test
    public void test() throws Exception {
        TestUtil.runJob("jobs/iliValidator");
    }
}
