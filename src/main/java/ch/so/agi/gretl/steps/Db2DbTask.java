package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.logging.Logger;
import ch.so.agi.gretl.util.EmptyFileException;
import ch.so.agi.gretl.util.NotAllowedSqlExpressionException;
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
public class Db2DbTask extends DefaultTask {

    private GretlLogger log;
    public Db2DbTask () {
        LogEnvironment.initGradleIntegrated();
        this.log = LogEnvironment.getLogger(this.getClass());
    }

    @Input
    public TransactionContext sourceDb;
    @Input
    public TransactionContext targetDb;
    @Input
    public List<TransferSet> transferSet;

    @TaskAction
    public void db2DbTask() {

            try {
                new Db2DbStep().processAllTransferSets(sourceDb, targetDb, transferSet);
                log.info("Task start");
            } catch (SQLException e) {
                dbRollback(e);
                log.info( "SQLException: " + e.getMessage());
                throw new GradleException("Failed to execute Db2DbStep: " + getName(), e);
            } catch (FileNotFoundException e) {
                dbRollback(e);
                log.info( "FileNotFoundException: " + e.getMessage());
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
                log.info( "Transaction successful!");
            } catch (SQLException e) {
                log.info( "SQLException: " + e.getMessage());
                dbRollback(e);
                throw new GradleException("Failed to execute Db2DbStep: " + getName(), e);
            }
    }

    private void dbRollback(Exception e) {
        try {
            sourceDb.dbRollback();
        } catch (SQLException e1) {
            log.debug( "Failed to rollback "+e.getMessage());
        }
    }
}

