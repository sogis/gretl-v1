package ch.so.agi.gretl.steps;


import ch.so.agi.gretl.util.DbConnector;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Class which is used for different tasks on the database
 */
public class TransactionContext  {

    private String dbUri;
    private String dbUser;
    private String dbPassword;

    //KONSTRUKTOR
    public TransactionContext(String dbUri, String dbUser, String dbPassword) {
        this.dbUri = dbUri;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }
    //KONSTRUKTOR ENDE

    public Connection getDbConnection() {
        if (dbConnection == null) {
            dbConnection = DbConnector.connect(dbUri, dbUser, dbPassword);
        }
        return dbConnection;
    }

    private Connection dbConnection = null;

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

    public void dbCommit() throws SQLException {
            if (dbConnection != null) {
                dbConnection.commit();
            }
    }

    public void dbRollback() throws SQLException {
        if (dbConnection != null) {
            dbConnection.rollback();
        }
    }


}
