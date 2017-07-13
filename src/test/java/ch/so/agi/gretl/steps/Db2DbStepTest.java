package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.DbConnector;
import ch.so.agi.gretl.util.EmptyFileException;
import ch.so.agi.gretl.util.NotAllowedSqlExpressionException;
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

    //Konstruktor//
    public Db2DbStepTest () {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
    }


    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private String e;
    private GretlLogger log;


    @Test
    public void FaultFreeExecutionTest() throws Exception {
        //unittest

        ////////////////////////////////////////
        //Test Vorbereitung ////////////////////
        ////////////////////////////////////////

        DbConnector dbConn = new DbConnector();
        Connection con = DbConnector.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
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
                sqlFile.getAbsolutePath(),
                "colors_copy"
        ));


        TransactionContext sourceDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        TransactionContext targetDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        ////////////////////////////
        // Test-Subjekt ////////////
        ////////////////////////////

        Db2DbStep db2db = new Db2DbStep();
        db2db.processAllTransferSets(sourceDb, targetDb, mylist);


        //////////////////////////////
        // Verifikation /////////////
        /////////////////////////////

        ResultSet rs = stmt.executeQuery("SELECT * FROM colors_copy WHERE farbname = 'blau'");
        while(rs.next()) {
            if (!rs.getObject("rot").equals(0)) throw new Exception(e);
            if (!rs.getObject("farbname").equals("blau")) throw new Exception(e);
        }
        log.info("YEA. Ich hab's geschaaaaft");
        stmt.execute("DROP TABLE colors");
        stmt.execute("DROP TABLE colors_copy");
        con.close();
    }

    @Test
    public void NotAllowedSqlExpressionInScriptTest() throws Exception {
        //unittest
        DbConnector dbConn = new DbConnector();
        Connection con = DbConnector.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
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
                sqlFile.getAbsolutePath(),
                "colors_copy"
        ));


        TransactionContext sourceDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        TransactionContext targetDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

        Db2DbStep db2db = new Db2DbStep();

        try {
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            Assert.fail();
        } catch (NotAllowedSqlExpressionException e) {

        } finally {
            stmt.execute("DROP TABLE colors");
            con.close();
        }

    }

    @Test
    public void Db2DbEmptyFileTest() throws Exception {
        //unittest
        DbConnector dbConn = new DbConnector();
        Connection con = DbConnector.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
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
                sqlFile.getAbsolutePath(),
                "colors_copy"
        ));


        TransactionContext sourceDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        TransactionContext targetDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

        Db2DbStep db2db = new Db2DbStep();

        try {
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            Assert.fail("EmptyFileException müsste geworfen werden");
        } catch (EmptyFileException e) {

        } finally {
            stmt.execute("DROP TABLE colors");
            con.close();
        }

    }

    @Test
    public void SQLExceptionTest() throws Exception {
        //unittest
        DbConnector dbConn = new DbConnector();
        Connection con = DbConnector.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
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
                sqlFile.getAbsolutePath(),
                "colors_copy"
        ));


        TransactionContext sourceDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        TransactionContext targetDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

        Db2DbStep db2db = new Db2DbStep();

        try {
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            Assert.fail("EmptyFileException müsste geworfen werden");
            log.info("You should not be here!");
        } catch (SQLException e) {
            log.info("Got SQLException. YEA!"+e.getMessage());
        } finally {
            log.info("Finally, Ufff.... ");
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
        Connection con = DbConnector.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
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
                sqlFile.getAbsolutePath(),
                "colors_copy"
        ));


        TransactionContext sourceDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        TransactionContext targetDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

        Db2DbStep db2db = new Db2DbStep();

        try {
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            Assert.fail("Eine Exception müsste geworfen werden. ");
            log.info("You should not be here!");
        } catch (SQLException e) {
            log.info("Got SQLException. YEA!"+e.getMessage());
        } finally {
            log.info("Finally, Ufff.... ");
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
        Connection con = DbConnector.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
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
                sqlFile.getAbsolutePath(),
                "colors_copy"
        ));

        TransactionContext sourceDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        TransactionContext targetDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

        Db2DbStep db2db = new Db2DbStep();

        try {
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            Assert.fail("Eine Exception müsste geworfen werden. ");
            log.info("You should not be here!");
        } catch (SQLException e) {
            log.info("Got SQLException. YEA!"+e.getMessage());
        } finally {
            log.info("Finally, Ufff.... ");
            stmt.execute("DROP TABLE colors");
            con.close();
        }
    }
    //TEST No Exception for empty Table //////////////////////////////

    @Test
    public void EmptyTableTest() throws Exception {
        //unittest
        DbConnector dbConn = new DbConnector();
        Connection con = DbConnector.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
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
                sqlFile.getAbsolutePath(),
                "colors_copy"
        ));


        TransactionContext sourceDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        TransactionContext targetDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

        Db2DbStep db2db = new Db2DbStep();

        try {
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            log.info("EmptyTableTest is fine");
        } catch (SQLException e) {
            log.info("Got SQLException. "+e.getMessage());
            throw new Exception(e);
        } finally {
            log.info("Finally, Ufff.... ");
            stmt.execute("DROP TABLE colors");
            con.close();
        }
    }
    //TEST SourceColumn Value is NULL throws no Exception ////////////

    @Test
    public void NullSourceValueTest() throws Exception {
        //unittest
        DbConnector dbConn = new DbConnector();
        Connection con = DbConnector.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
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
                sqlFile.getAbsolutePath(),
                "colors_copy"
        ));

        TransactionContext sourceDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        TransactionContext targetDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

        Db2DbStep db2db = new Db2DbStep();

        try {
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            log.info("NULL-Value Test succeeded");
        } catch (SQLException e) {
            log.info("Got SQLException. "+e.getMessage());
            throw new Exception(e);
        } finally {
            log.info("Finally, Ufff.... ");
            stmt.execute("DROP TABLE colors");
            con.close();
        }
    }
    //TEST if Delete works ///////////////////////////////////////////

    @Test
    public void DeleteTest() throws Exception {
        //unittest
        DbConnector dbConn = new DbConnector();
        Connection con = DbConnector.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
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
                sqlFile.getAbsolutePath(),
                "colors_copy"
        ));


        TransactionContext sourceDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        TransactionContext targetDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

        Db2DbStep db2db = new Db2DbStep();

        try {
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
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
            log.info("Finally, Ufff.... ");
            stmt.execute("DROP TABLE colors");
            con.close();
        }
    }
    //TEST with ORACLE and PostgreSQL ////////////////////////////////

    //MUSS NOCH ÜBERDACHT WERDEN. SOLLTE JA AUTOMATISCH DURCHLAUFEN!

    //TEST if Connections close at the end of the process

    @Test
    public void CloseConnectionsTest() throws Exception {

        ////////////////////////////////////////
        //Test Vorbereitung ////////////////////
        ////////////////////////////////////////

        DbConnector dbConn = new DbConnector();
        Connection con = DbConnector.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
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
                sqlFile.getAbsolutePath(),
                "colors_copy"
        ));


        TransactionContext sourceDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        TransactionContext targetDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);


        Db2DbStep db2db = new Db2DbStep();
        db2db.processAllTransferSets(sourceDb, targetDb, mylist);


        //////////////////////////////
        // Verifikation /////////////
        /////////////////////////////

        if ((!sourceDb.getDbConnection().isClosed())||(!targetDb.getDbConnection().isClosed())) {
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
        Connection con = DbConnector.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
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
                sqlFile.getAbsolutePath(),
                "colors_copy"
        ));


        TransactionContext sourceDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        TransactionContext targetDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);


        Db2DbStep db2db = new Db2DbStep();
        try {
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
        } catch (SQLException e) {
            log.info("Got a SQL Exception as expected");
            //System.out.println("Got a SQL Exception s expected");
        }

        //////////////////////////////
        // Verifikation /////////////
        /////////////////////////////

        if ((!sourceDb.getDbConnection().isClosed())||(!targetDb.getDbConnection().isClosed())) {
            throw new ConnectException(e) {
            };
        }
    }

}