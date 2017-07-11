package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.util.EmptyFileException;
import ch.so.agi.gretl.util.NotAllowedSqlExpressionException;
import ch.so.agi.gretl.logging.Logger;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by bjsvwsch on 19.05.17.
 */
public class Db2DbStepTask extends DefaultTask {

    @Input
    public TransactionContext sourceDb;
    @Input
    public TransactionContext targetDb;
    @Input
    public List<TransferSet> transferSet;

    @TaskAction
    public void db2DbStepTask() {

            try {
                new Db2DbStep(sourceDb.getDbConnection(), targetDb.getDbConnection()).processAllTransferSets(transferSet);
                Logger.log(Logger.INFO_LEVEL, "Task start");
            } catch (SQLException e) {
                dbRollback(e);
                Logger.log(Logger.INFO_LEVEL, "SQLException: " + e.getMessage());
                throw new GradleException("Failed to execute Db2DbStep: " + getName(), e);
            } catch (FileNotFoundException e) {
                dbRollback(e);
                Logger.log(Logger.INFO_LEVEL, "FileNotFoundException: " + e.getMessage());
                throw new GradleException("Failed to execute Db2DbStep: " + getName(), e);
            } catch (EmptyFileException e) {
                dbRollback(e);
                throw new GradleException("Failed to execute Db2DbStep: " + getName(), e);
            } catch (NotAllowedSqlExpressionException e) {
                dbRollback(e);
                throw new GradleException("Failed to execute Db2DbStep: " + getName(), e);
            }
            try {
                sourceDb.dbCommit();
                targetDb.dbCommit();
                Logger.log(Logger.INFO_LEVEL, "Transaction successful!");
            } catch (SQLException e) {
                Logger.log(Logger.INFO_LEVEL, "SQLException: " + e.getMessage());
                dbRollback(e);
                throw new GradleException("Failed to execute Db2DbStep: " + getName(), e);

            }
    }

    private void dbRollback(Exception e) {
        try {
            sourceDb.dbRollback();
        } catch (SQLException e1) {
            Logger.log(Logger.DEBUG_LEVEL, "Failed to rollback "+e.getMessage());
        }
    }
}

