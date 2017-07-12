package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.util.DbConnector;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
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

    //Create temporary folder for saving sqlfiles
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();


    @Test
    public void executeWithoutFiles() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();
        TransactionContext sourceDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);
        List<File> sqlListe = new ArrayList<File>();

        try {
            x.execute(sourceDb,sqlListe);
            Assert.fail();
        } catch (Exception e) {

        }
    }

    @Test
    public void executeWithoutDb() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();
        TransactionContext sourceDb = null;

        File sqlFile =  folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write(" SELECT \n" +
                "  ST_Collect(geometrie),\n" +
                "  forstreviere_forstkreis.aname\n" +
                "FROM \n" +
                "  awjf_forstreviere.forstreviere_forstreviergeometrie\n" +
                "  LEFT JOIN awjf_forstreviere.forstreviere_forstrevier\n" +
                "    ON forstreviere_forstrevier.t_id=forstreviere_forstreviergeometrie.forstrevier\n" +
                "  LEFT JOIN awjf_forstreviere.forstreviere_forstkreis\n" +
                "    ON forstreviere_forstkreis.t_id=forstreviere_forstrevier.forstkreis\n" +
                "GROUP BY forstreviere_forstkreis.t_id");
        writer.close();
        List<File> sqlListe = new ArrayList<File>();
        sqlListe.add(sqlFile);

        try {
            x.execute(sourceDb,sqlListe);
            Assert.fail();
        } catch (Exception e) {

        }
    }


    @Test
    public void executeDifferentExtensions() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();

        TransactionContext sourceDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);

        File sqlFile =  folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write(" SELECT \n" +
                "  ST_Collect(geometrie),\n" +
                "  forstreviere_forstkreis.aname\n" +
                "FROM \n" +
                "  awjf_forstreviere.forstreviere_forstreviergeometrie\n" +
                "  LEFT JOIN awjf_forstreviere.forstreviere_forstrevier\n" +
                "    ON forstreviere_forstrevier.t_id=forstreviere_forstreviergeometrie.forstrevier\n" +
                "  LEFT JOIN awjf_forstreviere.forstreviere_forstkreis\n" +
                "    ON forstreviere_forstkreis.t_id=forstreviere_forstrevier.forstkreis\n" +
                "GROUP BY forstreviere_forstkreis.t_id");
        writer.close();

        File sqlFile1 =  folder.newFile("query1.txt");
        BufferedWriter writer1 = new BufferedWriter(new FileWriter(sqlFile1));
        writer1.write(" SELECT \n" +
                "  ST_Collect(geometrie),\n" +
                "  forstreviere_forstkreis.aname\n" +
                "FROM \n" +
                "  awjf_forstreviere.forstreviere_forstreviergeometrie\n" +
                "  LEFT JOIN awjf_forstreviere.forstreviere_forstrevier\n" +
                "    ON forstreviere_forstrevier.t_id=forstreviere_forstreviergeometrie.forstrevier\n" +
                "  LEFT JOIN awjf_forstreviere.forstreviere_forstkreis\n" +
                "    ON forstreviere_forstkreis.t_id=forstreviere_forstrevier.forstkreis\n" +
                "GROUP BY forstreviere_forstkreis.t_id");
        writer1.close();
        List<File> sqlListe = new ArrayList<File>();
        sqlListe.add(sqlFile);
        sqlListe.add(sqlFile1);

        try {
            x.execute(sourceDb,sqlListe);
            Assert.fail();
        } catch (Exception e) {

        }
    }


    @Test
    public void executeEmptyFile() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();
        TransactionContext sourceDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);
        Connection con = sourceDb.getDbConnection();
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE colors2 ( " +
                "  rot integer, " +
                "  gruen integer, " +
                "  blau integer, " +
                "  farbname VARCHAR(200))");
        stmt.execute("INSERT INTO colors2  VALUES (255,0,0,'rot')");


        File sqlFile =  folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write(" SELECT \n" +
                "  colors2.rot,\n" +
                "  gruen,\n" +
                "  colors2.blau,\n" +
                "  colors2.farbname\n" +
                "FROM colors2\n" +
                "WHERE farbname = 'rot'");
        writer.close();

        File sqlFile1 =  folder.newFile("query1.sql");
        BufferedWriter writer1 = new BufferedWriter(new FileWriter(sqlFile1));
        writer1.write("");
        writer1.close();
        List<File> sqlListe = new ArrayList<File>();
        sqlListe.add(sqlFile);
        sqlListe.add(sqlFile1);

        x.execute(sourceDb,sqlListe);
        con.close();
    }


    @Test
    public void executeWrongQuery() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();
        TransactionContext sourceDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);
        Connection con = sourceDb.getDbConnection();
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE colors1 ( " +
                "  rot integer, " +
                "  gruen integer, " +
                "  blau integer, " +
                "  farbname VARCHAR(200))");
        stmt.execute("INSERT INTO colors1  VALUES (255,0,0,'rot')");


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
        List<File> sqlListe = new ArrayList<File>();
        sqlListe.add(sqlFile);

        try {
            x.execute(sourceDb, sqlListe);
            Assert.fail();
        } catch (Exception e) {

        }

    }


    @Test
    public void execute() throws Exception {
        SqlExecutorStep x = new SqlExecutorStep();
        TransactionContext sourceDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);
        Connection con = sourceDb.getDbConnection();
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


        File sqlFile =  folder.newFile("query.sql");
        BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile));
        writer.write(" SELECT \n" +
                "  colors.rot,\n" +
                "  gruen,\n" +
                "  colors.blau,\n" +
                "  colors.farbname\n" +
                "FROM colors\n" +
                "WHERE farbname = 'rot'");
        writer.close();

        File sqlFile1 =  folder.newFile("query1.sql");
        BufferedWriter writer1 = new BufferedWriter(new FileWriter(sqlFile1));
        writer1.write("SELECT farbname\n" +
                        "FROM colors\n" +
                        "WHERE gruen=0\n" +
                        "GROUP BY farbname");
        writer1.close();
        List<File> sqlListe = new ArrayList<File>();
        sqlListe.add(sqlFile);
        sqlListe.add(sqlFile1);

        x.execute(sourceDb,sqlListe);
    }






}