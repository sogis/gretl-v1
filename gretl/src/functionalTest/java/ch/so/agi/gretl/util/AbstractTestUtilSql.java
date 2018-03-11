package ch.so.agi.gretl.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class AbstractTestUtilSql {
    public static void closeCon(Connection con){
        try {
            if(con != null)
                con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static int execCountQuery(Connection con, String query) {
        Statement s = null;
        int count = -1;
        try {
            s = con.createStatement();
            ResultSet rs = s.executeQuery(query);
            rs.next();
            count = rs.getInt(1);

            if(count == -1)
                throw new RuntimeException(String.format("Query [%s] did not return valid row count",query));
        } catch (SQLException se){
            throw new RuntimeException(se);
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return count;
    }
}
