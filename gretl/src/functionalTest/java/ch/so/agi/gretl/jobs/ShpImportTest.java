package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.TestUtil;
import ch.so.agi.gretl.util.TestUtilSqlPg;

import org.junit.Test;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ShpImportTest {
    @Test
    public void importOk() throws Exception {
        String schemaName = "shpimport".toLowerCase();
        Connection con = null;
        try {
        	    // prepare postgres
            con = TestUtilSqlPg.connect();
            TestUtilSqlPg.createOrReplaceSchema(con, schemaName);
            Statement s1 = con.createStatement();
            s1.execute("CREATE TABLE "+schemaName+".importdata(t_id serial, \"Aint\" integer, adec decimal(7,1), atext varchar(40), aenum varchar(120),adate date, geometrie geometry(POINT,2056), aextra varchar(40))");
            s1.close();
            TestUtilSqlPg.grantDataModsInSchemaToUser(con, schemaName, TestUtilSqlPg.CON_DMLUSER);

            con.commit();
            TestUtilSqlPg.closeCon(con);

            // run job
            GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSqlPg.VARNAME_CON_URI, TestUtilSqlPg.CON_URI)};
            TestUtil.runJob("jobs/ShpImport", gvs);

            // check results
            con = TestUtilSqlPg.connect();

            Statement s2 = con.createStatement();
            ResultSet rs=s2.executeQuery("SELECT \"Aint\" , adec, atext, aenum,adate, ST_X(geometrie), ST_Y(geometrie), aextra FROM "+schemaName+".importdata WHERE t_id=1"); 
            if (!rs.next()) {
                fail();
            }
            assertEquals(2,rs.getInt(1));
            assertEquals(new BigDecimal("3.4"),rs.getBigDecimal(2));
            assertEquals("abc",rs.getString(3));
            assertEquals("rot",rs.getString(4));
            assertEquals(new java.sql.Date(2013-1900,10-1,21),rs.getDate(5));
            assertEquals(2638000.0,rs.getFloat(6),0.000001);
            assertEquals(1175250.0,rs.getFloat(7),0.000001);
            if (rs.next()) {
                fail();
            }
            rs.close();
            s1.close();
        } finally {
            TestUtilSqlPg.closeCon(con);
        }
    }
	
    @Test
    public void importOkBatchSize() throws Exception {
        String schemaName = "shpimport".toLowerCase();
        Connection con = null;
        try {
        	    // prepare postgres
            con = TestUtilSqlPg.connect();
            TestUtilSqlPg.createOrReplaceSchema(con, schemaName);
            Statement s1 = con.createStatement();
            s1.execute("CREATE TABLE "+schemaName+".importdata_batchsize(t_id serial, \"Aint\" integer, adec decimal(7,1), atext varchar(40), aenum varchar(120),adate date, geometrie geometry(POINT,2056), aextra varchar(40))");
            s1.close();
            TestUtilSqlPg.grantDataModsInSchemaToUser(con, schemaName, TestUtilSqlPg.CON_DMLUSER);

            con.commit();
            TestUtilSqlPg.closeCon(con);

            // run job
            GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSqlPg.VARNAME_CON_URI, TestUtilSqlPg.CON_URI)};
            TestUtil.runJob("jobs/ShpImportBatchSize", gvs);

            // check results
            con = TestUtilSqlPg.connect();

            Statement s2 = con.createStatement();
            ResultSet rs=s2.executeQuery("SELECT \"Aint\" , adec, atext, aenum,adate, ST_X(geometrie), ST_Y(geometrie), aextra FROM "+schemaName+".importdata_batchsize WHERE t_id=1"); 
            if (!rs.next()) {
                fail();
            }
            assertEquals(2,rs.getInt(1));
            assertEquals(new BigDecimal("3.4"),rs.getBigDecimal(2));
            assertEquals("abc",rs.getString(3));
            assertEquals("rot",rs.getString(4));
            assertEquals(new java.sql.Date(2013-1900,10-1,21),rs.getDate(5));
            assertEquals(2638000.0,rs.getFloat(6),0.000001);
            assertEquals(1175250.0,rs.getFloat(7),0.000001);
            if (rs.next()) {
                fail();
            }
            rs.close();
            s1.close();
        } finally {
            TestUtilSqlPg.closeCon(con);
        }
    }
}
