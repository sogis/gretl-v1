package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.logging.Logger;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This Class represents the SqlExecutorStep-Task
 */
public class SqlExecutorTask extends DefaultTask {

    @Input
    public TransactionContext sourceDb;


    @Input
    public List<String> sqlFiles;


    @TaskAction
    public void sqlExecuterStepTask() {

        List<File> files = convertToValidatedFileList(sqlFiles);

        try {
            new SqlExecutorStep().execute(sourceDb, files);
            Logger.log(Logger.INFO_LEVEL,"Task start");
            try {
                sourceDb.dbCommit();
            } catch (SQLException e) {
                Logger.log(Logger.INFO_LEVEL, "SQLException: " + e.getMessage());
                throw new GradleException("SQLException: " + e.getMessage());
            }
        } catch (Exception e) {
            Logger.log(Logger.INFO_LEVEL,"Exception: "+e.getMessage());
            throw new GradleException("SqlExecutorStep: "+e.getMessage());
        }
    }

    private static List<File> convertToValidatedFileList(List<String> filePaths){

        List<File> files = new ArrayList<File>();

        for(String filePath : filePaths)
        {
            if(filePath == null || filePath.length() == 0)
                throw new IllegalArgumentException("Filepaths must not be null or empty");

            File file = new File(filePath);

            if(!file.canRead())
                throw new IllegalArgumentException("Can not read the file at path: " + filePath);

            files.add(new File(filePath));
        }

        return files;
    }
}

