package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.TestUtil;
import ch.so.agi.gretl.util.TestUtilSqlPg;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Db2DbTaskTest {
    /**
     * Tests if db2db can handle (write) standard wkb geometry type.
     * Db2Db supports casting binary data to postgis wkb by using
     * an additional third parameter in transferset.
     * src: binary data (= ST_AsBinary)
     * dest: PostGIS geometry
     * @throws Exception
     */
    @Test
    public void canWriteGeomFromWkbTest() throws Exception {
        String schemaName = "db2dbWkb".toLowerCase();
        Connection con = null;
        String geomSrc = "LINESTRING(2600000 1200000,2600001 1200001)";
        try {
            con = TestUtilSqlPg.connect();
            TestUtilSqlPg.createOrReplaceSchema(con, schemaName);

            Statement stmt = con.createStatement();
            stmt.execute("CREATE TABLE "+schemaName+".source_data(geom geometry(LINESTRING,2056))");
            stmt.execute("INSERT INTO "+schemaName+".source_data(geom) VALUES (ST_GeomFromText('"+geomSrc+"', 2056))");
            stmt.execute("CREATE TABLE "+schemaName+".target_data(geom geometry(LINESTRING,2056))");
            stmt.close();
            TestUtilSqlPg.grantDataModsInSchemaToUser(con, schemaName, TestUtilSqlPg.CON_DMLUSER);

            con.commit();
            TestUtilSqlPg.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSqlPg.VARNAME_CON_URI, TestUtilSqlPg.CON_URI)};
            TestUtil.runJob("jobs/Db2DbWkb", gvs);
            
            con = TestUtilSqlPg.connect();
            assertEqualGeomInSourceAndTarget(con, schemaName, "target_data", geomSrc);
        }
        finally {
            TestUtilSqlPg.closeCon(con);
        }
    }
    
    /**
     * Tests if db2db can handle (write) wkt geometry to PostGIS as geometry.
     * This is supported by a third transferset parameter.
     * src: wkt
     * dest: PostGIS geometry
     * @throws Exception
     */
    @Test
    public void canWriteGeomFromWktTest() throws Exception {
        String schemaName = "db2dbWkt".toLowerCase();
        Connection con = null;
        String geomSrc = "LINESTRING(2600000 1200000,2600001 1200001)";
        try {
            con = TestUtilSqlPg.connect();
            TestUtilSqlPg.createOrReplaceSchema(con, schemaName);

            Statement stmt = con.createStatement();
            stmt.execute("CREATE TABLE "+schemaName+".source_data(geom geometry(LINESTRING,2056))");
            stmt.execute("INSERT INTO "+schemaName+".source_data(geom) VALUES (ST_GeomFromText('"+geomSrc+"', 2056))");
            stmt.execute("CREATE TABLE "+schemaName+".target_data(geom geometry(LINESTRING,2056))");
            stmt.close();
            TestUtilSqlPg.grantDataModsInSchemaToUser(con, schemaName, TestUtilSqlPg.CON_DMLUSER);

            con.commit();
            TestUtilSqlPg.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSqlPg.VARNAME_CON_URI, TestUtilSqlPg.CON_URI)};
            TestUtil.runJob("jobs/Db2DbWkt", gvs);
            
            con = TestUtilSqlPg.connect();
            assertEqualGeomInSourceAndTarget(con, schemaName, "target_data", geomSrc);
        }
        finally {
            TestUtilSqlPg.closeCon(con);
        }
    }
    
    /**
     * Tests if db2db can handle (write) a geosjon geometry to PostGIS as geometry.
     * This is supported by a third transferset parameter.
     * src: geojson
     * dest: PostGIS geometry
     * @throws Exception
     */
    @Test
    public void canWriteGeomFromGeoJsonTest() throws Exception {
        String schemaName = "db2dbGeoJson".toLowerCase();
        Connection con = null;
        String geomSrc = "LINESTRING(2600000 1200000,2600001 1200001)";
        try {
            con = TestUtilSqlPg.connect();
            TestUtilSqlPg.createOrReplaceSchema(con, schemaName);

            Statement stmt = con.createStatement();
            stmt.execute("CREATE TABLE "+schemaName+".source_data(geom geometry(LINESTRING,2056))");
            stmt.execute("INSERT INTO "+schemaName+".source_data(geom) VALUES (ST_GeomFromText('"+geomSrc+"', 2056))");
            stmt.execute("CREATE TABLE "+schemaName+".target_data(geom geometry(LINESTRING,2056))");
            stmt.close();
            TestUtilSqlPg.grantDataModsInSchemaToUser(con, schemaName, TestUtilSqlPg.CON_DMLUSER);

            con.commit();
            TestUtilSqlPg.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSqlPg.VARNAME_CON_URI, TestUtilSqlPg.CON_URI)};
            TestUtil.runJob("jobs/Db2DbGeoJson", gvs);
            
            con = TestUtilSqlPg.connect();
            assertEqualGeomInSourceAndTarget(con, schemaName, "target_data", geomSrc);
        }
        finally {
            TestUtilSqlPg.closeCon(con);
        }
    }
    
    /**
     * Tests if fetchSize parameter is working.
     * Gradle throws an error if a parameter is being
     * used that is not defined in the task class.
     */
    @Test
    public void fetchSizeParameterTest() throws Exception {
        String schemaName = "db2dbTaskFetchSize".toLowerCase();
        Connection con = null;
        try {
            con = TestUtilSqlPg.connect();
            TestUtilSqlPg.createOrReplaceSchema(con, schemaName);

            Statement stmt = con.createStatement();
            stmt.execute("CREATE TABLE "+schemaName+".source_data(t_id serial, aint integer, adec decimal(7,1), atext varchar(40), aenum varchar(120),adate date, atimestamp timestamp, aboolean boolean,geom_so geometry(POINT,2056))");
            stmt.execute("INSERT INTO "+schemaName+".source_data(t_id, aint, adec, atext, adate, atimestamp, aboolean, geom_so) VALUES (1,2,3.4,'abc','2013-10-21','2015-02-16T08:35:45.000','true',ST_GeomFromText('POINT(2638000.0 1175250.0)',2056))");
            stmt.execute("INSERT INTO "+schemaName+".source_data(t_id, aint, adec, atext, adate, atimestamp, aboolean, geom_so) VALUES (2,33,44.4,'asdf','2017-12-21','2015-03-16T11:35:45.000','true',ST_GeomFromText('POINT(2648000.0 1185250.0)',2056))");
            
            stmt.execute("CREATE TABLE "+schemaName+".target_data(t_id serial, aint integer, adec decimal(7,1), atext varchar(40), aenum varchar(120),adate date, atimestamp timestamp, aboolean boolean,geom_so geometry(POINT,2056))");

            stmt.close();
            TestUtilSqlPg.grantDataModsInSchemaToUser(con, schemaName, TestUtilSqlPg.CON_DMLUSER);

            con.commit();
            TestUtilSqlPg.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSqlPg.VARNAME_CON_URI, TestUtilSqlPg.CON_URI)};
            TestUtil.runJob("jobs/Db2DbTaskFetchSize", gvs);
            
            con = TestUtilSqlPg.connect();
            String countDestSql = String.format("select count(*) from %s.target_data", schemaName);
            int countDest = TestUtilSqlPg.execCountQuery(con, countDestSql);

            Assert.assertEquals(
                    "Rowcount in table source_data must be equal to rowcount in table target_data",
                    2,
                    countDest);
        }
        finally {
            TestUtilSqlPg.closeCon(con);
        }
    }
    
    /**
     * Tests that a chain of statements executes properly and results in the correct
     * number of inserts (corresponding to the last statement)
     * 1. Statement transfers rows from a to b
     * 2. Statement transfers rows from b to a
     */
    @Test
    public void taskChainTest() throws Exception {
        String schemaName = "db2dbTaskChain".toLowerCase();
        Connection con = null;
        try{
            con = TestUtilSqlPg.connect();
            TestUtilSqlPg.createOrReplaceSchema(con, schemaName);
            int countSrc = prepareDb2DbChainTables(con, schemaName);
            con.commit();
            TestUtilSqlPg.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSqlPg.VARNAME_CON_URI, TestUtilSqlPg.CON_URI)};
            TestUtil.runJob("jobs/Db2DbTaskChain", gvs);

            //reconnect to check results
            con = TestUtilSqlPg.connect();
            String countDestSql = String.format("SELECT count(*) FROM %s.albums_dest", schemaName);
            int countDest = TestUtilSqlPg.execCountQuery(con, countDestSql);

            Assert.assertEquals(
                    "Rowcount in table albums_src must be equal to rowcount in table albums_dest",
                    countSrc,
                    countDest);
        }
        finally {
            TestUtilSqlPg.closeCon(con);
        }
    }

    /**
     * Tests if the sql-files can be configured using a relative path.
     *
     * The relative path relates to the location of the build.gradle file
     * of the corresponding gretl job.
     */
    @Test
    public void relativePathTest() throws Exception{
        String schemaName = "relativePath".toLowerCase();
        Connection con = null;
        try{
            con = TestUtilSqlPg.connect();
            TestUtilSqlPg.createOrReplaceSchema(con, schemaName);

            int countSrc = prepareDb2DbChainTables(con, schemaName);
            con.commit();
            TestUtilSqlPg.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSqlPg.VARNAME_CON_URI, TestUtilSqlPg.CON_URI)};
            TestUtil.runJob("jobs/Db2DbTaskRelPath", gvs);

            //reconnect to check results
            con = TestUtilSqlPg.connect();
            String countDestSql = String.format("SELECT count(*) FROM %s.albums_dest", schemaName);
            int countDest = TestUtilSqlPg.execCountQuery(con, countDestSql);

            Assert.assertEquals(
                    "Rowcount in table albums_src must be equal to rowcount in table albums_dest",
                    countSrc,
                    countDest);
        }
        finally {
            TestUtilSqlPg.closeCon(con);
        }
    }

    /**
     * Tests that the delete flag of the Db2dbTask's Transferset works properly
     */
    @Test
    public void deleteDestTableContent() throws Exception{
        String schemaName = "deleteDestTableContent".toLowerCase();
        Connection con = null;
        try{
            con = TestUtilSqlPg.connect();
            TestUtilSqlPg.createOrReplaceSchema(con, schemaName);

            int countSrc = prepareDb2DbChainTables(con, schemaName);
            insertRowsInAlbumsTable(con, schemaName, "dest", 3);

            con.commit();
            TestUtilSqlPg.closeCon(con);

            GradleVariable[] gvs = {GradleVariable.newGradleProperty(TestUtilSqlPg.VARNAME_CON_URI, TestUtilSqlPg.CON_URI)};
            TestUtil.runJob("jobs/Db2DbTaskDelTable", gvs);

            //reconnect to check results
            con = TestUtilSqlPg.connect();
            String countDestSql = String.format("SELECT count(*) FROM %s.albums_dest", schemaName);
            int countDest = TestUtilSqlPg.execCountQuery(con, countDestSql);

            Assert.assertEquals(
                    "Rowcount in table albums_src must be equal to rowcount in table albums_dest",
                    countSrc,
                    countDest);
        }
        finally {
            TestUtilSqlPg.closeCon(con);
        }
    }
    
    private int prepareDb2DbChainTables(Connection con, String schemaName) {
        int srcRowCount = 4;

        String ddlBase = "CREATE TABLE %s.albums_%s(" +
                "title text, artist text, release_date text," +
                "publisher text, media_type text)";

        try {
            //source table
            Statement s1 = con.createStatement();
            System.out.println(String.format(ddlBase, schemaName, "src"));
            s1.execute(String.format(ddlBase, schemaName, "src"));
            s1.close();

            //dest table
            Statement s2 = con.createStatement();
            s2.execute(String.format(ddlBase, schemaName,"dest"));
            s2.close();

            //intermediate table
            Statement s3 = con.createStatement();
            s3.execute(String.format(ddlBase, schemaName,"intermediate"));
            s3.close();

            insertRowsInAlbumsTable(con, schemaName, "src", 4);

            TestUtilSqlPg.grantDataModsInSchemaToUser(con, schemaName, TestUtilSqlPg.CON_DMLUSER);
        } catch(SQLException se){
            throw new RuntimeException(se);
        }

        return srcRowCount;
    }
    
    private void insertRowsInAlbumsTable(Connection con, String schemaName, String tableSuffix, int numRows) throws SQLException{
        PreparedStatement ps = con.prepareStatement(
                String.format("INSERT INTO %s.albums_%s VALUES (?,?,?,?,?)", schemaName, tableSuffix)
        );

        String[] row = {"Exodus", "Andy Hunter", "7/9/2002", "Sparrow Records", "CD"};
        for(int i=0; i<numRows; i++){
            for(int j=0; j<row.length; j++){
                ps.setString(j+1, row[j]);
            }
            ps.executeUpdate();
        }
        ps.close();
    }
    
    private static void assertEqualGeomInSourceAndTarget(Connection con, String schemaName, String tableName, String geomSrc) throws SQLException {
        Statement check = con.createStatement();
        ResultSet rs = check.executeQuery("SELECT ST_AsText(geom) AS geom_text FROM "+schemaName+"."+tableName);
        rs.next();
        String geomRes = rs.getString(1).trim().toUpperCase();

        Assert.assertEquals("The transferred geometry is not equal to the geometry in the source table", geomSrc, geomRes);
    }
}
