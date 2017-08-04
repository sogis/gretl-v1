package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


/**
 * Tests for the SqlExecutorStep
 */
public class SqlExecutorStepTest {
    private GretlLogger log;

    public SqlExecutorStepTest() {
        LogEnvironment.initStandalone();
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    //Create temporary folder for saving sqlfiles
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void initialize() throws Exception {
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);
        createTestDb(sourceDb);
    }

    @After
    public void finalise() throws Exception {
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);
        clearTestDb(sourceDb);
    }




    @Test
    public void executeWithoutFiles() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);

        List<File> sqlListe = new ArrayList<>();

        try {
            x.execute(sourceDb,sqlListe);
            Assert.fail();
        } catch (Exception e) {

        }
    }

    @Test
    public void executeWithoutDb() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();
        Connector sourceDb = null;

        List<File> sqlListe = createCorrectSqlFiles();

        try {
            x.execute(sourceDb,sqlListe);
            Assert.fail();
        } catch (Exception e) {

        }
    }


    @Test
    public void executeDifferentExtensions() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();

        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);

        List<File> sqlListe = createCorrectSqlFiles();
        sqlListe.add(createSqlFileWithWrongExtension());


        try {
            x.execute(sourceDb,sqlListe);
            Assert.fail();
        } catch (Exception e) {

        }
    }


    @Test
    public void executeEmptyFile() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);

        List<File>sqlListe = createCorrectSqlFiles();
        sqlListe.add(createEmptySqlFile());

        x.execute(sourceDb,sqlListe);
    }


    @Test
    public void executeWrongQuery() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);

        List<File> sqlListe = createWrongSqlFiles();

        try {
            x.execute(sourceDb, sqlListe);
            Assert.fail();
        } catch (Exception e) {

        }

    }


    @Test
    public void executePositiveTest() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);

        List<File> sqlListe = createCorrectSqlFiles();

        x.execute(sourceDb,sqlListe);
    }


    @Test
    public void checkIfConnectionIsClosed() throws Exception{
        SqlExecutorStep x = new SqlExecutorStep();
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);

        List<File> sqlListe = createCorrectSqlFiles();

        x.execute(sourceDb,sqlListe);
        Assert.assertTrue(sourceDb.getDbConnection().isClosed());
    }

    @Test
    public void notClosedConnectionThrowsError() throws Exception{
        SqlExecutorStep x = new SqlExecutorStep();
        Connector sourceDb = new Connector("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);

        List<File> sqlListe = createCorrectSqlFiles();

        x.execute(sourceDb,sqlListe);
        Assert.assertFalse(!sourceDb.getDbConnection().isClosed());
    }


    private void clearTestDb(Connector sourceDb) throws Exception {
        Connection con = sourceDb.getDbConnection();
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        stmt.execute("DROP TABLE colors");
        con.close();
    }

    private void createTestDb(Connector sourceDb )
            throws Exception{
        Connection con = sourceDb.getDbConnection();
        con.setAutoCommit(true);
        createTableInTestDb(con);
        con.close();

    }

    private void createTableInTestDb(Connection con) throws Exception {
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE colors ( " +
                "  rot integer, " +
                "  gruen integer, " +
                "  blau integer, " +
                "  farbname VARCHAR(200))");
        writeExampleDataInTestDB(con);
    }

    private void writeExampleDataInTestDB(Connection con) throws Exception{
        Statement stmt = con.createStatement();
        stmt.execute("INSERT INTO colors  VALUES (255,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (251,0,0,'rot')");
        stmt.execute("INSERT INTO colors  VALUES (0,0,255,'blau')");
    }

    private File createSqlFileWithWrongExtension() throws Exception{
        File sqlFile =  folder.newFile("query.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write(" SELECT \n" +
                "  colors.rot,\n" +
                "  gruen,\n" +
                "  colors.blau,\n" +
                "  colors.farbname\n" +
                "FROM colors\n" +
                "WHERE farbname = 'rot'");
        writer.close();
        return sqlFile;
    }

    private File createEmptySqlFile() throws Exception {
        File sqlFile1 =  folder.newFile("query2.sql");
        BufferedWriter writer1 = new BufferedWriter(new FileWriter(sqlFile1));
        writer1.write("");
        writer1.close();
        return sqlFile1;
    }

    private List<File> createWrongSqlFiles() throws Exception {
        File sqlFile =  folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write(" SELECT \n" +
                "  colors1.rot,\n" +
                "  gruen,\n" +
                "  colors1.blau,\n" +
                "  colors1.farbname\n" +
                "FROM color1\n" +
                "WHERE farbname = 'rot'");
        writer.close();
        List<File> sqlListe = new ArrayList<>();
        sqlListe.add(sqlFile);
        return sqlListe;
    }

    private List<File> createCorrectSqlFiles() throws Exception {
        File sqlFile =  folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write(" SELECT \n" +
                "  colors.rot,\n" +
                "  gruen,\n" +
                "  colors.blau,\n" +
                "  colors.farbname\n" +
                "FROM colors\n" +
                "WHERE farbname = 'rot'   ;    SELECT farbname FROM colors WHERE gruen=0 GROUP BY farbname");
        writer.close();

        File sqlFile1 =  folder.newFile("query1.sql");
        BufferedWriter writer1 = new BufferedWriter(new FileWriter(sqlFile1));
        writer1.write("SELECT farbname\n" +
                "FROM colors\n" +
                "WHERE gruen=0\n" +
                "GROUP BY farbname");
        writer1.close();
        List<File> sqlListe = new ArrayList<>();
        sqlListe.add(sqlFile);
        sqlListe.add(sqlFile1);
        return sqlListe;
    }





}