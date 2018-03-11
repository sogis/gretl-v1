package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.TestUtil;
import ch.so.agi.gretl.util.TestUtilSqlPg;
import ch.so.agi.gretl.util.TestUtilSqlSqlite;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Random;
import java.util.UUID;

public class Sqlite2PostgresTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Tests loading several hundred thousand rows from sqlite to postgres.
     * Loading 300'000 rows should take about 15 seconds
     */
	@Test
	public void positiveBulkLoadPostgisTest() throws Exception {
        int numRows = 300000;
        String schemaName = "BULKLOAD2POSTGIS".toLowerCase();
        String geomWkt = "LINESTRING(2600000 1200000,2600001 1200001)";
        String sqliteDbFileName = "src/functionalTest/jobs/sqlite2postgresBulk/"+schemaName+".sqlite";
        
        Connection srcCon = null;
        Connection targetCon = null;
        
        try {
            // prepare sqlite source database
            File sqliteDb = new File(sqliteDbFileName);
            Files.deleteIfExists(sqliteDb.toPath());
            sqliteDb = new File(sqliteDbFileName);
            
            srcCon = TestUtilSqlSqlite.connect(sqliteDb);
            
            String sqlSqlite = "CREATE TABLE source_data(MYINT INTEGER, MYFLOAT REAL, MYTEXT TEXT, MYWKT TEXT)";
            Statement stmtSqlite = srcCon.createStatement();
            stmtSqlite.execute(sqlSqlite);

            Random random = new Random();

            PreparedStatement ps = srcCon.prepareStatement("INSERT INTO source_data VALUES(?, ?, ?, ?)");
            for(int i=0; i<numRows; i++){
                ps.setInt(1, random.nextInt());
                ps.setDouble(2,random.nextDouble());
                ps.setString(3, UUID.randomUUID().toString());
                ps.setString(4, geomWkt);
                ps.addBatch();
            }

            ps.executeBatch();
            ps.close();

            srcCon.commit();
            TestUtilSqlSqlite.closeCon(srcCon);
            
            // prepare postgis target database
            targetCon = TestUtilSqlPg.connect();
            TestUtilSqlPg.createOrReplaceSchema(targetCon, schemaName);

            String sqlPg = "CREATE TABLE "+schemaName+".target_data(MYINT INTEGER, MYFLOAT REAL, MYTEXT VARCHAR(50), MYGEOM GEOMETRY(LINESTRING,2056))";

            Statement stmtPg = targetCon.createStatement();
            stmtPg.execute(sqlPg);
            
            TestUtilSqlPg.grantDataModsInSchemaToUser(targetCon, schemaName, TestUtilSqlPg.CON_DMLUSER);

            targetCon.commit();
            TestUtilSqlPg.closeCon(targetCon);

            // run gradle job
            GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSqlPg.VARNAME_CON_URI, TestUtilSqlPg.CON_URI)};
            TestUtil.runJob("jobs/Sqlite2PostgresBulk", gvs);

            // check
            targetCon = TestUtilSqlPg.connect();            
            int countDest = TestUtilSqlPg.execCountQuery(targetCon, "SELECT count(*) FROM "+schemaName+".target_data;");

            Assert.assertEquals(
                    "Check Statement must return exactly " + numRows,
                    numRows,
                    countDest);
        } finally {
            if(srcCon != null){ srcCon.close(); }
            if(targetCon != null){ targetCon.close(); }
            Files.deleteIfExists(new File(sqliteDbFileName).toPath());
        }
	}
	
    /**
     * Tests if the sqlite datatypes and geometry as wkt are transferred
     * faultfree from sqlite to postgis
     */
    @Test
    public void sqlite2postgresDatatypes() throws Exception {
        int numRows = 1;
        String schemaName = "DATATYPES2POSTGIS".toLowerCase();
        String geomWkt = "LINESTRING(2600000 1200000,2600001 1200001)";
        String sqliteDbFileName = "src/functionalTest/jobs/sqlite2postgresDatatypes/"+schemaName+".sqlite";
        
        Connection srcCon = null;
        Connection targetCon = null;
        
        try {
            // prepare sqlite source database
            File sqliteDb = new File(sqliteDbFileName);
            Files.deleteIfExists(sqliteDb.toPath());
            sqliteDb = new File(sqliteDbFileName);
            
            srcCon = TestUtilSqlSqlite.connect(sqliteDb);
            
            String sqlSqlite = "CREATE TABLE source_data(MYINT INTEGER, MYFLOAT REAL, MYTEXT TEXT, MYWKT TEXT)";
            Statement stmtSqlite = srcCon.createStatement();
            stmtSqlite.execute(sqlSqlite);

            Random random = new Random();

            PreparedStatement ps = srcCon.prepareStatement("INSERT INTO source_data VALUES(?, ?, ?, ?)");
            for(int i=0; i<numRows; i++){
                ps.setInt(1, random.nextInt());
                ps.setDouble(2,random.nextDouble());
                ps.setString(3, UUID.randomUUID().toString());
                ps.setString(4, geomWkt);
                ps.addBatch();
            }

            ps.executeBatch();
            ps.close();

            srcCon.commit();
            TestUtilSqlSqlite.closeCon(srcCon);
            
            // prepare postgis target database
            targetCon = TestUtilSqlPg.connect();
            TestUtilSqlPg.createOrReplaceSchema(targetCon, schemaName);

            String sqlPg = "CREATE TABLE "+schemaName+".target_data(MYINT INTEGER, MYFLOAT REAL, MYTEXT VARCHAR(50), MYGEOM GEOMETRY(LINESTRING,2056))";

            Statement stmtPg = targetCon.createStatement();
            stmtPg.execute(sqlPg);
            
            TestUtilSqlPg.grantDataModsInSchemaToUser(targetCon, schemaName, TestUtilSqlPg.CON_DMLUSER);

            targetCon.commit();
            TestUtilSqlPg.closeCon(targetCon);

            // run gradle job
            GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSqlPg.VARNAME_CON_URI, TestUtilSqlPg.CON_URI)};
            TestUtil.runJob("jobs/Sqlite2PostgresDatatypes", gvs);

            // check
            String checkSql = "SELECT COUNT(*) FROM "+schemaName+".target_data " +
                    "WHERE MYINT IS NOT NULL AND MYFLOAT IS NOT NULL AND MYTEXT IS NOT NULL AND " +
                    "ST_Equals(MYGEOM, ST_GeomFromText('"+geomWkt+"', 2056)) = True;";

            targetCon = TestUtilSqlPg.connect();            
            int countDest = TestUtilSqlPg.execCountQuery(targetCon, checkSql);

            Assert.assertEquals(
                    "Check Statement must return exactly " + numRows,
                    numRows,
                    countDest);    
        } finally {
            if(srcCon != null){ srcCon.close(); }
            if(targetCon != null){ targetCon.close(); }
            Files.deleteIfExists(new File(sqliteDbFileName).toPath());
        }
    }
}
