package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.util.*;
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
    private String taskName;


    public SqlExecutorStep() {
        this(null);
    }

    public SqlExecutorStep(String taskName){
        if (taskName == null){
            taskName = SqlExecutorStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
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
    public void execute(Connector trans, List<File> sqlfiles)
            throws Exception {

        Connection db = null;

        log.lifecycle(taskName + ": Start SqlExecutor");

        checkIfConnectorIsNotNull(trans);

        assertAtLeastOneSqlFileIsGiven(sqlfiles);



        log.lifecycle(taskName +": Given parameters DB-URL: "  + trans.connect().getMetaData().getURL() +
                ", DB-User: " + trans.connect().getMetaData().getUserName() +
                ", Files: " + sqlfiles);


        logPathToInputSqlFiles(sqlfiles);





        try{
            db = trans.connect();

            checkIfNoExistingFileIsEmpty(sqlfiles);

            checkFilesExtensionsForSqlExtension(sqlfiles);

            checkFilesForUTF8WithoutBOM(sqlfiles);

            readSqlFiles(sqlfiles, db);

            db.commit();


            log.lifecycle(taskName + ": End SqlExecutor (successful)");

        } catch (Exception e){
            if (db != null) {
                db.rollback();
            }
            throw e;

        } finally {
            if (db != null){
                db.close();
            }
        }
    }


    /**
     *
     * @param trans             Connector
     * @throws GretlException   if Connector is null
     */
    private void checkIfConnectorIsNotNull(Connector trans)

            throws GretlException{

        if (trans == null) {
            throw new GretlException(GretlException.TYPE_NO_DB, "Connector-String must not be null");
        }
    }

    /**
     * @param sqlfiles      Files with .sql-extension which contain queries
     * @throws GretlException    if File is missing
     */
    private void assertAtLeastOneSqlFileIsGiven(List<File> sqlfiles)
            throws GretlException {

        if (sqlfiles == null || sqlfiles.size() < 1){
            throw new GretlException(GretlException.TYPE_NO_FILE, "Inputfile are either null or there is no inputfile");
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
     * @param sqlfiles          Files with queries as List
     * @throws Exception        File is empty or can not be found
     */
    private void checkIfNoExistingFileIsEmpty(List<File> sqlfiles)
            throws Exception {
        for (File file: sqlfiles){
            if (file.exists()){
                BufferedReader br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
                if (br.readLine() == null) {
                    throw new EmptyFileException("File must not be empty: " + file.getAbsolutePath());
                }
            } else {
                throw new FileNotFoundException("File could not be found: " + file.getAbsolutePath());

            }
        }

    }


    /**
     * @param sqlfiles      Files with .sql-extension which contain queries
     * @throws Exception    if no correct file extension
     */
    private void checkFilesExtensionsForSqlExtension(List<File> sqlfiles)
            throws Exception {

        for (File file: sqlfiles) {
            String fileExtension = FileExtension.getFileExtension(file);
            if (!fileExtension.equalsIgnoreCase("sql")){
                throw new GretlException(GretlException.TYPE_WRONG_EXTENSION,"File extension must be .sql. Error at File: " + file.getAbsolutePath());
            }
        }
    }


    /**
     * @param sqlfiles      Files with queries as list
     * @throws Exception    if File is not encoded in UTF8 or has BOM
     */
    private void checkFilesForUTF8WithoutBOM(List<File> sqlfiles)
            throws Exception {

        for (File file: sqlfiles) {
            FileStylingDefinition.checkForUtf8(file);
            FileStylingDefinition.checkForBOMInFile(file);
        }
    }


    /**
     * @param sqlfiles      Files with .sql-extension which contain queries
     * @param db            connection to database
     * @throws Exception    if problems with reading file or with executing queries
     */
    private void readSqlFiles(List<File> sqlfiles, Connection db)
            throws Exception {

        for (File sqlfile: sqlfiles){

            executeAllSqlStatements(db, sqlfile);

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
            int modifiedLines = dbstmt.executeUpdate(statement);  //only allows SQL INSERT, UPDATE or DELETE

            if (modifiedLines==1) {
                log.lifecycle(taskName + ": " + modifiedLines + " Line has been modified.");
            } else if (modifiedLines>1) {
                log.lifecycle(taskName +": " + modifiedLines + " Lines have been modified.");
            } else if (modifiedLines<1){
                log.lifecycle(taskName + ": No Line has been modified.");
            }


        } catch (SQLException ex) {
            throw new SQLException("Error while executing the sqlstatement. " + ex.getMessage());
        } finally {
            dbstmt.close();
        }
    }

}
