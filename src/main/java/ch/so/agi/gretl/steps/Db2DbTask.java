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
    public void db2DbTask() throws Exception {

            try {
                new Db2DbStep().processAllTransferSets(sourceDb, targetDb, transferSet);
                log.info("Task start");
            } catch (Exception e) {
                log.livecycle("Error in Db2DbTask!"+e); //todo log.error("Error in ...", e);
                throw new Exception();
            }
    }

    private void dbRollback(Exception e) {
        try {
            sourceDb.getDbConnection().rollback();
        } catch (SQLException e1) {
            log.debug( "Failed to rollback "+e.getMessage());
        }
    }
}

