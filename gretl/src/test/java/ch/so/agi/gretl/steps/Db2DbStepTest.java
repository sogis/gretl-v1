package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.api.TransferSet;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.DbConnector;
import ch.so.agi.gretl.util.EmptyFileException;
import ch.so.agi.gretl.util.GretlException;
import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

import static org.gradle.internal.impldep.org.testng.AssertJUnit.assertEquals;
import static org.junit.Assert.fail;

public class Db2DbStepTest {

	@Deprecated
    private static final String GEOM_WKT = "LINESTRING(2600000 1200000,2600001 1200001)";

    public Db2DbStepTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private GretlLogger log;

    @After
    public void finalise() throws Exception {
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        clearTestDb(sourceDb);
    }

    @Test
    public void faultFreeExecutionTest() throws Exception {
        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = TestUtil.createFile(folder, "SELECT * FROM colors; ", "query.sql");
            File sqlFile2 = TestUtil.createFile(folder, "SELECT * FROM colors; ", "query2.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(true)
            ));
            mylist.add(new TransferSet(
                    sqlFile2.getAbsolutePath(), "colors_copy", new Boolean(false)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);

            ResultSet rs = con.connect().createStatement().executeQuery("SELECT * FROM colors_copy WHERE farbname = 'blau'");
            while(rs.next()) {
                assertEquals(rs.getObject("rot"),0);
                assertEquals(rs.getObject("farbname"),"blau");
            }
        } finally {
            con.connect().close();
        }
    }
    
    @Test
    public void newlineAtEndOfFileTest() throws Exception {
        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = folder.newFile("query.sql");
            FileWriter sqlWriter=null;
            try {
                sqlWriter=new FileWriter(sqlFile);
                sqlWriter.write("SELECT * FROM colors;");
                sqlWriter.write(System.getProperty("line.separator"));
                //sqlWriter.write("SELECT * FROM colors;");
            }finally {
                if(sqlWriter!=null) {
                    sqlWriter.close();
                    sqlWriter=null;
                }
            }
            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", true
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
        } finally {
            con.connect().close();
        }
    }
    
    @Test
    public void fileWithMultipleStmtTest() throws Exception {
        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = folder.newFile("query.sql");
            FileWriter sqlWriter=null;
            try {
                sqlWriter=new FileWriter(sqlFile);
                sqlWriter.write("SELECT * FROM colors;");
                sqlWriter.write(System.getProperty("line.separator"));
                sqlWriter.write("SELECT * FROM colors;");
            }finally {
                if(sqlWriter!=null) {
                    sqlWriter.close();
                    sqlWriter=null;
                }
            }
            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", true
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            fail();
        }catch(IOException ex) {
            
        } finally {
            con.connect().close();
        }
    }

    @Test
    public void db2DbEmptyFileTest() throws Exception {
        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = folder.newFile("query.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(false)
            ));

           Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
           Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();

            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            Assert.fail("EmptyFileException müsste geworfen werden");
        } catch (EmptyFileException e) {

        } finally {
            con.connect().close();
        }

    }

    // TODO: Was testet diese Methode? Fehler bei leerer sql-Datei oder Fehler bei falschem sql?
    //--> Bitte aufräumen und Methode besser benennen.
    @Test
    public void sqlExceptionTest() throws Exception {
        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = TestUtil.createFile(folder, "SELECT BLABLABLA FROM colors", "query.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(false)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();

            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            Assert.fail("EmptyFileException must be thrown");

        } catch (SQLException e) {
            log.debug("Got SQLException as expected");
        } finally{
            con.connect().close();
        }
    }

    @Test
    public void columnNumberTest() throws Exception {
        Connection con = DbConnector.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        con.setAutoCommit(true);
        try {
            //Hier müssen die Tabellen manuell erstellt werden, da die Tabelle colors_copy
            //ja gerade mit nicht genug Spalten angelegt werden soll!
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

            File sqlFile = TestUtil.createFile(folder, "SELECT rot, gruen, blau, farbname FROM colors", "query.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(false)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();

            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            Assert.fail("Eine Exception müsste geworfen werden. ");

        } catch (GretlException ge) {
            boolean isMismatchException = GretlException.TYPE_COLUMN_MISMATCH.equals(ge.getType());
            if(!isMismatchException){
                throw ge;
            }

        } finally {
            con.close();
        }
    }

    @Test
    public void incompatibleDataTypeTest() throws Exception {
        Connection con = DbConnector.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        con.setAutoCommit(true);
        try {
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

            File sqlFile = TestUtil.createFile(folder, "SELECT * FROM colors", "query.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(false)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();

            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            Assert.fail("Eine Exception müsste geworfen werden. ");

        } catch (SQLException e) {
            log.debug("Got SQLException as expected");
        } finally {
            con.close();
        }
    }

    @Test
    public void copyEmptyTableToOtherTableTest() throws Exception {
        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = TestUtil.createFile(folder, "SELECT * FROM colors", "query.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(false)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
        } finally {
            con.connect().close();
        }
    }

    @Test
    public void deleteTest() throws Exception {
        Connection con = null;
        try {
            Connector connector = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            con = connector.connect();
            createTestDb(connector);
            Statement stmt = con.createStatement();
            stmt.execute("INSERT INTO colors_copy  VALUES (255,0,0,'rot')");
            stmt.execute("INSERT INTO colors_copy  VALUES (251,0,0,'rot')");
            stmt.execute("INSERT INTO colors_copy  VALUES (0,0,255,'blau')");
            stmt.execute("INSERT INTO colors_copy  VALUES (251,0,0,'rot')");
            stmt.execute("INSERT INTO colors_copy  VALUES (67,2,255,'blauauaua')");
            stmt.execute("INSERT INTO colors_copy  VALUES (251,45,23,'rotototo')");
            stmt.execute("INSERT INTO colors_copy  VALUES (67,3,255,'blauwederenzian')");
            con.commit();
        } finally {
            con.close();
        }

        File sqlFile = TestUtil.createFile(folder, "SELECT * FROM colors;", "query.sql");

        ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
        mylist.add(new TransferSet(
                sqlFile.getAbsolutePath(), "colors_copy", new Boolean(true)
        ));

        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB", "bjsvwsch", null);
        Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB", "bjsvwsch", null);


        Db2DbStep db2db = new Db2DbStep();
        //db2dbstep ausführen
        db2db.processAllTransferSets(sourceDb, targetDb, mylist);

        //Select auf db, um korrektes ausführen zu verifizieren
        Connection con2 = null;
        try {
            Connector connector2 = new Connector("jdbc:derby:memory:myInMemDB", "bjsvwsch", null);
            con2 = connector2.connect();
            ResultSet rs = con2.createStatement().executeQuery("SELECT COUNT(*) FROM colors_copy");
            rs.next();
            int i = rs.getInt(1);
            con2.commit();
            assertEquals(i, 3);
        } finally {
            con2.close();
        }
    }

    @Test
    public void closeConnectionsTest() throws Exception {
        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = TestUtil.createFile(folder, "SELECT * FROM colors", "query.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(true)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);

            Assert.assertTrue("SourceConnection is not closed", sourceDb.connect().isClosed());
            Assert.assertTrue("TargetConnection is not closed", targetDb.connect().isClosed());

        } finally {
            con.connect().close();
        }
    }


    @Test
    public void closeConnectionsAfterFailedTest() throws Exception {

        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = TestUtil.createFile(folder, "SELECT güggeliblau FROM colors_copy", "query.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(true)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();
            try {
                db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            } catch (SQLException e) {
                log.debug("Got SQLException as expected");
            } catch (Exception e) {
                log.debug("Got Exception as expected");
            }

            Assert.assertTrue("SourceConnection is not closed", sourceDb.connect().isClosed());
            Assert.assertTrue("TargetConnection is not closed", targetDb.connect().isClosed());
        } finally {
            con.connect().close();
        }
    }

    private void clearTestDb(Connector sourceDb) throws Exception {
        Connection con = sourceDb.connect();
        con.setAutoCommit(true);
        try {
            Statement stmt = con.createStatement();
            try {
                stmt.execute("DROP TABLE colors");
            } catch (SQLException e) {};
            try {
                stmt.execute("DROP TABLE colors_copy");
            } catch (SQLException e) {};
        } finally {
            con.close();
        }
    }

    private void createTestDb(Connector sourceDb) throws Exception{
        Connection con = sourceDb.connect();
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