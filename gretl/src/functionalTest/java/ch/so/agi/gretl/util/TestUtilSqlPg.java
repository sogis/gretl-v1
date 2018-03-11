package ch.so.agi.gretl.util;

import java.sql.*;

/**
 * Contains helper methods for the tests of the Db2DbTask and SqlExecutorTask
 */
public class TestUtilSqlPg extends AbstractTestUtilSql {
    public static final String VARNAME_CON_URI = "gretltest_dburi_pg";
    public static final String CON_URI = System.getProperty(VARNAME_CON_URI); //"jdbc:postgresql://localhost:5432/gretl"
    
    public static final String CON_DDLUSER = "ddluser";
    public static final String CON_DDLPASS = "ddluser";
    public static final String CON_DMLUSER = "dmluser";

    private static void dropSchema(String schemaName, Connection con) throws SQLException {
        if(con == null){ return; }

        Statement s = con.createStatement();
        s.execute(String.format("DROP SCHEMA %s CASCADE", schemaName));
    }
    
    public static Connection connect() {
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
    
    /** Grant data modification rights to all tables in given schema.
     * Data modification includes select, insert, update, delete.
     * @param con connection handle to database
     * @param schemaName name of schema in database
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
}
