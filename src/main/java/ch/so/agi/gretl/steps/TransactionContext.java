package ch.so.agi.gretl.steps;


import ch.so.agi.gretl.util.DbConnector;


import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Class which is used get a connection to the database
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

    public Connection getDbConnection() throws SQLException {
        if (dbConnection == null) {
            dbConnection = DbConnector.connect(dbUri, dbUser, dbPassword);
            dbConnection.setAutoCommit(false);
        }
        return dbConnection;
    }



}
