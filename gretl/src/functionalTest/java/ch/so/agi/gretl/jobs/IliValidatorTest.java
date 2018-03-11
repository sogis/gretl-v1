package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.TestUtil;
import org.junit.Test;
import static org.junit.Assert.*;

public class IliValidatorTest {
    @Test
    public void validationOk() throws Exception {
        GradleVariable[] gvs = null; // {GradleVariable.newGradleProperty(TestUtilSqlPg.VARNAME_CON_URI, TestUtilSqlPg.CON_URI)};
        TestUtil.runJob("jobs/IliValidator", gvs);
    }
    
    @Test
    public void validationFail() throws Exception {
        GradleVariable[] gvs = null; // {GradleVariable.newGradleProperty(TestUtilSqlPg.VARNAME_CON_URI, TestUtilSqlPg.CON_URI)};
        assertEquals(1, TestUtil.runJob("jobs/IliValidatorFail", gvs, new StringBuffer(), new StringBuffer()));
    }
}
