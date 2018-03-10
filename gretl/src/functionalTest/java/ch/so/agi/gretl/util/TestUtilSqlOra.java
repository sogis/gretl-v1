package ch.so.agi.gretl.util;

import java.sql.*;

/**
 * Contains helper methods for the tests of the Db2DbTask and SqlExecutorTask
 */
public class TestUtilSqlOra {
    public static final String VARNAME_CON_URI = "gretltest_dburi_ora";
    public static final String CON_URI = System.getProperty(VARNAME_CON_URI); //"jdbc:oracle:thin:@localhost:1521:xe"
}
