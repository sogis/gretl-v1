package ch.so.agi.gretl.steps;


import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * This Class represents the the Task which executes the SQLExecutorStep
 * Only this Class should execute the SQLExecutorStep. Users must use this Class to access SQLExecutorStep
 */
public class SqlExecutorTask extends DefaultTask {
    private static GretlLogger log;

    public SqlExecutorTask () {
        LogEnvironment.initGradleIntegrated();
        this.log = LogEnvironment.getLogger(this.getClass());
    }


    @Input
    public TransactionContext database;


    @Input
    public List<String> sqlFiles;


    @TaskAction
    public void executeSQLExecutor() {

        if (sqlFiles==null) {
            throw new GradleException("sqlFiles is null");
        }

        List<File> files = convertToValidatedFileList(sqlFiles);

        try {

            new SqlExecutorStep().execute(database, files);
            log.info("Task start");
        } catch (Exception e) {
            log.info("Exception: "+e.getMessage());
            throw new GradleException("SqlExecutorStep: "+e.getMessage());
        }
    }

    private static List<File> convertToValidatedFileList(List<String> filePaths){

        List<File> files = new ArrayList<>();

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

