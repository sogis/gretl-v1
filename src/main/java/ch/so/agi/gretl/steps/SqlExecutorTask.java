package ch.so.agi.gretl.steps;


import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//todo weiss ich aufgrund des kommentars mehr wie wenn ich die "nackte" klasse ohne kommentar sehe?
// -> Wie ist die beziehung zwischen step und task und wieso?
/**
 * This Class represents the SqlExecutorStep-Task
 */
public class SqlExecutorTask extends DefaultTask {

    public SqlExecutorTask () {
        LogEnvironment.initGradleIntegrated();
        this.log = LogEnvironment.getLogger(this.getClass());
    }

//todo wieso sourceDB? Es gibt keine targetdb -> besser einfach database
    @Input
    private TransactionContext sourceDb;


    @Input
    private List<String> sqlFiles;

    private GretlLogger log;//todo kontrollieren und static machen - hab ich (oliver) ergänzt da es nichht kompiliert hat...


    @TaskAction
    public void sqlExecuterStepTask() { //todo hier gibt's sicher noch einen sprechenderen namen für die methode

        List<File> files = convertToValidatedFileList(sqlFiles);

        try {
            new SqlExecutorStep().execute(sourceDb, files);
            log.info("Task start");
            try {
                sourceDb.dbCommit(); //todo kein connectionhandling im Task
                //todo wo wird sichergestellt dass in jedem Fall die Connection geschlossen wird?
            } catch (SQLException e) {
                log.info("SQLException: " + e.getMessage());
                throw new GradleException("SQLException: " + e.getMessage());
            }
        } catch (Exception e) {
            log.info("Exception: "+e.getMessage());
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

