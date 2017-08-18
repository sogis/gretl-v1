package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
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

        String taskName = ((Task)this).getName();
        convertToAbsolutePaths(transferSets);

        try {
            Db2DbStep step = new Db2DbStep(taskName);
            step.processAllTransferSets(sourceDb, targetDb, transferSets);
            log.info("Task start");
        } catch (Exception e) {
            log.error("Exception in creating / invoking Db2DbStep in Db2DbTask", e);

            GradleException gradleEx = TaskUtil.toGradleException(e);
            throw gradleEx;
        }
    }

    private void convertToAbsolutePaths(List<TransferSet> transferSets){

        for(TransferSet ts : transferSets){
            File configured = ts.getInputSqlFile();
            File absolutePath = TaskUtil.createAbsolutePath(configured, ((Task)this).getProject());
            ts.setInputSqlFile(absolutePath);
        }
    }

}

