package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.TestUtil;
import ch.so.agi.gretl.util.TestUtilSql;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.*;

public class Db2DbTaskTest {

    /*
    Test's that a chain of statements executes properly and results in the correct
    number of inserts (corresponding to the last statement)
        1. Statement transfers rows from a to b
        2. Statement transfers rows from b to a
    */
    @Test
    public void db2dbTaskChainTest() throws Exception {
        String schemaName = "db2dbTaskChain".toLowerCase();
        Connection con = null;
        try{
            con = TestUtilSql.connectPG();
            TestUtilSql.createOrReplaceSchema(con, schemaName);
            int countSrc = TestUtilSql.prepareDb2DbChainTables(con, schemaName);
            con.commit();
            TestUtilSql.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSql.VARNAME_PG_CON_URI, TestUtilSql.PG_CON_URI)};
            TestUtil.runJob("jobs/db2dbTask", gvs);
        }
        finally {
            TestUtilSql.closeCon(con);
        }
    }
}
