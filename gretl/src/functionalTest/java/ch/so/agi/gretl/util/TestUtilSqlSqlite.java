package ch.so.agi.gretl.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestUtilSqlSqlite extends AbstractTestUtilSql {
    public static Connection connect(File dbLocation) {
        Connection con = null;
        try {
        		String url = "jdbc:sqlite:" + dbLocation.getAbsolutePath();
        	    con = DriverManager.getConnection(url);
            con.setAutoCommit(false);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }

        return con;
    }
}