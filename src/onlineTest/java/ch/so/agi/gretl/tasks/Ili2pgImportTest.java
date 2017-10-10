package ch.so.agi.gretl.tasks;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.text.RandomStringGenerator;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import static org.gradle.testkit.runner.TaskOutcome.*;
import static org.junit.Assert.*;

public class Ili2pgImportTest {
	
    public Ili2pgImportTest () {
        LogEnvironment.initStandalone();
        //this.log = LogEnvironment.getLogger(Ili2pgImportTest.class);
    }
    
    //private static GretlLogger log;
    private static Logger log = Logging.getLogger(Ili2pgImportTest.class);
    
    // Database name
    private static String dbName = null;
    
    @BeforeClass
    public static void setupDatabase() throws SQLException {
        String url = null; 
        Connection conn = null;
        Statement stmt =  null;
        
        try {
            // create database
            url = "jdbc:postgresql://localhost:5432/postgres?user=postgres";
            conn = DriverManager.getConnection(url);
            
            // Create a random database name since there will be more
            // than one database for testing.
            RandomStringGenerator generator = new RandomStringGenerator.Builder()
                    .withinRange('a', 'z').build();
            dbName = generator.generate(10);
            
            // create database
            stmt = conn.createStatement();
            stmt.execute("CREATE DATABASE " + dbName);       
            conn.close();
            
            url = "jdbc:postgresql://localhost:5432/"+dbName+"?user=postgres";
            conn = DriverManager.getConnection(url);
            
            // create extensions
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            stmt.execute("CREATE EXTENSION IF NOT EXISTS postgis;");
            stmt.execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";");
            conn.commit();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            if (conn != null) {
                conn.rollback();
            }  
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            conn.setAutoCommit(true);
            conn.close();
        }
    }
    
    @Test
    public void successfulImportTest() throws Exception {
        String dbUrl = "jdbc:postgresql://localhost:5432/"+dbName;
        String itfFile = new File("src/onlineTest/data/ch/so/agi/gretl/tasks/Ili2pgImportTest/ch_254900.itf").getAbsolutePath();
        int featureCount = 125; 
        
        // run tasks
        GradleRunner runner = GradleRunner.create()
                .withProjectDir(new File("src/onlineTest/data/ch/so/agi/gretl/tasks/Ili2pgImportTest/"))
                .withArguments("-PdbUrl="+dbUrl, "-PdbUser=postgres", "-PdbPass=", "-PitfFile="+itfFile, "importDataset")
                .withDebug(true);
                
        BuildResult result = runner.build();
        
        // check tasks result
        Connection conn = null;
        Statement stmt = null;
        int count = 0;
        try {
            conn = DriverManager.getConnection(dbUrl, "postgres", "");
                        
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT count(*) FROM ch_2549.boflaeche");       
            rs.next();
            count = (int) rs.getInt(1);
            System.out.println("*************");
            System.out.println(count);
            System.out.println("*************");

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
            log.error(e.getMessage()); 
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            conn.close();
        }

        assertEquals(featureCount, count);
        assertEquals(SUCCESS, result.task(":importDataset").getOutcome());
    }
    
    @Test
    public void dummyTest() throws Exception {    	
    		Assert.assertTrue("This will succeed.", true);
    }
    
    @Ignore
    @Test
    public void dummyTestFail() throws Exception {
    		Assert.assertTrue("This will not succeed.", false);
    }
}
