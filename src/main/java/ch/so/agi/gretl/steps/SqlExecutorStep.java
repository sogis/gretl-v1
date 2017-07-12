package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.util.FileExtension;
import ch.so.agi.gretl.util.SqlReader;
import ch.so.agi.gretl.logging.Logger;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * The SqlExecutorStep Class is used as a Step and does Transformations on geodata based on sql-Scripts
 */
public class SqlExecutorStep {


    /**
     * Executes the queries within the .sql-files in the specified database. But does not commit SQL-Statements
     *
     * @param trans
     * @param sqlfiles      Files with .sql-extension which contain queries
     * @throws Exception
     */
    public void execute(TransactionContext trans, List<File> sqlfiles)
            throws Exception {

        String CompleteQuery = "";

        Logger.log(Logger.INFO_LEVEL,"Start SqlExecutorStep");

        //Check for files in list
        if (sqlfiles==null || sqlfiles.size()<1){
            throw new IllegalAccessException("Missing input files");
        }

        //Log all input files
        for (File inputfile: sqlfiles){
            Logger.log(Logger.INFO_LEVEL, inputfile.getAbsolutePath());
        }

        try{
            Connection db = trans.getDbConnection();

            checkFileExtensionsForSqlExtension(sqlfiles);

            readSqlFiles(sqlfiles, db);

        } catch (Exception e){
            throw new Exception ("Could not connect to Database");
        } finally {
            trans.dbConnectionClose();
        }
    }


    private static void checkFileExtensionsForSqlExtension(List<File> sqlfiles)
            throws Exception {

        for (File file: sqlfiles) {
            String fileExtension = FileExtension.getFileExtension(file);
            if (!fileExtension.equalsIgnoreCase("sql")){
                throw new Exception("incorrect file extension at file: " + file.getAbsolutePath());
            }
        }
    }


    private static void readSqlFiles(List<File> sqlfiles, Connection db)
            throws Exception {

        for (File sqlfile: sqlfiles){

            try {
                FileInputStream sqlFileInputStream = new FileInputStream(sqlfile);
                InputStreamReader sqlFileReader = null;
                sqlFileReader = new InputStreamReader(sqlFileInputStream);
                executeSqlScript(db, sqlFileReader);
                sqlFileReader.close();
                sqlFileInputStream.close();

            } catch (Exception h) {
                throw new Exception("Error with File: " + sqlfile.getAbsolutePath() + " " + h.toString());
            }
        }
    }


    /**
     * Gets the sqlqueries out of the given file and executes the statements on the given database
     * @param conn              Database connection
     * @param inputStreamReader inputStream of a specific file
     */
    private static void executeSqlScript(Connection conn, InputStreamReader inputStreamReader)
            throws Exception{

        PushbackReader reader = null;
        reader = new PushbackReader(inputStreamReader);

        executeAllSqlStatements(conn, reader);


        reader.close();

    }


    private static void executeAllSqlStatements (Connection conn, PushbackReader reader)
            throws Exception {

        String statement = SqlReader.readSqlStmt(reader);

        while (statement != null) {

            prepareSqlStatement(conn,statement);
            statement = SqlReader.readSqlStmt(reader);
        }
    }

    private static void prepareSqlStatement(Connection conn, String statement)
            throws Exception{

        statement = statement.trim();

        if (statement.length() > 0) {
            Logger.log(Logger.DEBUG_LEVEL, statement);

            Statement dbstmt = null;
            dbstmt = conn.createStatement();

            executeSqlStatement(conn, dbstmt, statement);
        }
    }

    private static void executeSqlStatement (Connection conn, Statement dbstmt, String statement)
            throws Exception {

        try {
            dbstmt.execute(statement);
        } catch (SQLException ex) {
            throw new Exception("Error while executing the sqlstatement. " + ex);
        } finally {
            dbstmt.close();
        }
    }

}
