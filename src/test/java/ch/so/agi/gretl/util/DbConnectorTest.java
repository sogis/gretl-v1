package ch.so.agi.gretl.util;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;

/**
 * Test for the Class DbConnector
 */
public class DbConnectorTest {
    private GretlLogger log;
    private Connection con = null;


    public DbConnectorTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Test
    public void connectToDerbyDb() throws Exception {

        DbConnector x = new DbConnector();
        try {
            con =x.connect("jdbc:derby:memory:myInMemDB;create=true", null, null);
            if (con.isClosed()) {
                Assert.fail();}
        } catch (Exception e){
            throw new Exception("Could not connect to database");
        } finally {
            if (con!=null){
                con.close();
            }
        }
    }

    @Test
    public void connectionAutoCommit() throws Exception {

        DbConnector x = new DbConnector();
        try {
            con =x.connect("jdbc:derby:memory:myInMemDB;create=true", null, null);
            if (con.getAutoCommit()) {
                Assert.fail();
            }
        } catch (Exception e) {
            throw new Exception("Auto Commit on");
        }finally {
            if (con!=null){
                con.close();
            }
        }
    }
}