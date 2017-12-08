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
            createSqlExecuterTaskChainTables(con, schemaName);

            con.commit();
            TestUtilSql.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSql.VARNAME_PG_CON_URI, TestUtilSql.PG_CON_URI)};
            TestUtil.runJob("jobs/sqlExecutorTask", gvs);

            //reconnect to check results
            con = TestUtilSql.connectPG();

            String countSrcSql = String.format("select count(*) from %s.albums_src", schemaName);
            String countDestSql = String.format("select count(*) from %s.albums_dest", schemaName);

            int countSrc = TestUtilSql.execCountQuery(con, countSrcSql);
            int countDest = TestUtilSql.execCountQuery(con, countDestSql);

            Assert.assertEquals("Rowcount in destination table must be equal to rowcount in source table", countSrc, countDest);
            Assert.assertTrue("Rowcount in destination table must be greater than zero", countDest > 0);
        }
        finally {
            TestUtilSql.closeCon(con);
        }
    }

    private static void createSqlExecuterTaskChainTables(Connection con, String schemaName){

        String ddlBase = "CREATE TABLE %s.albums_%s(" +
                "title text, artist text, release_date text," +
                "publisher text, media_type text)";

        try{
            //source table
            Statement s1 = con.createStatement();
            System.out.println(String.format(ddlBase, schemaName, "src"));
            s1.execute(String.format(ddlBase, schemaName, "src"));
            s1.close();

            //dest table
            Statement s2 = con.createStatement();
            s2.execute(String.format(ddlBase, schemaName,"dest"));
            s2.close();

            TestUtilSql.grantTableModsInSchemaToUser(con, schemaName, "dmlUser");
        }
        catch(SQLException se){
            throw new RuntimeException(se);
        }
    }
}
