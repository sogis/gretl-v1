package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.util.DbConnector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Class which is used for different tasks on the database
 */
public class TransactionContext  {

    private String dbUri;
    private String dbUser;
    private String dbPassword;
    private GretlLogger log;
    private Connection dbConnection = null;


    public TransactionContext(String dbUri, String dbUser, String dbPassword) {
        this.dbUri = dbUri;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    public Connection getDbConnection() {
        if (dbConnection == null) {
            dbConnection = DbConnector.connect(dbUri, dbUser, dbPassword);
        }
        return dbConnection;
    }

    //todo was ist der Mehrwert dieser Methode gegenüber einem einfachen Aufruf von dbConnection.close(); ?
    public void dbConnectionClose() {
        try {
            if (dbConnection != null) {
                dbConnection.close();
                dbConnection = null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

//todo mehrwert der methode?
    public void dbCommit() throws SQLException {
            if (dbConnection != null) {
                dbConnection.commit();
            }
    }

//todo mehrwert der methode?
    public void dbRollback() throws SQLException {
        if (dbConnection != null) {
            dbConnection.rollback();
        }
    }


}
