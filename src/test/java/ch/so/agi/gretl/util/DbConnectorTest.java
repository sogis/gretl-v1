package ch.so.agi.gretl.util;

import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;

/**
 * Test for the Class DbConnector
 */
public class DbConnectorTest {

    @Test
    public void connectToDerbyDb() throws Exception {
        DbConnector x = new DbConnector();
        Connection con =x.connect("jdbc:derby:memory:myInMemDB;create=true", null, null);
        if (con.isClosed()) {
            Assert.fail();
            con.close();
            throw new Exception("Could not connect to database");
        }
    }

    @Test
    public void connectionAutoCommit() throws Exception {
        DbConnector x = new DbConnector();
        Connection con =x.connect("jdbc:derby:memory:myInMemDB;create=true", null, null);
        if (con.getAutoCommit()) {
            Assert.fail();
            con.close();
            throw new Exception("Auto Commit on");
        }
    }
}