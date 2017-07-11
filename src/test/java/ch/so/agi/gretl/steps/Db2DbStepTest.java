package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.util.DbConnector;
import ch.so.agi.gretl.util.EmptyFileException;
import ch.so.agi.gretl.util.NotAllowedSqlExpressionException;
import ch.so.agi.gretl.logging.Logger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by bjsvwsch on 03.05.17.
 */
public class Db2DbStepTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private String e;


    @Test
    public void PositiveTest() throws Exception {
        //unittest

        ////////////////////////////////////////
        //Test Vorbereitung ////////////////////
        ////////////////////////////////////////

        DbConnector dbConn = new DbConnector();
        Connection con = dbConn.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE colors ( " +
                "  rot integer, " +
                "  gruen integer, " +
                "  blau integer, " +
                "  farbname VARCHAR(200))");
        stmt.execute("INSERT INTO colors  VALUES (255,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (251,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (0,0,255,'blau')");
        stmt.execute("CREATE TABLE colors_copy (rot integer, gruen integer, blau integer, farbname VARCHAR(200))");

        File sqlFile =  folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write("SELECT * FROM colors");
        writer.close();


        ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
        mylist.add(new TransferSet(
                new Boolean(true),
                new File(sqlFile.getAbsolutePath().toString()),
                "colors_copy"
        ));


        DbConnector x = new DbConnector();
        Connection xcon = x.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        xcon.setAutoCommit(true);

        DbConnector y = new DbConnector();
        Connection ycon = y.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        ycon.setAutoCommit(true);

        ////////////////////////////
        // Test-Subjekt ////////////
        ////////////////////////////

        Db2DbStep db2db = new Db2DbStep(xcon, ycon);
        db2db.processAllTransferSets(mylist);


        //////////////////////////////
        // Verifikation /////////////
        /////////////////////////////

        ResultSet rs = stmt.executeQuery("SELECT * FROM colors_copy WHERE farbname = 'blau'");
        while(rs.next()) {
            if (!rs.getObject("rot").equals(0)) throw new Exception(e);
            if (!rs.getObject("farbname").equals("blau")) throw new Exception(e);
        }
        stmt.execute("DROP TABLE colors");
        stmt.execute("DROP TABLE colors_copy");
        con.close();
    }

    @Test
    public void NotAllowedSqlExpressionInScriptTest() throws Exception {
        //unittest
        DbConnector dbConn = new DbConnector();
        Connection con = dbConn.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE colors ( " +
                "  rot integer, " +
                "  gruen integer, " +
                "  blau integer, " +
                "  farbname VARCHAR(200))");
        stmt.execute("INSERT INTO colors  VALUES (255,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (251,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (0,0,255,'blau')");

        File sqlFile = folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write("DELETE FROM colors");
        writer.close();


        ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
        mylist.add(new TransferSet(
                new Boolean(false),
                new File(sqlFile.getAbsolutePath().toString()),
                "colors_copy"
        ));


        DbConnector x = new DbConnector();
        Connection xcon = x.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        xcon.setAutoCommit(true);

        DbConnector y = new DbConnector();
        Connection ycon = y.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        ycon.setAutoCommit(true);

        Db2DbStep db2db = new Db2DbStep(xcon, ycon);

        try {
            db2db.processAllTransferSets(mylist);
            Assert.fail();
        } catch (NotAllowedSqlExpressionException e) {

        } finally {
            xcon.close();
            ycon.close();
            stmt.execute("DROP TABLE colors");
            con.close();
        }

    }

    @Test
    public void Db2DbEmptyFileTest() throws Exception {
        //unittest
        DbConnector dbConn = new DbConnector();
        Connection con = dbConn.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE colors ( " +
                "  rot integer, " +
                "  gruen integer, " +
                "  blau integer, " +
                "  farbname VARCHAR(200))");
        stmt.execute("INSERT INTO colors  VALUES (255,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (251,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (0,0,255,'blau')");

        File sqlFile = folder.newFile("query.sql");

        ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
        mylist.add(new TransferSet(
                new Boolean(false),
                new File(sqlFile.getAbsolutePath().toString()),
                "colors_copy"
        ));


        DbConnector x = new DbConnector();
        Connection xcon = x.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        xcon.setAutoCommit(true);

        DbConnector y = new DbConnector();
        Connection ycon = y.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        ycon.setAutoCommit(true);

        Db2DbStep db2db = new Db2DbStep(xcon, ycon);

        try {
            db2db.processAllTransferSets(mylist);
            Assert.fail("EmptyFileException müsste geworfen werden");
        } catch (EmptyFileException e) {

        } finally {
            xcon.close();
            ycon.close();
            stmt.execute("DROP TABLE colors");
            con.close();
        }

    }

    @Test
    public void SQLExceptionTest() throws Exception {
        //unittest
        DbConnector dbConn = new DbConnector();
        Connection con = dbConn.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE colors ( " +
                "  rot integer, " +
                "  gruen integer, " +
                "  blau integer, " +
                "  farbname VARCHAR(200))");
        stmt.execute("INSERT INTO colors  VALUES (255,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (251,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (0,0,255,'blau')");

        File sqlFile = folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write("SELECT BLABLABLA FROM colors");
        writer.close();

        ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
        mylist.add(new TransferSet(
                new Boolean(false),
                new File(sqlFile.getAbsolutePath().toString()),
                "colors_copy"
        ));


        DbConnector x = new DbConnector();
        Connection xcon = x.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        xcon.setAutoCommit(true);

        DbConnector y = new DbConnector();
        Connection ycon = y.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        ycon.setAutoCommit(true);

        Db2DbStep db2db = new Db2DbStep(xcon, ycon);

        try {
            db2db.processAllTransferSets(mylist);
            Assert.fail("EmptyFileException müsste geworfen werden");
            Logger.log(Logger.INFO_LEVEL, "You should not be here!");
        } catch (SQLException e) {
            Logger.log(Logger.INFO_LEVEL,"Got SQLException. YEA!"+e.getMessage());
        } finally {
            Logger.log(Logger.INFO_LEVEL,"Finally, Ufff.... ");
            xcon.close();
            ycon.close();
            stmt.execute("DROP TABLE colors");
            con.close();
        }
    }

    //TEST SPALTENANZAHL //////////////
    // Sollte eine SQL-Exception geben.
    @Test
    public void ColumnNumberTest() throws Exception {
        //unittest
        DbConnector dbConn = new DbConnector();
        Connection con = dbConn.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE colors ( " +
                "  rot integer, " +
                "  gruen integer, " +
                "  blau integer, " +
                "  farbname VARCHAR(200))");
        stmt.execute("INSERT INTO colors  VALUES (255,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (251,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (0,0,255,'blau')");

        stmt.execute("CREATE TABLE colors_copy (rot integer, gruen integer)");

        File sqlFile = folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write("SELECT rot, gruen, blau, farbname FROM colors");
        writer.close();

        ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
        mylist.add(new TransferSet(
                new Boolean(false),
                new File(sqlFile.getAbsolutePath().toString()),
                "colors_copy"
        ));


        DbConnector x = new DbConnector();
        Connection xcon = x.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        xcon.setAutoCommit(true);

        DbConnector y = new DbConnector();
        Connection ycon = y.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        ycon.setAutoCommit(true);

        Db2DbStep db2db = new Db2DbStep(xcon, ycon);

        try {
            db2db.processAllTransferSets(mylist);
            Assert.fail("Eine Exception müsste geworfen werden. ");
            Logger.log(Logger.INFO_LEVEL, "You should not be here!");
        } catch (SQLException e) {
            Logger.log(Logger.INFO_LEVEL,"Got SQLException. YEA!"+e.getMessage());
        } finally {
            Logger.log(Logger.INFO_LEVEL,"Finally, Ufff.... ");
            xcon.close();
            ycon.close();
            stmt.execute("DROP TABLE colors");
            con.close();
        }
    }
    //TEST Inkompatible Datentypen (String in int insert) ////////////////

    // Sollte eine SQL-Exception geben.
    @Test
    public void IncompatibleDataTypeTest() throws Exception {
        //unittest
        DbConnector dbConn = new DbConnector();
        Connection con = dbConn.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE colors ( " +
                "  rot integer, " +
                "  gruen integer, " +
                "  blau integer, " +
                "  farbname VARCHAR(200))");
        stmt.execute("INSERT INTO colors  VALUES (255,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (251,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (0,0,255,'blau')");

        stmt.execute("CREATE TABLE colors_copy (rot integer, gruen integer, blau integer, farbname integer)");

        File sqlFile = folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write("SELECT * FROM colors");
        writer.close();

        ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
        mylist.add(new TransferSet(
                new Boolean(false),
                new File(sqlFile.getAbsolutePath().toString()),
                "colors_copy"
        ));


        DbConnector x = new DbConnector();
        Connection xcon = x.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        xcon.setAutoCommit(true);

        DbConnector y = new DbConnector();
        Connection ycon = y.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        ycon.setAutoCommit(true);

        Db2DbStep db2db = new Db2DbStep(xcon, ycon);

        try {
            db2db.processAllTransferSets(mylist);
            Assert.fail("Eine Exception müsste geworfen werden. ");
            Logger.log(Logger.INFO_LEVEL, "You should not be here!");
        } catch (SQLException e) {
            Logger.log(Logger.INFO_LEVEL,"Got SQLException. YEA!"+e.getMessage());
        } finally {
            Logger.log(Logger.INFO_LEVEL,"Finally, Ufff.... ");
            xcon.close();
            ycon.close();
            stmt.execute("DROP TABLE colors");
            con.close();
        }
    }
    //TEST No Exception for empty Table //////////////////////////////

    @Test
    public void EmptyTableTest() throws Exception {
        //unittest
        DbConnector dbConn = new DbConnector();
        Connection con = dbConn.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE colors ( " +
                "  rot integer, " +
                "  gruen integer, " +
                "  blau integer, " +
                "  farbname VARCHAR(200))");

        stmt.execute("CREATE TABLE colors_copy (rot integer, gruen integer, blau integer, farbname integer)");

        File sqlFile = folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write("SELECT * FROM colors");
        writer.close();

        ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
        mylist.add(new TransferSet(
                new Boolean(false),
                new File(sqlFile.getAbsolutePath().toString()),
                "colors_copy"
        ));


        DbConnector x = new DbConnector();
        Connection xcon = x.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        xcon.setAutoCommit(true);

        DbConnector y = new DbConnector();
        Connection ycon = y.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        ycon.setAutoCommit(true);

        Db2DbStep db2db = new Db2DbStep(xcon, ycon);

        try {
            db2db.processAllTransferSets(mylist);
            Logger.log(Logger.INFO_LEVEL, "EmptyTableTest is fine");
        } catch (SQLException e) {
            Logger.log(Logger.INFO_LEVEL,"Got SQLException. "+e.getMessage());
            throw new Exception(e);
        } finally {
            Logger.log(Logger.INFO_LEVEL,"Finally, Ufff.... ");
            xcon.close();
            ycon.close();
            stmt.execute("DROP TABLE colors");
            con.close();
        }
    }
    //TEST SourceColumn Value is NULL throws no Exception ////////////

    @Test
    public void NullSourceValueTest() throws Exception {
        //unittest
        DbConnector dbConn = new DbConnector();
        Connection con = dbConn.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE colors ( " +
                "  rot integer, " +
                "  gruen integer, " +
                "  blau integer, " +
                "  farbname VARCHAR(200))");
        stmt.execute("INSERT INTO colors  VALUES (NULL,NULL,NULL,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (251,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (0,0,255,NULL)");

        stmt.execute("CREATE TABLE colors_copy (rot integer, gruen integer, blau integer, farbname VARCHAR(200))");

        File sqlFile = folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write("SELECT * FROM colors");
        writer.close();

        ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
        mylist.add(new TransferSet(
                new Boolean(false),
                new File(sqlFile.getAbsolutePath().toString()),
                "colors_copy"
        ));


        DbConnector x = new DbConnector();
        Connection xcon = x.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        xcon.setAutoCommit(true);

        DbConnector y = new DbConnector();
        Connection ycon = y.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        ycon.setAutoCommit(true);

        Db2DbStep db2db = new Db2DbStep(xcon, ycon);

        try {
            db2db.processAllTransferSets(mylist);
            Logger.log(Logger.INFO_LEVEL, "NULL-Value Test succeeded");
        } catch (SQLException e) {
            Logger.log(Logger.INFO_LEVEL,"Got SQLException. "+e.getMessage());
            throw new Exception(e);
        } finally {
            Logger.log(Logger.INFO_LEVEL,"Finally, Ufff.... ");
            xcon.close();
            ycon.close();
            stmt.execute("DROP TABLE colors");
            con.close();
        }
    }
    //TEST if Delete works ///////////////////////////////////////////

    @Test
    public void DeleteTest() throws Exception {
        //unittest
        DbConnector dbConn = new DbConnector();
        Connection con = dbConn.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE colors ( " +
                "  rot integer, " +
                "  gruen integer, " +
                "  blau integer, " +
                "  farbname VARCHAR(200))");
        stmt.execute("INSERT INTO colors  VALUES (243,123,235,'blau')");
        stmt.execute("INSERT INTO colors  VALUES (251,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (0,0,255,NULL)");

        stmt.execute("CREATE TABLE colors_copy (rot integer, gruen integer, blau integer, farbname VARCHAR(200))");

        File sqlFile = folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write("SELECT * FROM colors WHERE farbname = 'rot'");
        writer.close();

        ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
        mylist.add(new TransferSet(
                new Boolean(true),
                new File(sqlFile.getAbsolutePath().toString()),
                "colors_copy"
        ));


        DbConnector x = new DbConnector();
        Connection xcon = x.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        xcon.setAutoCommit(true);

        DbConnector y = new DbConnector();
        Connection ycon = y.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        ycon.setAutoCommit(true);

        Db2DbStep db2db = new Db2DbStep(xcon, ycon);

        try {
            db2db.processAllTransferSets(mylist);
            ResultSet rs = stmt.executeQuery("SELECT * FROM colors_copy");
            //////////////////////////////
            // Verifikation /////////////
            /////////////////////////////
            int count = 0;
            while(rs.next()) {
                ++count;
            }
            if(count > 1) {
                throw new Exception();
            }
        } finally {
            Logger.log(Logger.INFO_LEVEL,"Finally, Ufff.... ");
            xcon.close();
            ycon.close();
            stmt.execute("DROP TABLE colors");
            con.close();
        }
    }
    //TEST with ORACLE and PostgreSQL ////////////////////////////////

    //ACHTUNG: Dieser Test läuft nicht ohne weiteres! Es braucht manuelle Anpassungen in der DB-Konfiguration!

    @Test
    public void PostgresqlDerbyTest() throws Exception {
        //unittest
        DbConnector dbConn = new DbConnector();
        Connection con = dbConn.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE colors ( " +
                "  rot integer, " +
                "  gruen integer, " +
                "  blau integer, " +
                "  farbname VARCHAR(200))");
        stmt.execute("INSERT INTO colors  VALUES (342,123,222,'blau')");
        stmt.execute("INSERT INTO colors  VALUES (251,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (0,0,255,NULL)");

        Connection con2 = dbConn.connect("jdbc:postgresql://10.36.54.200:54321/sogis", "bjsvwsch", null);
        con2.setAutoCommit(true);
        Statement stmt2 = con2.createStatement();

        stmt2.execute("CREATE TABLE IF NOT EXISTS colors_copy (rot integer, gruen integer, blau integer, farbname VARCHAR(200))");

        File sqlFile = folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write("SELECT * FROM colors");
        writer.close();

        ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
        mylist.add(new TransferSet(
                new Boolean(true),
                new File(sqlFile.getAbsolutePath().toString()),
                "colors_copy"
        ));


        DbConnector x = new DbConnector();
        Connection xcon = x.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        xcon.setAutoCommit(true);

        DbConnector y = new DbConnector();
        Connection ycon = y.connect("jdbc:postgresql://10.36.54.200:54321/sogis", "bjsvwsch", null);
        ycon.setAutoCommit(true);

        Db2DbStep db2db = new Db2DbStep(xcon, ycon);

        try {
            db2db.processAllTransferSets(mylist);
            Logger.log(Logger.INFO_LEVEL, "DerbyToPostgreSQL succeeded");
        } catch (SQLException e) {
            Logger.log(Logger.INFO_LEVEL,"Got SQLException. "+e.getMessage());
            throw new Exception(e);
        } finally {
            Logger.log(Logger.INFO_LEVEL,"Finally, Ufff.... ");
            xcon.close();
            ycon.close();
            stmt.execute("DROP TABLE colors");
            con.close();
        }
    }

    //TEST if Connections close at the end of the process

    @Test
    public void CloseConnectionsTest() throws Exception {

        ////////////////////////////////////////
        //Test Vorbereitung ////////////////////
        ////////////////////////////////////////

        DbConnector dbConn = new DbConnector();
        Connection con = dbConn.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE colors ( " +
                "  rot integer, " +
                "  gruen integer, " +
                "  blau integer, " +
                "  farbname VARCHAR(200))");
        stmt.execute("INSERT INTO colors  VALUES (255,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (251,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (0,0,255,'blau')");
        stmt.execute("CREATE TABLE colors_copy (rot integer, gruen integer, blau integer, farbname VARCHAR(200))");

        File sqlFile =  folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write("SELECT * FROM colors");
        writer.close();


        ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
        mylist.add(new TransferSet(
                new Boolean(true),
                new File(sqlFile.getAbsolutePath().toString()),
                "colors_copy"
        ));


        DbConnector x = new DbConnector();
        Connection xcon = x.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        xcon.setAutoCommit(true);

        DbConnector y = new DbConnector();
        Connection ycon = y.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        ycon.setAutoCommit(true);


        Db2DbStep db2db = new Db2DbStep(xcon, ycon);
        db2db.processAllTransferSets(mylist);


        //////////////////////////////
        // Verifikation /////////////
        /////////////////////////////

        if ((!xcon.isClosed())||(!ycon.isClosed())) {
            throw new ConnectException(e) {
            };
        }
    }

    //TEST if Connections close at the end of a failed process

    @Test
    public void CloseConnectionsAfterFailedTest() throws Exception {

        ////////////////////////////////////////
        //Test Vorbereitung ////////////////////
        ////////////////////////////////////////

        DbConnector dbConn = new DbConnector();
        Connection con = dbConn.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE colors ( " +
                "  rot integer, " +
                "  gruen integer, " +
                "  blau integer, " +
                "  farbname VARCHAR(200))");
        stmt.execute("INSERT INTO colors  VALUES (255,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (251,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (0,0,255,'blau')");
        stmt.execute("CREATE TABLE colors_copy (rot integer, gruen integer, blau integer, farbname VARCHAR(200))");

        File sqlFile =  folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write("SELECT güggeliblau FROM colors");
        writer.close();


        ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
        mylist.add(new TransferSet(
                new Boolean(true),
                new File(sqlFile.getAbsolutePath().toString()),
                "colors_copy"
        ));


        DbConnector x = new DbConnector();
        Connection xcon = x.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        xcon.setAutoCommit(true);

        DbConnector y = new DbConnector();
        Connection ycon = y.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        ycon.setAutoCommit(true);


        Db2DbStep db2db = new Db2DbStep(xcon, ycon);
        try {
            db2db.processAllTransferSets(mylist);
        } catch (SQLException e) {
            Logger.log(Logger.INFO_LEVEL,"Got a SQL Exception as expected");
            //System.out.println("Got a SQL Exception s expected");
        }

        //////////////////////////////
        // Verifikation /////////////
        /////////////////////////////

        if ((!xcon.isClosed())||(!ycon.isClosed())) {
            throw new ConnectException(e) {
            };
        }
    }

}