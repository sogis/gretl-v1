package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.DbConnector;
import ch.so.agi.gretl.util.EmptyFileException;
import ch.so.agi.gretl.util.GretlException;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by bjsvwsch on 03.05.17.
 */
public class Db2DbStepTest {

    private static final String GEOM_WKT = "LINESTRING(2600000 1200000,2600001 1200001)";

    private static final String[] PG_WRITER_CONNECTION = {
            "jdbc:postgresql://192.168.56.31:5432/automatedtest",
            "dmluser",
            "dmluser"
    };

    private static final String[] PG_READER_CONNECTION = {
            "jdbc:postgresql://192.168.56.31:5432/automatedtest",
            "readeruser",
            "readeruser"
    };

    //Konstruktor//
    public Db2DbStepTest () {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
    }


    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private String e;
    private GretlLogger log;

    @After
    public void finalise() throws Exception {
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        clearTestDb(sourceDb);
    }

    @Test
    public void FaultFreeExecutionTest() throws Exception {
        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = createFile("SELECT * FROM colors; ", "query.sql");
            File sqlFile2 = createFile("SELECT * FROM colors; ", "query2.sql");

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
                //todo assertEquals(...)
                if (!rs.getObject("rot").equals(0)) throw new GretlException(e);
                if (!rs.getObject("farbname").equals("blau")) throw new GretlException(e);
            }
        } finally {
            con.connect().close();
        }


    }

    @Test
    public void Db2DbEmptyFileTest() throws Exception {
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

        } catch (Exception e) {
            //todo wieso catch exception? Dann gibt der test ok zurück obwohl gar nicht ok.
        } finally {
            con.connect().close();
        }

    }

    @Test
    public void SQLExceptionTest() throws Exception {
        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = createFile("SELECT BLABLABLA FROM colors", "query.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(false)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();

            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            Assert.fail("EmptyFileException müsste geworfen werden");

            //todo wie kann als Resultat eines Tests zwei Exception-Typen erwartet werden? Wie kann "catch all" jemals
            //richtig sein bei einem spezifischen test?
        } catch (SQLException e) {
            log.debug("Got SQLException as expected");
        } catch (Exception e) {
            log.debug("Got Exception as expected");
        } finally{
            con.connect().close();
        }
    }

    @Test
    public void ColumnNumberTest() throws Exception {
        //unittest
        DbConnector dbConn = new DbConnector();
        Connection con = DbConnector.connect("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        con.setAutoCommit(true);
        try {
            //todo Erstellen und füllen der Testtabelle ist bei Noemi schon als utilmethode vorhanden
            //utilmethode in entsprechende utilklasse rausmanufakteren und gemeinsam nutzen...
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

            File sqlFile = createFile("SELECT rot, gruen, blau, farbname FROM colors", "query.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(false)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();

            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            Assert.fail("Eine Exception müsste geworfen werden. ");

            //todo welche Exception wird erwartet? nur genau diese abfangen.
        } catch (SQLException e) {
            log.debug("Got SQLException as expected");
        } catch (Exception e) {
            log.debug("Got Exception as expected");
        } finally {
            con.close();
        }
    }

    @Test
    public void IncompatibleDataTypeTest() throws Exception {
        //unittest
        DbConnector dbConn = new DbConnector();
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

            File sqlFile = createFile("SELECT * FROM colors", "query.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(false)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();

            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            Assert.fail("Eine Exception müsste geworfen werden. ");

            //todo welche ex?
        } catch (SQLException e) {
            log.debug("Got SQLException as expected");
        } catch (Exception e) {
            log.debug("Got Exception as expected");
        } finally {
            con.close();
        }
    }

    //todo namen: was testet diese Testmethode genau?
    @Test
    public void EmptyTableTest() throws Exception {
        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTableInTestDb(con.connect());
        try {
            File sqlFile = createFile("SELECT * FROM colors", "query.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(false)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
        } catch (SQLException e) {
            //todo wieso catch und throw?
            throw new GretlException(e);
        } finally {
            con.connect().close();
        }
    }

    //todo namen...
    @Test
    public void NullSourceValueTest() throws Exception {

        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = createFile("SELECT * FROM colors", "query.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(false)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();
            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
        } catch (SQLException e) {
            //todo wieso catch und throw?
            throw new GretlException(e);
        } finally {
            con.connect().close();
        }
    }

    @Test
    public void DeleteTest() throws Exception {
        //unittest
        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = createFile("SELECT rot, gruen, blau, farbname FROM (SELECT ROW_NUMBER() OVER() AS rownum, colors.* FROM colors) AS tmp WHERE rownum <= 1;", "query.sql");

            ArrayList<TransferSet> mylist = new ArrayList<TransferSet>();
            mylist.add(new TransferSet(
                    sqlFile.getAbsolutePath(), "colors_copy", new Boolean(true)
            ));

            Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
            Connector targetDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);

            Db2DbStep db2db = new Db2DbStep();

            db2db.processAllTransferSets(sourceDb, targetDb, mylist);
            ResultSet rs = con.connect().createStatement().executeQuery("SELECT * FROM colors_copy");

            //todo assertEquals(rs.getFetchSize(), 0)?

            int count = 0;
            while(rs.next()) {
                ++count;
            }
            if(count > 1) {
                //todo trumpscher logeintrag ist lustig, muss aber zwecks verständlicherer Meldungen ersetzt werden ;-)
                log.info("Got "+count+" rows! Very sad!");
                throw new GretlException();
            }
        } finally {
            con.connect().close();
        }
    }
    //TEST with ORACLE and PostgreSQL ////////////////////////////////

    //TEST MUSS evtl. NOCH GESCHRIEBEN WERDEN....


    @Test
    public void CloseConnectionsTest() throws Exception {

        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = createFile("SELECT * FROM colors", "query.sql");

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

            //todo entfernen - ist aufgrund finally redundant
            con.connect().close();
        } finally {
            con.connect().close();
        }
    }

    @Test
    public void CloseConnectionsAfterFailedTest() throws Exception {

        Connector con = new Connector("jdbc:derby:memory:myInMemDB;create=true", "bjsvwsch", null);
        createTestDb(con);
        try {
            File sqlFile = createFile("SELECT güggeliblau FROM colors_copy", "query.sql");

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

    @Ignore("Test is ignored as long as we do not have a standardized postgres db for unit testing")
    @Test
    public void canWriteGeomFromWkbTest() throws Exception {
        String schemaName = "GeomFromWkbTest";

        Connection con = null;

        try{
            con =  connectToPreparedPgDb(schemaName);
            preparePgGeomSourceSinkTables(schemaName, con);

            Db2DbStep step = new Db2DbStep();
            File queryFile = createFile(
                    String.format("select ST_AsBinary(geom) as geom from %s.source", schemaName),
                    "select.sql");

            Connector src = new Connector(PG_READER_CONNECTION[0], PG_READER_CONNECTION[1], PG_READER_CONNECTION[2]);
            Connector sink = new Connector(PG_WRITER_CONNECTION[0], PG_WRITER_CONNECTION[1], PG_WRITER_CONNECTION[2]);
            TransferSet tSet = new TransferSet(
                    queryFile.getAbsolutePath(),
                    schemaName + ".SINK",
                    true,
                    new String[]{"geom:wkb:2056"}
            );

            step.processAllTransferSets(src, sink, Arrays.asList(tSet));

            assertEqualGeomInSourceAndSink(con, schemaName);

            dropSchema(schemaName, con);
        }
        finally {
            if(con != null)
                con.close();
        }
    }

    @Ignore("Test is ignored as long as we do not have a standardized postgres db for unit testing")
    @Test
    public void canWriteGeomFromWktTest() throws Exception {
        String schemaName = "GeomFromWktTest";

        Connection con = null;

        try{
            con = connectToPreparedPgDb(schemaName);
            preparePgGeomSourceSinkTables(schemaName, con);


            Db2DbStep step = new Db2DbStep();
            File queryFile = createFile(
                    String.format("select ST_AsText(geom) as geom from %s.source", schemaName),
                    "select.sql");

            Connector src = new Connector(PG_READER_CONNECTION[0], PG_READER_CONNECTION[1], PG_READER_CONNECTION[2]);
            Connector sink = new Connector(PG_WRITER_CONNECTION[0], PG_WRITER_CONNECTION[1], PG_WRITER_CONNECTION[2]);
            TransferSet tSet = new TransferSet(
                    queryFile.getAbsolutePath(),
                    schemaName + ".SINK",
                    true,
                    new String[]{"geom:wkt:2056"}
            );

            step.processAllTransferSets(src, sink, Arrays.asList(tSet));

            assertEqualGeomInSourceAndSink(con, schemaName);

            dropSchema(schemaName, con);
        }
        finally {
            if(con != null)
                con.close();
        }
    }

    @Ignore("Test is ignored as long as we do not have a standardized postgres db for unit testing")
    @Test
    public void canWriteGeomFromGeoJsonTest() throws Exception {
        String schemaName = "GeomFromGeoJsonTest";

        Connection con = null;

        try{
            con = connectToPreparedPgDb(schemaName);
            preparePgGeomSourceSinkTables(schemaName, con);

            //grant insert , delete on all tables in schema [schema] to [user] ..dml_user
            //grant select on all tables in schema [schema] to [user] ..reader_user.
            Db2DbStep step = new Db2DbStep();
            File queryFile = createFile(
                    String.format("select ST_AsGeoJSON(geom) as geom from %s.source", schemaName),
                    "select.sql");

            Connector src = new Connector(PG_READER_CONNECTION[0], PG_READER_CONNECTION[1], PG_READER_CONNECTION[2]);
            Connector sink = new Connector(PG_WRITER_CONNECTION[0], PG_WRITER_CONNECTION[1], PG_WRITER_CONNECTION[2]);
            TransferSet tSet = new TransferSet(
                    queryFile.getAbsolutePath(),
                    schemaName + ".SINK",
                    true,
                    new String[]{"geom:geojson:2056"}
            );

            step.processAllTransferSets(src, sink, Arrays.asList(tSet));

            assertEqualGeomInSourceAndSink(con, schemaName);

            dropSchema(schemaName, con);
        }
        finally {
            if(con != null)
                con.close();
        }
    }

    private static void assertEqualGeomInSourceAndSink(Connection con, String schemaName) throws SQLException {
        Statement check = con.createStatement();
        ResultSet rs = check.executeQuery(
                String.format("select ST_AsText(geom) as geom_text from %s.sink", schemaName));
        rs.next();
        String geomRes = rs.getString(1).trim().toUpperCase();

        Assert.assertEquals("The transferred geometry is not equal to the geometry in the source table", GEOM_WKT, geomRes);
    }

    private static void preparePgGeomSourceSinkTables(String schemaName, Connection con) throws SQLException {
        Statement prep = con.createStatement();
        prep.addBatch(String.format("CREATE TABLE %s.SOURCE (geom geometry(LINESTRING,2056) );",schemaName));
        prep.addBatch(String.format("CREATE TABLE %s.SINK (geom geometry(LINESTRING,2056) );",schemaName));
        prep.addBatch(String.format("INSERT INTO %s.SOURCE VALUES ( ST_GeomFromText('%s', 2056) )", schemaName, GEOM_WKT));

        prep.addBatch(String.format("GRANT SELECT ON ALL TABLES IN SCHEMA %s TO READERUSER", schemaName));
        prep.addBatch(String.format("GRANT SELECT, INSERT, DELETE ON ALL TABLES IN SCHEMA %s TO DMLUSER", schemaName));

        prep.executeBatch();
        con.commit();
    }

    private void dropSchema(String schemaName, Connection con) throws SQLException{
        Statement s = con.createStatement();
        s.execute(String.format("drop schema %s cascade", schemaName));
    }

    private Connection connectToPreparedPgDb(String schemaName) throws Exception {
        Driver pgDriver = (Driver)Class.forName("org.postgresql.Driver").newInstance();
        DriverManager.registerDriver(pgDriver);

        Connection con = DriverManager.getConnection(
            "jdbc:postgresql://192.168.56.31:5432/automatedtest",
            "ddluser",
            "ddluser");

        con.setAutoCommit(false);

        Statement s = con.createStatement();
        s.addBatch(String.format("drop schema if exists %s cascade", schemaName));
        s.addBatch("create schema " + schemaName);
        s.executeBatch();
        con.commit();

        return con;
    }





    //HILFSFUNKTIONEN FÜR DIE TESTS! ////

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

    private File createFile(String stm, String fileName) throws IOException {
        File sqlFile =  folder.newFile(fileName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write(stm);
        writer.close();
        return sqlFile;
    }

    private void createTestDb(Connector sourceDb )
            throws Exception{
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