package ch.so.agi.gretl.util;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

/** Utility-Class to open connections **/
public class DbConnector {

    private static HashMap<String, String> jdbcDriverClasses = null;

    private static Connection con=null;
    private static GretlLogger log = LogEnvironment.getLogger(DbConnector.class) ;

    static{
        jdbcDriverClasses = new HashMap<String, String>();
        jdbcDriverClasses.put("postgresql","org.postgresql.Driver");
        jdbcDriverClasses.put("sqlite","org.sqlite.JDBC");
        jdbcDriverClasses.put("derby","org.apache.derby.jdbc.EmbeddedDriver");
    }

    /**
     * Returns the connection to a specific database. The database is specified by the arguments ConnectionUrl,
     * UserName and Password.
     *
     * @param ConnectionUrl database specific JDBC-Connection-URL
     * @param UserName      database user
     * @param Password      password of given database user
     * @return              the connection to the specific database
     */
    public static Connection connect(String ConnectionUrl, String UserName, String Password) {
        try {

            String[] splits = ConnectionUrl.split(":");
            if (splits.length < 3)
                throw new IllegalArgumentException("Connection string is malformed: " + ConnectionUrl);

            String driverType = splits[1];
            //hash
            String driverClassName = jdbcDriverClasses.get(driverType);
            if(driverClassName == null)
                throw new IllegalArgumentException(
                        "Configuration error. ConnectionUrl contains unsupported driver type: " + driverType + "(" + ConnectionUrl + ")");


            Driver driver = null;

            try {
                driver = (Driver)Class.forName(driverClassName).newInstance();
            }
            catch(Exception e){
                throw new RuntimeException("Could not find and load jdbc Driver Class " + driverClassName, e);
            }

            DriverManager.registerDriver(driver);

            con = DriverManager.getConnection(
                    ConnectionUrl,UserName,Password);
            con.setAutoCommit(false);

            log.debug("DB connected with these Parameters:  ConnectionURL:" + ConnectionUrl +
                    " Username: " + UserName +
                    " Password: " + Password);

        } catch (SQLException e) {
            if (con!=null) {
                try {
                    con.rollback();
                    con.close();
                    con = null;
                } catch (SQLException f) {
                    log.info(f.toString());
                }
            }
            log.error("Could not connect to: " + ConnectionUrl, e);
            throw new RuntimeException("Could not connect to: " + ConnectionUrl, e);
        }
        return con;
    }
}
