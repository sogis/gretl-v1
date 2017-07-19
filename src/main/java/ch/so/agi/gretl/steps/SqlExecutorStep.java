package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.util.FileExtension;
import ch.so.agi.gretl.util.SqlReader;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

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

    private GretlLogger log;


    public SqlExecutorStep() {

        this.log = LogEnvironment.getLogger(this.getClass());

    }


    /**
     * Executes the queries within the .sql-files in the specified database. But does not commit SQL-Statements
     *
     * @param trans         Database properties to generate database connection
     * @param sqlfiles      Files with .sql-extension which contain queries
     * @throws Exception    if File is missing, no correct extension, no connection to database, could not read file or
     *                      problems while executing sql-queries
     */
    public void execute(TransactionContext trans, List<File> sqlfiles)
            throws Exception {

        Connection db = null;

        log.info("Start SqlExecutorStep");

        checkIfAtLeastOneSqlFileIsGiven(sqlfiles);

        logPathToInputSqlFiles(sqlfiles);



        try{
            db = trans.getDbConnection();

            checkFileExtensionsForSqlExtension(sqlfiles);

            readSqlFiles(sqlfiles, db);

            db.commit();

        } catch (Exception e){
            if (db!=null) {
                db.rollback();
            }
            throw e;

        } finally {
            if (db!=null){
                db.close();
            }

        }
    }


    /**
     * @param sqlfiles      Files with .sql-extension which contain queries
     * @throws Exception    if File is missing
     */
    private void checkIfAtLeastOneSqlFileIsGiven(List<File> sqlfiles)
            throws Exception {

        if (sqlfiles==null || sqlfiles.size()<1){
            throw new IllegalAccessException("Missing input files");
        }
    }

    /**
     * @param sqlfiles      Files with .sql-extension which contain queries
     */
    private void logPathToInputSqlFiles(List<File> sqlfiles) {

        for (File inputfile: sqlfiles){
            log.info(inputfile.getAbsolutePath());
        }
    }

    /**
     * @param sqlfiles      Files with .sql-extension which contain queries
     * @throws Exception    if no correct file extension
     */
    private void checkFileExtensionsForSqlExtension(List<File> sqlfiles)
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
    private void readSqlFiles(List<File> sqlfiles, Connection db)
            throws Exception {

        for (File sqlfile: sqlfiles){

            try {

                executeAllSqlStatements(db, sqlfile);


            } catch (Exception h) {
                throw new Exception("Error with File: " + sqlfile.getAbsolutePath() + " " + h.toString());
            }
        }
    }




    /**
     * @param conn             Database connection
     * @param sqlfile          SQL-File
     * @throws Exception       SQL-Exception while executing sqlstatement
     */
    private void executeAllSqlStatements (Connection conn, File sqlfile)
            throws Exception {


        String statement = SqlReader.readSqlStmt(sqlfile);

        while (statement != null) {

            prepareSqlStatement(conn,statement);
            statement = SqlReader.nextSqlStmt();
        }

    }

    /**
     * @param conn          Connection to database
     * @param statement     sql-Statement
     * @throws Exception    SQL-Exception while executing sqlstatement
     */
    private void prepareSqlStatement(Connection conn, String statement)
            throws Exception {

        statement = statement.trim();

        if (statement.length() > 0) {
            log.debug(statement);

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
    private void executeSqlStatement (Statement dbstmt, String statement)
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
