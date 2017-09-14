package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
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

        log.lifecycle( taskName + ": \n\nStart Db2DbStep. Found "+transferSets.size()+" transferSets. \n" +
                "sourceDb = "+sourceDb.connect().getMetaData().getURL()+", " +
                "user = "+sourceDb.connect().getMetaData().getUserName()+", \n" +
                "targetDb = "+targetDb.connect().getMetaData().getURL()+", " +
                "user = "+targetDb.connect().getMetaData().getUserName()+"\n");

        Connection sourceDbConnection = null;
        Connection targetDbConnection = null;

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
                
                processTransferSet(sourceDbConnection, targetDbConnection, transferSet);
            }
            sourceDbConnection.commit();
            targetDbConnection.commit();
            log.lifecycle(taskName + ": Transfered all Transfersets");
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
            if (sourceDb.connect() != null) {
                sourceDb.connect().close();
            }
            if (targetDb.connect() != null) {
                targetDb.connect().close();
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
     */
    private void processTransferSet(Connection srcCon, Connection targetCon, TransferSet transferSet) throws SQLException, IOException, EmptyFileException, NotAllowedSqlExpressionException {
        if (transferSet.getDeleteAllRows() == true) {
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

        while (rs.next()) {
            transferRow(rs, insertRowStatement, columncount);
        }
        insertRowStatement.executeBatch();
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
        log.info("Try to delete all rows in Table "+destTableName);
        log.debug("SQL-Statement = "+sqltruncate);
        try {
            PreparedStatement stmt = targetCon.prepareStatement(sqltruncate);
            stmt.execute();
            //Test if there are no more Rows in the targettable
            ResultSet rs = targetCon.createStatement().executeQuery("SELECT * FROM " + destTableName);
            if (rs.next()) {
                log.debug( "DELETE FROM TABLE "+destTableName+" failed. There are still Rows in the Target-Table!");
                throw new SQLException();
            }
            else {
                log.info( "DELETE succesfull!");
            }
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
    private PreparedStatement createInsertRowStatement(Connection srcCon, Connection targetCon, ResultSet rs, TransferSet tSet) throws SQLException {
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

            String colName = meta.getColumnName(j);
            columnNames.append(colName);

            if(tSet.isGeoColumn(colName)){
                String func = tSet.wrapWithGeoTransformFunction(colName, "?");
                bindVariables.append(func);
            }
            else {
                bindVariables.append("?");
            }
        }
        log.debug("Transfering table with " + rs.getFetchSize() + " rows and "+meta.getColumnCount() + " columns to table " + tSet.getOutputQualifiedTableName());

        String sql = "INSERT INTO " + tSet.getOutputQualifiedTableName() + " ("
                + columnNames
                + ") VALUES ("
                + bindVariables
                + ")";
        PreparedStatement insertRowStatement = targetCon.prepareStatement(sql);

        log.lifecycle(String.format(taskName + ": Sql insert statement: [%s]", sql));

        return insertRowStatement;
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