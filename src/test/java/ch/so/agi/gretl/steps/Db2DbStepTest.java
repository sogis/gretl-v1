package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.DbConnector;
import ch.so.agi.gretl.util.EmptyFileException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Created by bjsvwsch on 03.05.17.
 */
public class Db2DbStepTest {

    //Konstruktor//
    public Db2DbStepTest () {
        LogEnvironment.initStandalone(Level.ALL);
        this.log = LogEnvironment.getLogger(this.getClass());
    }


    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private String e;
    private GretlLogger log;

    @After
    public void finalise() throws Exception {
        TransactionContext sourceDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        clearTestDb(sourceDb);
    }


    @Test
    public void FaultFreeExecutionTest() throws Exception {

        TransactionContext con = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        log.debug("Debug Message");

        File sqlFile = createFile("SELECT * FROM colors; ");

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


        ResultSet rs = con.getDbConnection().createStatement().executeQuery("SELECT * FROM colors_copy WHERE farbname = 'blau'");
        while(rs.next()) {
            if (!rs.getObject("rot").equals(0)) throw new Exception(e);
            if (!rs.getObject("farbname").equals("blau")) throw new Exception(e);
        }
        con.getDbConnection().close();
    }

    @Test
    public void Db2DbEmptyFileTest() throws Exception {
        TransactionContext con = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);

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

        } catch (Exception e) {

        } finally {
            con.getDbConnection().close();
        }

    }

    @Test
    public void SQLExceptionTest() throws Exception {
        TransactionContext con = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);

        File sqlFile = createFile("SELECT BLABLABLA FROM colors");

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
        } catch (SQLException e) {

        } catch (Exception e) {

        } finally{
            con.getDbConnection().close();
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

        File sqlFile = createFile("SELECT rot, gruen, blau, farbname FROM colors");

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
        } catch (SQLException e) {

        } catch (Exception e) {

        } finally {
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

        File sqlFile = createFile("SELECT * FROM colors");

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
        } catch (SQLException e) {

        } catch (Exception e) {

        } finally {
            con.close();
        }
    }
    //TEST No Exception for empty Table //////////////////////////////

    @Test
    public void EmptyTableTest() throws Exception {
        TransactionContext con = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTableInTestDb(con.getDbConnection());

        File sqlFile = createFile("SELECT * FROM colors");

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
        } catch (SQLException e) {
            throw new Exception(e);
        } finally {
            con.getDbConnection().close();
        }
    }
    //TEST SourceColumn Value is NULL throws no Exception ////////////

    @Test
    public void NullSourceValueTest() throws Exception {

        TransactionContext con = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);

        File sqlFile = createFile("SELECT * FROM colors");

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
        } catch (SQLException e) {
            throw new Exception(e);
        } finally {
            con.getDbConnection().close();
        }
    }
    //TEST if Delete works ///////////////////////////////////////////

    @Test
    public void DeleteTest() throws Exception {
        //unittest
        TransactionContext con = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);

        File sqlFile = createFile("SELECT rot, gruen, blau, farbname FROM (SELECT ROW_NUMBER() OVER() AS rownum, colors.* FROM colors) AS tmp WHERE rownum <= 1;");

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
            ResultSet rs = con.getDbConnection().createStatement().executeQuery("SELECT * FROM colors_copy");

            int count = 0;
            while(rs.next()) {
                ++count;
            }
            if(count > 1) {
                log.info("Got "+count+" rows! Very sad!");
                throw new Exception();
            }
        } finally {
            con.getDbConnection().close();
        }
    }
    //TEST with ORACLE and PostgreSQL ////////////////////////////////

    //TEST MUSS evtl. NOCH GESCHRIEBEN WERDEN....

    //TEST if Connections close at the end of the process

    @Test
    public void CloseConnectionsTest() throws Exception {

        TransactionContext con = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);

        File sqlFile = createFile("SELECT * FROM colors");

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

        if ((!sourceDb.getDbConnection().isClosed())||(!targetDb.getDbConnection().isClosed())) {
            throw new ConnectException(e) { //todo ConnectException ist auf kontext nicht zutreffend - besser zwei asserts verwenden für src und targetconnection
            };
        }
        con.getDbConnection().close();
    }

    //TEST if Connections close at the end of a failed process

    @Test
    public void CloseConnectionsAfterFailedTest() throws Exception {

        TransactionContext con = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);

        File sqlFile = createFile("SELECT güggeliblau FROM colors_copy");

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
            db2db.processAllTransferSets(sourceDb,targetDb,mylist);
        } catch (SQLException e) {

        } catch (Exception e) {

        }

        if ((!sourceDb.getDbConnection().isClosed())||(!targetDb.getDbConnection().isClosed())) {
            throw new ConnectException(e) { //todo siehe kommentar oben
            };
        }
        con.getDbConnection().close();
    }

    //HILFSFUNKTIONEN FÜR DIE TESTS! ////


    private void clearTestDb(TransactionContext sourceDb) throws Exception {
        Connection con = sourceDb.getDbConnection();
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        try {
            stmt.execute("DROP TABLE colors");
        } catch (SQLException e) {};
        try {
            stmt.execute("DROP TABLE colors_copy");
        } catch (SQLException e) {};

    }

    private File createFile(String stm) throws IOException {
        File sqlFile =  folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write(stm);
        writer.close();
        return sqlFile;
    }

    private void createTestDb(TransactionContext sourceDb )
            throws Exception{
        Connection con = sourceDb.getDbConnection();
        createTableInTestDb(con);
        writeExampleDataInTestDB(con);

    }

    private void createTableInTestDb(Connection con) throws Exception {
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE colors ( " +
                "  rot integer, " +
                "  gruen integer, " +
                "  blau integer, " +
                "  farbname VARCHAR(200))");
        stmt.execute("CREATE TABLE colors_copy (rot integer, gruen integer, blau integer, farbname VARCHAR(200))");
    }

    private void writeExampleDataInTestDB(Connection con) throws Exception{
        Statement stmt = con.createStatement();
        stmt.execute("INSERT INTO colors  VALUES (255,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (251,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (0,0,255,'blau')");
    }

}