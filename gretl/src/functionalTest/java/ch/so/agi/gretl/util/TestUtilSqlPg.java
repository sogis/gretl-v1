package ch.so.agi.gretl.util;

import java.sql.*;

/**
 * Contains helper methods for the tests of the Db2DbTask and SqlExecutorTask
 */
public class TestUtilSqlPg {
    public static final String VARNAME_CON_URI = "gretltest_dburi_pg";
    public static final String CON_URI = System.getProperty(VARNAME_CON_URI);//"jdbc:postgresql://localhost:5432/gretl"

//    public static final String VARNAME_ORA_CON_URI = "gretltest_dburi_ora";
//    public static final String ORA_CON_URI = System.getProperty(VARNAME_ORA_CON_URI);//"jdbc:postgresql://localhost:5432/gretl"

    public static final String CON_DDLUSER = "ddluser";
    public static final String CON_DDLPASS = "ddluser";
    public static final String CON_DMLUSER = "dmluser";

    private static void dropSchema(String schemaName, Connection con) throws SQLException {
        if(con == null){ return; }

        Statement s = con.createStatement();
        s.execute(String.format("DROP SCHEMA %s CASCADE", schemaName));
    }

    public static void closeCon(Connection con){
        try {
            if(con != null)
                con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection connect(){
        Connection con = null;
        try {
            Driver pgDriver = (Driver)Class.forName("org.postgresql.Driver").newInstance();
            DriverManager.registerDriver(pgDriver);

            con = DriverManager.getConnection(
                    CON_URI,
                    CON_DDLUSER,
                    CON_DDLPASS);

            con.setAutoCommit(false);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }

        return con;
    }

    public static void createOrReplaceSchema(Connection con, String schemaName) {
        try {
            Statement s = con.createStatement();
            s.addBatch(String.format("DROP SCHEMA IF EXISTS %s CASCADE", schemaName));
            s.addBatch("CREATE SCHEMA " + schemaName);
            s.addBatch(String.format("GRANT USAGE ON SCHEMA %s TO dmluser", schemaName));
            s.addBatch(String.format("GRANT USAGE ON SCHEMA %s TO readeruser", schemaName));
            s.executeBatch();
            con.commit();
        }
        catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int execCountQuery(Connection con, String query) {
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
    /** grant data modification rights to all tables in given schema.
     * Data modification includes select, insert, update, delete.
     * @param con connection handle to db
     * @param schemaName name of schema in db
     * @param userName user to give rights to
     */
    public static void grantDataModsInSchemaToUser(Connection con, String schemaName, String userName) {

        String sql = String.format("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA %s TO %s", schemaName, userName);
        Statement s = null;
        try {
            s = con.createStatement();
            s.execute(sql);
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

    public static void createSqlExecuterTaskChainTables(Connection con, String schemaName) {

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

            TestUtilSqlPg.grantDataModsInSchemaToUser(con, schemaName, CON_DMLUSER);
        }
        catch(SQLException se){
            throw new RuntimeException(se);
        }
    }

    public static int prepareDb2DbChainTables(Connection con, String schemaName) {
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

            TestUtilSqlPg.grantDataModsInSchemaToUser(con, schemaName, CON_DMLUSER);
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
