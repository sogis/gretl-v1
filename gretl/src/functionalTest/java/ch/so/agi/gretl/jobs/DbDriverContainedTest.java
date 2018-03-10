package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.TestUtil;
import ch.so.agi.gretl.util.TestUtilSqlOra;
import org.junit.Test;
import ch.so.agi.gretl.util.DbDriversReachableTest;
import org.junit.experimental.categories.Category;

public class DbDriverContainedTest {

    @Category(DbDriversReachableTest.class)
    @Test
    public void SqliteDriverContainedTest() throws Exception {
        TestUtil.runJob("jobs/dbTasks_SqliteLibsPresent");
    }

    @Category(DbDriversReachableTest.class)
    @Test
    public void OracleDriverContainedTest() throws Exception {
        GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSqlOra.VARNAME_CON_URI, TestUtilSqlOra.CON_URI)};
        TestUtil.runJob("jobs/dbTasks_OracleLibsPresent", gvs);
    }
}