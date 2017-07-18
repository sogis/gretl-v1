package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.EmptyFileException;
import ch.so.agi.gretl.util.EmptyListException;
import ch.so.agi.gretl.util.NotAllowedSqlExpressionException;
import ch.so.agi.gretl.util.SqlReader;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * The Db2DbStep Class is used as a Step for transfer of data from one to anoter (or the same) database.
 * In the input SQL-File there could be a more or less complex Select Statement.
 */


public class Db2DbStep {

    private Connection sourceDbConnection;
    private Connection targetDbConnection;
    private GretlLogger log;

    /** Constructor **/
    public Db2DbStep() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    /**
     * Main Methode.Calls for each transferSet methode processTransferSet
     * @param sourceDb
     * @param targetDb
     * @param transferSets
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws EmptyFileException
     * @throws NotAllowedSqlExpressionException
     * @throws EmptyListException
     */
    public void processAllTransferSets(TransactionContext sourceDb, TransactionContext targetDb, List<TransferSet> transferSets) throws SQLException, FileNotFoundException, EmptyFileException, NotAllowedSqlExpressionException, EmptyListException {
        checkIfListNotEmpty(transferSets);
        log.info( "Found "+transferSets.size()+" transferSets");
        sourceDbConnection = sourceDb.getDbConnection();
        targetDbConnection = targetDb.getDbConnection();
        for(TransferSet transferSet : transferSets){
            processTransferSet(sourceDbConnection, targetDbConnection, transferSet);
        }

    }

