package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.TestUtil;
import ch.so.agi.gretl.util.TestUtilSql;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlExecutorTaskTest {
    /*
    Test's that a chain of statements executes properly and results in the correct
    number of inserts (corresponding to the last statement)
    1. statement: create the schema for the tables
    2. statement: fill the source table with rows
    3. statement: execute the "insert into select from" statement
    */
    @Test
    public void sqlExecuterTaskChainTest() throws Exception {
        String schemaName = "sqlExecuterTaskChain".toLowerCase();
        Connection con = null;
        try{
            con = TestUtilSql.connectPG();
            TestUtilSql.createOrReplaceSchema(con, schemaName);
            TestUtilSql.createSqlExecuterTaskChainTables(con, schemaName);

            con.commit();
            TestUtilSql.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSql.VARNAME_PG_CON_URI, TestUtilSql.PG_CON_URI)};
            TestUtil.runJob("jobs/sqlExecutorTask", gvs);
        }
        finally {
            TestUtilSql.closeCon(con);
        }
    }
}
