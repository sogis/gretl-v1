package ch.so.agi.gretl.util;

import java.sql.*;

/**
 * Contains helper methods for the test's of the Db2DbTask and SqlExecutorTask
 */
public class TestUtilSql {
    public static final String VARNAME_PG_CON_URI = "gretltest_dburi";
    public static final String PG_CON_URI = System.getProperty(VARNAME_PG_CON_URI);//"jdbc:postgresql://localhost:5432/gretl"

    public static final String PG_CON_USER = "ddluser";
    public static final String PG_CON_PASS = "ddluser";

    private static void dropSchema(String schemaName, Connection con) throws SQLException {
        if(con == null){ return; }

        Statement s = con.createStatement();
        s.execute(String.format("drop schema %s cascade", schemaName));
    }

    public static void closeCon(Connection con){
        try {
            if(con != null)
                con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection connectPG(){
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

    public static void createOrReplaceSchema(Connection con, String schemaName){

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

    public static int execCountQuery(Connection con, String query){
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

    public static void grantTableModsInSchemaToUser(Connection con, String schemaName, String userName){

        String sql = String.format("grant select, insert, update, delete on all tables in schema %s to %s", schemaName, userName);
        //grant permissions to dmluser on all objects in the schema
        Statement s = null;
        try {
            s = con.createStatement();
            s.execute(String.format("grant select, insert, update, delete on all tables in schema %s to dmluser", schemaName));
            s.close();
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
    }

    public static void createSqlExecuterTaskChainTables(Connection con, String schemaName){

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

    public static int prepareDb2DbChainTables(Connection con, String schemaName){
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

            //dest table
            Statement s2 = con.createStatement();
            s2.execute(String.format(ddlBase, schemaName,"dest"));
            s2.close();

            //intermediate table
            Statement s3 = con.createStatement();
            s3.execute(String.format(ddlBase, schemaName,"intermediate"));
            s3.close();

            insertRowsInAlbumsTable(con, schemaName, "src", 4);

            TestUtilSql.grantTableModsInSchemaToUser(con, schemaName, "dmlUser");
        }
        catch(SQLException se){
            throw new RuntimeException(se);
        }

        return srcRowCount;
    }


    public static void insertRowsInAlbumsTable(Connection con, String schemaName, String tableSuffix, int numRows) throws SQLException{
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
}
