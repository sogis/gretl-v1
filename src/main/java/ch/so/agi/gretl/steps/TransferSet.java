package ch.so.agi.gretl.steps;

import java.io.File;

/**
 * Created by bjsvwsch on 04.05.17.
 */
public class TransferSet {


    private boolean deleteAllRows;
    private File inputSqlFile;
    private String outputQualifiedSchemaAndTableName;


    public TransferSet(boolean deleteAllRows, String inputSqlFilePath, String outputQualifiedSchemaAndTableName){
        this.deleteAllRows = deleteAllRows;

        if(inputSqlFilePath == null || inputSqlFilePath.length() == 0)
            throw new IllegalArgumentException("inputSqlFilePath must not be null or empty");

        this.inputSqlFile = new File(inputSqlFilePath);
        if(!this.inputSqlFile.canRead())
            throw new IllegalArgumentException("Can not read the file: " + inputSqlFilePath);

        if(outputQualifiedSchemaAndTableName == null || outputQualifiedSchemaAndTableName.length() == 0)
            throw new IllegalArgumentException("outputQualifiedSchemaAndTableName must not be null or empty");

        this.outputQualifiedSchemaAndTableName = outputQualifiedSchemaAndTableName;
    }

    public boolean getDeleteAllRows() {
        return deleteAllRows;
    }

    public File getInputSqlFile() { return inputSqlFile; }

    public String getOutputQualifiedSchemaAndTableName() {
        return outputQualifiedSchemaAndTableName;
    }
}