    /**
     * Controls the execution of a TransferSet
     * @param srcCon
     * @param targetCon
     * @param transferSet
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws EmptyFileException
     * @throws NotAllowedSqlExpressionException
     */
    private void processTransferSet(Connection srcCon, Connection targetCon, TransferSet transferSet) throws SQLException, FileNotFoundException, EmptyFileException, NotAllowedSqlExpressionException {
        try {
            if (transferSet.getDeleteAllRows() == true) {
                deleteDestTableContents(targetCon, transferSet.getOutputQualifiedSchemaAndTableName());
            }
            String selectStatement = extractSingleStatement(transferSet.getInputSqlFile());

            ResultSet rs = createResultSet(srcCon, selectStatement);

            PreparedStatement insertRowStatement = createInsertRowStatement(
                    srcCon,
                    rs,
                    transferSet.getOutputQualifiedSchemaAndTableName());

            int columncount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                transferRow(rs, insertRowStatement, columncount);
            }
        } finally {
            srcCon.rollback();
            srcCon.close();
            targetCon.rollback();
            targetCon.close();
        }
    }

    /**
     * Copies a row of the source ResultSet to the target table
     * @param rs
     * @param insertRowStatement
     * @param columncount
     * @throws SQLException
     */
    private void transferRow(ResultSet rs, PreparedStatement insertRowStatement, int columncount) throws SQLException {
        // assign column wise values
        for (int j = 1; j <= columncount; j++) {
            insertRowStatement.setObject(j,rs.getObject(j));
        }
        insertRowStatement.execute();
    }

    /**
     * Delete the content of the target table
     * @param targetCon
     * @param destTableName
     * @throws SQLException
     */
    private void deleteDestTableContents(Connection targetCon, String destTableName) throws SQLException {
        String sqltruncate = "DELETE FROM "+destTableName;
        log.info("Try to delete all rows in Table "+destTableName);
        try {
            PreparedStatement truncatestmt = targetDbConnection.prepareStatement(sqltruncate);
            truncatestmt.execute();
            log.info( "DELETE succesfull!");
        } catch (SQLException e1) {
            log.info( "DELETE FROM TABLE "+destTableName+" failed!");
            log.debug(e1.getMessage());
            throw e1;
        }
    }

    /**
     * Creates the ResultSet with the SelectStatement from the InputFile
     * @param srcCon
     * @param sqlSelectStatement
     * @return
     * @throws SQLException
     */
    private ResultSet createResultSet(Connection srcCon, String sqlSelectStatement) throws SQLException {
        Statement SQLStatement = sourceDbConnection.createStatement();
        ResultSet rs = SQLStatement.executeQuery(sqlSelectStatement);

        return rs;
    }

    /**
     * Creates woth the meta-data from the SelectStatement the Insert-Statement
     * @param srcCon
     * @param rs
     * @param destTableName
     * @throws SQLException
     */

    private PreparedStatement createInsertRowStatement(Connection srcCon, ResultSet rs, String destTableName) throws SQLException {
        ResultSetMetaData meta = null;
        Statement dbstmt = null;
        StringBuilder columnNames = null;
        StringBuilder bindVariables = null;

        try {
            meta = rs.getMetaData();
        } catch (SQLException g) {
            log.info( String.valueOf(g));
            throw new SQLException(g);
        }
        columnNames = new StringBuilder();
        bindVariables = new StringBuilder();

        int j;
        for (j = 1; j <= meta.getColumnCount(); j++)
        {
            if (j > 1) {
                columnNames.append(", ");
                bindVariables.append(", ");
            }
            columnNames.append(meta.getColumnName(j));
            bindVariables.append("?");
        }
        log.info( "I got "+j+" columns");
        // prepare destination sql
        String sql = "INSERT INTO " + destTableName + " ("
                + columnNames
                + ") VALUES ("
                + bindVariables
                + ")";
        log.debug("INSERT STATEMENT RAW = "+sql);
        //System.out.print("INSERT STATEMENT RAW = "+sql);

        PreparedStatement insertRowStatement = targetDbConnection.prepareStatement(sql);

        return insertRowStatement;
    }

    /**
     * Extracts a single statement out of the SQL-file and checks if it fits the conditions.
     * @param targetFile
     * @return
     * @throws FileNotFoundException
     * @throws EmptyFileException
     * @throws NotAllowedSqlExpressionException
     */

    private String extractSingleStatement(File targetFile) throws FileNotFoundException, EmptyFileException, NotAllowedSqlExpressionException {
        if(!targetFile.canRead()) {throw new FileNotFoundException();}
        FileReader read = new FileReader(targetFile);
        PushbackReader reader = null;
        reader = new PushbackReader(read);
        String line = null;

        /** LIST of forbidden words **/
        List<String> keywords = new ArrayList<>();
        keywords.add("INSERT");
        keywords.add("DELETE");
        keywords.add("UPDATE");
        keywords.add("DROP");
        keywords.add("CREATE");

        String firstline = null;
        try {
            line = SqlReader.readSqlStmt(reader);
            if(line == null) {
                log.info("Empty File. No Statement to execute!");
                throw new EmptyFileException("EmptyFile: "+targetFile.getName());
            }
            while (line != null) {
                firstline = line.trim();
                if (firstline.length() > 0) {
                    log.info( "Statement found. Length: " + firstline.length()+" caracters");
                    //Check if there are no bad words in the Statement
                    if (containsAKeyword(firstline, keywords) == true) {
                        log.info( "FOUND NOT ALLOWED WORDS IN SQL STATEMENT!");
                        throw new NotAllowedSqlExpressionException();
                    }
                } else {
                    log.info( "NO STATEMENT IN FILE!");
                    throw new FileNotFoundException();
                }
                // read next line
                line = SqlReader.readSqlStmt(reader);
                if (line != null) {
                    log.info( "There are more then 1 SQL-Statement in the file " + targetFile.getName() + " but only the first Statement will be executed!");
                    throw new RuntimeException();
                }

            }
        } catch (IOException e2) {
            throw new IllegalStateException(e2);
        } finally {
            try {
                reader.close();
            } catch (IOException e3) {
                throw new IllegalStateException(e3);
            }
        }
        return firstline;
    }

    /**
     * Checks if a String contains a keyword from a List
     * @param myString
     * @param keywords
     */

    private boolean containsAKeyword(String myString, List<String> keywords){
        for(String keyword : keywords){
            if(myString.contains(keyword)){
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param transferSets
     * @throws EmptyListException
     */
    private void checkIfListNotEmpty(List<TransferSet> transferSets) throws EmptyListException {
        if(transferSets.size() == 0) {
            throw new EmptyListException();
        }
    }



}