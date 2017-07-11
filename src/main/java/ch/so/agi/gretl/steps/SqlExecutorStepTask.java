package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.logging.Logger;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * This Class represents the SqlExecutorStep-Task
 */
public class SqlExecutorStepTask extends DefaultTask {

    @Input
    public TransactionContext sourceDb;


    @Input
    public List<File> sqlFiles;


    @TaskAction
    public void sqlExecuterStepTask() {

        try {
            new SqlExecutorStep().execute(sourceDb.getDbConnection(),sqlFiles);
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
}

