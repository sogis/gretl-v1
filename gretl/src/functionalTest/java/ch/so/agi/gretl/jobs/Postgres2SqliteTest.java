package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.TestUtil;
import ch.so.agi.gretl.util.TestUtilSqlPg;
import ch.so.agi.gretl.util.TestUtilSqlSqlite;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

public class Postgres2SqliteTest {
    /**
     * Tests if the "special" datatypes (Date, Time, GUID, Geometry, ..) are transferred
     * faultfree from postgres to sqlite.
     */
    @Test
    public void positivePostgis2SqliteTest() throws Exception {
        String schemaName = "POSTGRES2SQLITE".toLowerCase();
        String geomWkt = "LINESTRING(2600000 1200000,2600001 1200001)";
        String sqliteDbFileName = "src/functionalTest/jobs/postgres2sqliteDatatypes/"+schemaName+".sqlite";

        Connection srcCon = null;
        Connection targetCon = null;
        
        try {
        	    // prepare postgres
            targetCon = TestUtilSqlPg.connect();
            TestUtilSqlPg.createOrReplaceSchema(targetCon, schemaName);

            Statement stmtPg = targetCon.createStatement();
            String createSqlPg = "CREATE TABLE "+schemaName+".source_data(MYINT INTEGER, MYFLOAT REAL, MYTEXT VARCHAR(50), MYDATE DATE, MYTIME TIME, " +
                    "MYUUID UUID, MYGEOM GEOMETRY(LINESTRING,2056))";
            stmtPg.execute(createSqlPg);

            String insertSqlPg = "INSERT INTO "+schemaName+".source_data VALUES(15, 9.99, 'Hello Db2Db', CURRENT_DATE, CURRENT_TIME, '"+UUID.randomUUID()+"', ST_GeomFromText('"+geomWkt+"', 2056))";
            stmtPg.execute(insertSqlPg);

            TestUtilSqlPg.grantDataModsInSchemaToUser(targetCon, schemaName, TestUtilSqlPg.CON_DMLUSER);

            targetCon.commit();

            // prepare sqlite
            File sqliteDb = new File(sqliteDbFileName);
            Files.deleteIfExists(sqliteDb.toPath());
            sqliteDb = new File(sqliteDbFileName);

            srcCon = TestUtilSqlSqlite.connect(sqliteDb);
            
            String sqlSqlite = "CREATE TABLE target_data(MYINT INTEGER, MYFLOAT REAL, MYTEXT TEXT, MYDATE TEXT, MYTIME TEXT, " +
                    "MYUUID TEXT, MYGEOM_WKT TEXT)";
            Statement stmtSqlite = srcCon.createStatement();
            stmtSqlite.execute(sqlSqlite);

            srcCon.commit();
            TestUtilSqlSqlite.closeCon(srcCon);

            // run gradle
            GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSqlPg.VARNAME_CON_URI, TestUtilSqlPg.CON_URI)};
            TestUtil.runJob("jobs/Postgres2SqliteDatatypes", gvs);

            // check results
            String checkSQL = "SELECT COUNT(*) FROM target_data WHERE " +
                    "MYINT IS NOT NULL AND MYFLOAT IS NOT NULL AND MYTEXT IS NOT NULL AND MYDATE IS NOT NULL AND MYTIME IS NOT NULL AND MYUUID IS NOT NULL AND " +
                    "MYGEOM_WKT = '"+geomWkt+"'";

            targetCon = TestUtilSqlSqlite.connect(sqliteDb);            
            int countDest = TestUtilSqlSqlite.execCountQuery(targetCon, "SELECT count(*) FROM target_data;");

            Assert.assertEquals(
                    "Check Statement must return exactly one line.",
                    1,
                    countDest);
        } finally {
        	    TestUtilSqlPg.closeCon(srcCon);
        	    TestUtilSqlSqlite.closeCon(targetCon);
            Files.deleteIfExists(new File(sqliteDbFileName).toPath());
        }
    }
}
