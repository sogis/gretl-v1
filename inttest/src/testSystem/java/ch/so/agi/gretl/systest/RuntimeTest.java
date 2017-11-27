package ch.so.agi.gretl.systest;


import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;


import java.sql.*;

/**
 * Contains integration tests that:
 * - confirm that the runtime deployment is sane
 * - the tasks work well together in gradle
 * - currently (
 */
public class RuntimeTest {

    private static final String PG_CON_URI = "jdbc:postgresql://localhost:5432/automatedtest";
    private static final String PG_CON_USER = "ddluser";
    private static final String PG_CON_PASS = "ddluser";


    /*
        Test's that a chain of statements executes properly and results in the correct
        number of inserts (corresponding to the last statement)
        1. statement: create the schema for the tables
        2. statement: fill the source table with rows
        3. statement: execute the "insert into select from" statement
     */
    @Ignore
    @Test
    public void sqlExecuterTaskChainTest() throws Exception {
        String schemaName = "sqlExecuterTaskChain".toUpperCase();
        Connection con = null;
        try{
            con = connectPG();
            createOrReplaceSchema(con, schemaName);
            con.commit();
            closeCon(con);

            runJob(schemaName);

            //reconnect to check results
            con = connectPG();

            String countSrcSql = String.format("select count(*) from %s.albums_src", schemaName);
            String countDestSql = String.format("select count(*) from %s.albums_dest", schemaName);

            int countSrc = execCountQuery(con, countSrcSql);
            int countDest = execCountQuery(con, countDestSql);

            Assert.assertEquals("Rowcount in destination table must be equal to rowcount in source table", countSrc, countDest);
            Assert.assertTrue("Rowcount in destination table must be greater than zero", countDest > 0);
        }
        finally {
            closeCon(con);
        }
    }

    /*
    Test's that a chain of statements executes properly and results in the correct
    number of inserts (corresponding to the last statement)
        1. Statement transfers rows from a to b
        2. Statement transfers rows from b to a
    */
    @Ignore
    @Test
    public void db2dbTaskChainTest() throws Exception {
        String schemaName = "db2dbTaskChain".toUpperCase();
        Connection con = null;
        try{
            con = connectPG();
            createOrReplaceSchema(con, schemaName);
            int countSrc = prepareDb2DbChainTables(con, schemaName);
            con.commit();
            closeCon(con);

            runJob(schemaName);

            //reconnect to check results
            con = connectPG();
            String countDestSql = String.format("select count(*) from %s.albums_dest", schemaName);
            int countDest = execCountQuery(con, countDestSql);

            Assert.assertEquals(
                    "Rowcount in table albums_src must be equal to rowcount in table albums_dest",
                    countSrc,
                    countDest);
        }
        finally {
            closeCon(con);
        }
    }

    private static final int prepareDb2DbChainTables(Connection con, String schemaName){
        int srcRowCount = 4;


        String ddlBase = "CREATE TABLE %s.albums_%s(" +
                "title text, artist text, release_date text," +
                "publisher text, media_type text)";

        try{
            //source table
            Statement s1 = con.createStatement();
            System.out.println(String.format(ddlBase, schemaName, "src"));
            s1.execute(String.format(ddlBase, schemaName, "src"));
            s1.close();

            PreparedStatement ps = con.prepareStatement(
                    String.format("INSERT INTO %s.albums_src VALUES (?,?,?,?,?)", schemaName)
            );

            String[] row = {"Exodus", "Andy Hunter", "7/9/2002", "Sparrow Records", "CD"};
            for(int i=0; i<srcRowCount; i++){
                for(int j=0; j<row.length; j++){
                    ps.setString(j+1, row[j]);
                }
                ps.executeUpdate();
            }
            ps.close();

            //dest table
            Statement s2 = con.createStatement();
            s2.execute(String.format(ddlBase, schemaName,"dest"));
            s2.close();

            //intermediate table
            Statement s3 = con.createStatement();
            s3.execute(String.format(ddlBase, schemaName,"intermediate"));
            s3.close();
        }
        catch(SQLException se){
            throw new RuntimeException(se);
        }

        return srcRowCount;
    }

    private static int execCountQuery(Connection con, String query){
        Statement s = null;
        int count = -1;
        try{
            s = con.createStatement();
            ResultSet rs = s.executeQuery(query);
            rs.next();
            count = rs.getInt(1);

            if(count == -1)
                throw new RuntimeException(String.format("Query [%s] did not return valid row count",query));
        }
        catch (SQLException se){
            throw new RuntimeException(se);
        }
        finally {
            if(s != null){
                try{
                    s.close();
                }
                catch(Exception e){
                    throw new RuntimeException(e);
                }
            }
        }
        return count;
    }

    private static void runJob(String jobName){
        //todo anstossen des jobs mittels jenkins api. Soll exception werfen falls die jobdurchführung fehlerhaft war...
    }

    private static void dropSchema(String schemaName, Connection con) throws SQLException {
        if(con == null){ return; }

        Statement s = con.createStatement();
        s.execute(String.format("drop schema %s cascade", schemaName));
    }

    private static void closeCon(Connection con){
        try {
            if(con != null)
                con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Connection connectPG(){
        Connection con = null;
        try {
            Driver pgDriver = (Driver)Class.forName("org.postgresql.Driver").newInstance();
            DriverManager.registerDriver(pgDriver);

            con = DriverManager.getConnection(
                    PG_CON_URI,
                    PG_CON_USER,
                    PG_CON_PASS);

            con.setAutoCommit(false);
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }

        return con;
    }

    private static void createOrReplaceSchema(Connection con, String schemaName){

        try {
            Statement s = con.createStatement();
            s.addBatch(String.format("drop schema if exists %s cascade", schemaName));
            s.addBatch("create schema " + schemaName);
            s.addBatch(String.format("grant usage on schema %s to dmluser", schemaName));
            s.addBatch(String.format("grant usage on schema %s to readeruser", schemaName));
            s.executeBatch();
            con.commit();
        }
        catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
