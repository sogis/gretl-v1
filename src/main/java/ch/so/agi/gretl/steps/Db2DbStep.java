package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The Db2DbStep Class is used as a step for transfer of tabulated data from one to anoter database.
 * It needs a sourceDb (Connector), a targetDb (Connector) and a list of transferSet, containing 1. a
 * boolean paramterer concerning the emptying of the Targettable, 2. a SQL-file containing a SELECT-statement and
 * 3. a qualified target schema and table name (schema.table).
 */
public class Db2DbStep {

    private static GretlLogger log = LogEnvironment.getLogger(Db2DbStep.class);
    private String taskName;


    public Db2DbStep() {
        this(null);
    }

    public Db2DbStep(String taskName){
        if (taskName==null){
            taskName=Db2DbStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    /**
     * Main method. Calls for each transferSet methode processTransferSet
     * @param sourceDb The Source Databaseconnection
     * @param targetDb The Target Databaseconnection
     * @param transferSets A list of Transfersets
     * @throws Exception
     */
    public void processAllTransferSets(Connector sourceDb, Connector targetDb, List<TransferSet> transferSets) throws Exception {
        assertValidTransferSets(transferSets);

        log.lifecycle(String.format("Start Db2DbStep(Name: %s SourceDb: %s TargetDb: %s Transfers: %s)", taskName, sourceDb, targetDb, transferSets));

        Connection sourceDbConnection = null;
        Connection targetDbConnection = null;

        ArrayList<String> rowCountStrings = new ArrayList<String>();

        try {
            sourceDbConnection = sourceDb.connect();
            targetDbConnection = targetDb.connect();
            for(TransferSet transferSet : transferSets){
                //Check if file is readable
                if(!transferSet.getInputSqlFile().canRead()) {
                    throw new IllegalArgumentException("File"+transferSet.getInputSqlFile().getName()+" not found or not readable");
                }
                //Check if File is UTF8
                FileStylingDefinition.checkForUtf8(transferSet.getInputSqlFile());
                //Check if File contains no BOM. If File is Empty, there will be a NullPointerException catched away.
                try {
                    FileStylingDefinition.checkForBOMInFile(transferSet.getInputSqlFile());
                } catch (NullPointerException e){};
                
                int rowCount = processTransferSet(sourceDbConnection, targetDbConnection, transferSet);
                rowCountStrings.add(Integer.toString(rowCount));
            }
            sourceDbConnection.commit();
            targetDbConnection.commit();

            String rowCountList = String.join(",", rowCountStrings);
            log.lifecycle(
                    String.format(
                            "Db2DbStep %s: Transfered all Transfersets. Number of Transfersets: %s, transfered rows: [%s]",
                            taskName,
                            rowCountStrings.size(),
                            rowCountList
                    )
            );
        } catch (Exception e) {
            if (sourceDbConnection!=null) {
                sourceDbConnection.rollback();
            }
            if (targetDbConnection != null) {
                targetDbConnection.rollback();
            }
            log.error("Exception while executing processAllTransferSets()", e);
            throw e;
        } finally {
            if (sourceDbConnection != null) {
                sourceDbConnection.close();
            }
            if (targetDbConnection != null) {
                targetDbConnection.close();
            }
        }

    }


    /**
     * Controls the execution of a TransferSet
     * @param srcCon SourceDB Connection
     * @param targetCon TargetDB Connection
     * @param transferSet Transferset
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws EmptyFileException
     * @throws NotAllowedSqlExpressionException
     * @returns The number of processed rows
     */
    private int processTransferSet(Connection srcCon, Connection targetCon, TransferSet transferSet) throws SQLException, IOException, EmptyFileException, NotAllowedSqlExpressionException {
        if (transferSet.deleteAllRows()) {
            deleteDestTableContents(targetCon, transferSet.getOutputQualifiedTableName());
        }
        String selectStatement = extractSingleStatement(transferSet.getInputSqlFile());
        ResultSet rs = createResultSet(srcCon, selectStatement);
        PreparedStatement insertRowStatement = createInsertRowStatement(
                srcCon,
                targetCon,
                rs,
                transferSet);

        int columncount = rs.getMetaData().getColumnCount();
        int batchSize = 5000;
        int k = 0;
        while (rs.next()) {
            transferRow(rs, insertRowStatement, columncount);
            if(k % batchSize == 0) {
                insertRowStatement.executeBatch();
                insertRowStatement.clearBatch();
            }
            k+=1;
        }

        insertRowStatement.executeBatch();
        log.debug("Transfer "+k+" rows and "+columncount+" columns to table "+transferSet.getOutputQualifiedTableName());

        return k;
    }

    /**
     * Copies a row of the source ResultSet to the target table
     * @param rs ResultSet
     * @param insertRowStatement The prepared Insertstatement
     * @param columncount How many columns
     * @throws SQLException
     */
    private void transferRow(ResultSet rs, PreparedStatement insertRowStatement, int columncount) throws SQLException {
        // assign column wise values
        for (int j = 1; j <= columncount; j++) {
            insertRowStatement.setObject(j,rs.getObject(j));
        }
        //insertRowStatement.execute();
        insertRowStatement.addBatch();
    }

    /**
     * Delete the content of the target table
     * @param targetCon TargedDB Connection
     * @param destTableName Qualified Target Table Name (Schema.Table)
     * @throws SQLException
     */
    private void deleteDestTableContents(Connection targetCon, String destTableName) throws SQLException {
        String sqltruncate = "DELETE FROM "+destTableName;
        try {
            PreparedStatement stmt = targetCon.prepareStatement(sqltruncate);
            stmt.execute();
            log.info( "DELETE executed");
        } catch (SQLException e1) {
            log.error( "DELETE FROM TABLE "+destTableName+" failed.", e1);
            throw e1;
        }
    }

    /**
     * Creates the ResultSet with the SelectStatement from the InputFile
     * @param srcCon SourceDB Connection
     * @param sqlSelectStatement The SQL Statement extract from the input-file
     * @return rs Resultset
     * @throws SQLException
     */
    private ResultSet createResultSet(Connection srcCon, String sqlSelectStatement) throws SQLException {
        Statement SQLStatement = srcCon.createStatement();
        ResultSet rs = SQLStatement.executeQuery(sqlSelectStatement);

        return rs;
    }

    /**
     * Prepares the insert Statement. Leaves the Values as ?
     * @param srcCon SourceDB Connection
     * @param targetCon DargetDB Connection
     * @param rs ResultSet
     * @param tSet TransferSet
     * @return The InsertRowStatement
     * @throws SQLException
     */
    private PreparedStatement createInsertRowStatement(Connection srcCon, Connection targetCon, ResultSet rs, TransferSet tSet) {
        ResultSetMetaData meta = null;
        PreparedStatement insertRowStatement = null;

        try {
            meta = rs.getMetaData();

            String insertColNames = buildInsertColumnNames(meta, targetCon, tSet.getOutputQualifiedTableName());
            String valuesList = buildValuesList(meta, tSet);

            String sql = "INSERT INTO " + tSet.getOutputQualifiedTableName() + " ("
                    + insertColNames
                    + ") VALUES ("
                    + valuesList
                    + ")";
            insertRowStatement = targetCon.prepareStatement(sql);

            log.info(String.format(taskName + ": Sql insert statement: [%s]", sql));

        } catch (SQLException g) {
            throw new GretlException(g);
        }

        return insertRowStatement;
    }

    private static String buildValuesList(ResultSetMetaData meta, TransferSet tSet){
        StringBuffer valuesList = new StringBuffer();
        try {
            for (int j = 1; j <= meta.getColumnCount(); j++) {
                if (j > 1) {
                    valuesList.append(", ");
                }

                String colName = meta.getColumnName(j);

                if (tSet.isGeoColumn(colName)) {
                    String func = tSet.wrapWithGeoTransformFunction(colName, "?");
                    valuesList.append(func);
                } else {
                    valuesList.append("?");
                }
            }
        }
        catch(SQLException se){
            throw new GretlException(se);
        }
        return valuesList.toString();
    }

    private static String buildInsertColumnNames(ResultSetMetaData sourceMeta, Connection targetCon, String targetTableName){
        StringBuffer columnNames = new StringBuffer();
        AttributeNameMap colMap = AttributeNameMap.createAttributeNameMap(targetCon, targetTableName);
        try {
            for (int j = 1; j <= sourceMeta.getColumnCount(); j++) {
                if (j > 1) {
                    columnNames.append(", ");
                }

                String srcColName = sourceMeta.getColumnName(j);
                String targetColName = colMap.getAttributeName(srcColName);
                columnNames.append(targetColName);

            }
        }
        catch(SQLException se){
            throw new GretlException(se);
        }
        return columnNames.toString();
    }

    /**
     * Extracts a single statement out of the SQL-file and checks if it fits the conditions.
     * @param targetFile
     * @returnA Select Statement as String
     * @throws FileNotFoundException
     * @throws EmptyFileException
     */
    private String extractSingleStatement(File targetFile) throws IOException {
        String line = null;
        String firstline = null;
        
        line = SqlReader.readSqlStmt(targetFile);
        
        // SqlReader returns null if string (=statement) has zero length.
        // But it does not trim string.
        if (line == null) {
            String msg = "No statement found to execute. Empty file: " + targetFile.getName();
            log.error(msg, new EmptyFileException(msg));
            throw new EmptyFileException(msg);
        }
        
        firstline = line.trim();
        if (firstline.length() > 0) {
            log.info( "Statement found. Length: " + firstline.length() + " characters");
        } else {
            String msg = "No statement found to execute. Empty file: " + targetFile.getName();
            log.error(msg, new EmptyFileException(msg));
            throw new EmptyFileException(msg);
        }
        
        while (line != null) {
            line = SqlReader.nextSqlStmt();
            if (line != null) {
                if (line.trim().length() > 0) {
                    String msg = "There is more then one statement in the file!";
                    log.error(msg, new RuntimeException(msg));
                    throw new RuntimeException(msg);
                }   
            }
        }
        return firstline;
    }

    /**
     * Checks if the Transferset List is not Empty.
     * @param transferSets
     * @throws EmptyListException
     */
    private void assertValidTransferSets(List<TransferSet> transferSets) throws EmptyListException {
        if(transferSets.size() == 0) {
            throw new EmptyListException();
        }

        for(TransferSet ts : transferSets){
            if(!ts.getInputSqlFile().canRead()){
                throw new GretlException("Can not read input sql file at path: " + ts.getInputSqlFile().getPath());
            }
        }
    }



}