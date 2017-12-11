package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.TestUtil;
import ch.so.agi.gretl.util.TestUtilSql;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;



public class Db2DbTaskTest {
    /*
Test's that a chain of statements executes properly and results in the correct
number of inserts (corresponding to the last statement)
    1. Statement transfers rows from a to b
    2. Statement transfers rows from b to a
*/
    @Test
    public void taskChainTest() throws Exception {
        String schemaName = "db2dbTaskChain".toLowerCase();
        Connection con = null;
        try{
            con = TestUtilSql.connectPG();
            TestUtilSql.createOrReplaceSchema(con, schemaName);
            int countSrc = TestUtilSql.prepareDb2DbChainTables(con, schemaName);
            con.commit();
            TestUtilSql.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSql.VARNAME_PG_CON_URI, TestUtilSql.PG_CON_URI)};
            TestUtil.runJob("jobs/db2dbTaskChain", gvs);

            //reconnect to check results
            con = TestUtilSql.connectPG();
            String countDestSql = String.format("select count(*) from %s.albums_dest", schemaName);
            int countDest = TestUtilSql.execCountQuery(con, countDestSql);

            Assert.assertEquals(
                    "Rowcount in table albums_src must be equal to rowcount in table albums_dest",
                    countSrc,
                    countDest);
        }
        finally {
            TestUtilSql.closeCon(con);
        }
    }

    /**
     * Test's if the sql-files can be configured using a relative path.
     *
     * The relative path relates to the location of the build.gradle file
     * of the corresponding gretl job.
     */
    @Test
    public void relativePathTest() throws Exception{
        String schemaName = "relativePath".toLowerCase();
        Connection con = null;
        try{
            con = TestUtilSql.connectPG();
            TestUtilSql.createOrReplaceSchema(con, schemaName);

            int countSrc = TestUtilSql.prepareDb2DbChainTables(con, schemaName);
            con.commit();
            TestUtilSql.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSql.VARNAME_PG_CON_URI, TestUtilSql.PG_CON_URI)};
            TestUtil.runJob("jobs/db2dbTaskRelPath", gvs);

            //reconnect to check results
            con = TestUtilSql.connectPG();
            String countDestSql = String.format("select count(*) from %s.albums_dest", schemaName);
            int countDest = TestUtilSql.execCountQuery(con, countDestSql);

            Assert.assertEquals(
                    "Rowcount in table albums_src must be equal to rowcount in table albums_dest",
                    countSrc,
                    countDest);
        }
        finally {
            TestUtilSql.closeCon(con);
        }
    }

    /**
     * Test's that the delete flag of the Db2dbTask's Transferset works properly
     */
    @Test
    public void deleteDestTableContent() throws Exception{
        String schemaName = "deleteDestTableContent".toLowerCase();
        Connection con = null;
        try{
            con = TestUtilSql.connectPG();
            TestUtilSql.createOrReplaceSchema(con, schemaName);

            int countSrc = TestUtilSql.prepareDb2DbChainTables(con, schemaName);
            TestUtilSql.insertRowsInAlbumsTable(con, schemaName, "dest", 3);

            con.commit();
            TestUtilSql.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSql.VARNAME_PG_CON_URI, TestUtilSql.PG_CON_URI)};
            TestUtil.runJob("jobs/db2dbTaskDelTable", gvs);

            //reconnect to check results
            con = TestUtilSql.connectPG();
            String countDestSql = String.format("select count(*) from %s.albums_dest", schemaName);
            int countDest = TestUtilSql.execCountQuery(con, countDestSql);

            Assert.assertEquals(
                    "Rowcount in table albums_src must be equal to rowcount in table albums_dest",
                    countSrc,
                    countDest);
        }
        finally {
            TestUtilSql.closeCon(con);
        }
    }
}
