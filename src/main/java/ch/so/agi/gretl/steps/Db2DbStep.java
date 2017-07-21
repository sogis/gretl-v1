package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.EmptyFileException;
import ch.so.agi.gretl.util.EmptyListException;
import ch.so.agi.gretl.util.NotAllowedSqlExpressionException;
import ch.so.agi.gretl.util.SqlReader;

import java.io.*;
import java.sql.*;
import java.util.List;

/**
 * The Db2DbStep Class is used as a step for transfer of tabulated data from one to anoter database.
 * It needs a sourceDb (TransactionContext), a targetDb (TransactionContext) and a list of transferSet, containing 1. a
 * boolean paramterer concerning the emptying of the Targettable, 2. a SQL-file containing a SELECT-statement and
 * 3. a qualified target schema and table name (schema.table).
 */
public class Db2DbStep {

    private GretlLogger log;

    public Db2DbStep() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    /**
     * Main method. Calls for each transferSet methode processTransferSet
     * @param sourceDb
     * @param targetDb
     * @param transferSets
     * @throws Exception
     */
    public void processAllTransferSets(TransactionContext sourceDb, TransactionContext targetDb, List<TransferSet> transferSets) throws Exception {
        checkIfListNotEmpty(transferSets);
        log.livecycle( "\n\nStart Db2DbStep. Found "+transferSets.size()+" transferSets. \n" +
                "sourceDb = "+sourceDb.getDbConnection().getMetaData().getURL()+", " +
                "user = "+sourceDb.getDbConnection().getMetaData().getUserName()+", \n" +
                "targetDb = "+targetDb.getDbConnection().getMetaData().getURL()+", " +
                "user = "+targetDb.getDbConnection().getMetaData().getUserName()+"\n");
        try {
            Connection sourceDbConnection = sourceDb.getDbConnection();
            Connection targetDbConnection = targetDb.getDbConnection();
            for(TransferSet transferSet : transferSets){
                if(!transferSet.getInputSqlFile().canRead()) {
                    throw new IllegalArgumentException("File"+transferSet.getInputSqlFile().getName()+" not found or not readable");
                }
                processTransferSet(sourceDbConnection, targetDbConnection, transferSet);
            }
            sourceDbConnection.commit();
            targetDbConnection.commit();
            log.livecycle("Transfered all all Transfersets");
        } catch (Exception e) {
            if (sourceDb.getDbConnection()!=null) {
                sourceDb.getDbConnection().rollback();
            }
            if (targetDb.getDbConnection() != null) {
                targetDb.getDbConnection().rollback();
            }
            throw new Exception("Failed!: "+e);
        } finally {
            if (sourceDb.getDbConnection() != null) {
                sourceDb.getDbConnection().close();
            }
            if (targetDb.getDbConnection() != null) {
                targetDb.getDbConnection().close();
            }
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
    private void processTransferSet(Connection srcCon, Connection targetCon, TransferSet transferSet) throws SQLException, IOException, EmptyFileException, NotAllowedSqlExpressionException {
        if (transferSet.getDeleteAllRows() == true) {
            deleteDestTableContents(targetCon, transferSet.getOutputQualifiedSchemaAndTableName());
        }
        String selectStatement = extractSingleStatement(transferSet.getInputSqlFile());
        ResultSet rs = createResultSet(srcCon, selectStatement);
        PreparedStatement insertRowStatement = createInsertRowStatement(
                srcCon,
                targetCon,
                rs,
                transferSet.getOutputQualifiedSchemaAndTableName());

        int columncount = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            transferRow(rs, insertRowStatement, columncount);
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
            PreparedStatement truncatestmt = targetCon.prepareStatement(sqltruncate);
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
        Statement SQLStatement = srcCon.createStatement();
        ResultSet rs = SQLStatement.executeQuery(sqlSelectStatement);

        return rs;
    }

    /**
     * Creates woth the meta-data from the SelectStatement the Insert-Statement
     * @param srcCon
     * @param targetCon
     * @param rs
     * @param destTableName
     * @throws SQLException
     */
    private PreparedStatement createInsertRowStatement(Connection srcCon, Connection targetCon, ResultSet rs, String destTableName) throws SQLException {
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
        log.debug("Transfering table with "+rs.getFetchSize()+" rows and "+meta.getColumnCount()+" columns to table "+destTableName);

        String sql = "INSERT INTO " + destTableName + " ("
                + columnNames
                + ") VALUES ("
                + bindVariables
                + ")";
        PreparedStatement insertRowStatement = targetCon.prepareStatement(sql);

        return insertRowStatement;
    }

    /**
     * Extracts a single statement out of the SQL-file and checks if it fits the conditions.
     * @param targetFile
     * @return
     * @throws FileNotFoundException
     * @throws EmptyFileException
     */
    private String extractSingleStatement(File targetFile) throws EmptyFileException, IOException {

        String line = null;

        String firstline = null;
        try {
            line = SqlReader.readSqlStmt(targetFile);
            if(line == null) {
                log.info("Empty File. No Statement to execute!");
                throw new EmptyFileException("EmptyFile: "+targetFile.getName());
            }


            while (line != null) {
                firstline = line.trim();
                if (firstline.length() > 0) {
                    log.info( "Statement found. Length: " + firstline.length()+" caracters");
                } else {
                    log.info( "NO STATEMENT IN FILE!");
                    throw new FileNotFoundException();
                }
                line = SqlReader.nextSqlStmt();
                if(line != null) {
                    log.info("There are more then 1 Statement in the file!");
                    throw new RuntimeException();
                }
            }
        } catch (IOException e2) {
            throw new IllegalStateException(e2);
        }
        return firstline;
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