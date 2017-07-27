package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by bjsvwsch on 19.05.17.
 */
public class Db2DbTask extends DefaultTask {



    private static GretlLogger log;

    static {
        LogEnvironment.initGradleIntegrated();
        log = LogEnvironment.getLogger(Db2DbTask.class);
    }

    @Input
    public TransactionContext sourceDb;
    @Input
    public TransactionContext targetDb;
    @Input
    public List<TransferSet> transferSet;

    @TaskAction
    public void db2DbTask() throws Exception {

            try {
                Db2DbStep step = new Db2DbStep();
                step.processAllTransferSets(sourceDb, targetDb, transferSet);
                log.info("Task start");
            } catch (Exception e) {
                log.error("Exception in creating / invoking Db2DbStep in Db2DbTask", e);
                throw e;
            }
    }

    //todo weg damit, oder?
    private void dbRollback(Exception e) {
        try {
            sourceDb.getDbConnection().rollback();
        } catch (SQLException e1) {
            log.debug( "Failed to rollback "+e.getMessage());
        }
    }
}

