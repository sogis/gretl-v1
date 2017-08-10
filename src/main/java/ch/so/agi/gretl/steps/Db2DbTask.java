package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.ExConverter;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.util.List;

public class Db2DbTask extends DefaultTask {



    private static GretlLogger log;

    static {
        LogEnvironment.initGradleIntegrated();
        log = LogEnvironment.getLogger(Db2DbTask.class);
    }

    @Input
    public Connector sourceDb;
    @Input
    public Connector targetDb;
    @Input
    public List<TransferSet> transferSets;

    @TaskAction
    public void db2DbTask() throws Exception {

        String taskName = this.getName();

            try {
                Db2DbStep step = new Db2DbStep();
                step.processAllTransferSets(sourceDb, targetDb, transferSets);
                log.info("Task start");
            } catch (Exception e) {
                log.error("Exception in creating / invoking Db2DbStep in Db2DbTask", e);

                GradleException gradleEx = ExConverter.toGradleException(e);
                throw gradleEx;
            }
    }

}

