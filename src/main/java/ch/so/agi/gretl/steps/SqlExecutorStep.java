package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.util.FileExtension;
import ch.so.agi.gretl.util.SqlReader;
import ch.so.agi.gretl.logging.Logger;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


/**
 * The SqlExecutorStep Class is used as a Step and does Transformations on data within a database based on queries in
 * sql-Scripts
 */
public class SqlExecutorStep {


    /**
     * Executes the queries within the .sql-files in the specified database. But does not commit SQL-Statements
     *
     * @param trans         Database properties to generate database connection
     * @param sqlfiles      Files with .sql-extension which contain queries
     * @throws Exception    if File is missing, no correct extension, no connection to database, could not read file or
     *                      problems while executing sql-queries
     */
    public static void execute(TransactionContext trans, List<File> sqlfiles)
            throws Exception {

        Logger.log(Logger.INFO_LEVEL,"Start SqlExecutorStep");

        checkIfAtLeastOneSqlFileIsGiven(sqlfiles);

        logPathToInputSqlFiles(sqlfiles);


        try{
            Connection db = trans.getDbConnection();

            checkFileExtensionsForSqlExtension(sqlfiles);

            readSqlFiles(sqlfiles, db);

            db.commit();
            db.close();

        } catch (Exception e){
            throw new Exception ("Could not connect to Database: " + e);
        }
    }


    /**
     * @param sqlfiles      Files with .sql-extension which contain queries
     * @throws Exception    if File is missing
     */
    private static void checkIfAtLeastOneSqlFileIsGiven(List<File> sqlfiles)
            throws Exception {

        if (sqlfiles==null || sqlfiles.size()<1){
            throw new IllegalAccessException("Missing input files");
        }
    }

    /**
     * @param sqlfiles      Files with .sql-extension which contain queries
     */
    private static void logPathToInputSqlFiles(List<File> sqlfiles) {

        for (File inputfile: sqlfiles){
            Logger.log(Logger.INFO_LEVEL, inputfile.getAbsolutePath());
        }
    }

    /**
     * @param sqlfiles      Files with .sql-extension which contain queries
     * @throws Exception    if no correct file extension
     */
    private static void checkFileExtensionsForSqlExtension(List<File> sqlfiles)
            throws Exception {

        for (File file: sqlfiles) {
            String fileExtension = FileExtension.getFileExtension(file);
            if (!fileExtension.equalsIgnoreCase("sql")){
                throw new Exception("incorrect file extension at file: " + file.getAbsolutePath());
            }
        }
    }


    /**
     * @param sqlfiles      Files with .sql-extension which contain queries
     * @param db            connection to database
     * @throws Exception    if problems with reading file
     */
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
     * @throws Exception        SQL-Exception while executing sqlstatement
     */
    private static void executeSqlScript(Connection conn, InputStreamReader inputStreamReader)
            throws Exception{

        PushbackReader reader = null;
        reader = new PushbackReader(inputStreamReader);

        executeAllSqlStatements(conn, reader);


        reader.close();

    }


    /**
     * @param conn             Database connection
     * @param reader           Filereader
     * @throws Exception       SQL-Exception while executing sqlstatement
     */
    private static void executeAllSqlStatements (Connection conn, PushbackReader reader)
            throws Exception {

        String statement = SqlReader.readSqlStmt(reader);

        while (statement != null) {

            prepareSqlStatement(conn,statement);
            statement = SqlReader.readSqlStmt(reader);
        }
    }

    /**
     * @param conn          Connection to database
     * @param statement     sql-Statement
     * @throws Exception    SQL-Exception while executing sqlstatement
     */
    private static void prepareSqlStatement(Connection conn, String statement)
            throws Exception {

        statement = statement.trim();

        if (statement.length() > 0) {
            Logger.log(Logger.DEBUG_LEVEL, statement);

            Statement dbstmt = null;
            dbstmt = conn.createStatement();

            executeSqlStatement(dbstmt, statement);
        }
    }

    /**
     * @param dbstmt        Database sql-Statement
     * @param statement     sql-Statement
     * @throws Exception    SQL-Exception while executing sqlstatement
     */
    private static void executeSqlStatement (Statement dbstmt, String statement)
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
