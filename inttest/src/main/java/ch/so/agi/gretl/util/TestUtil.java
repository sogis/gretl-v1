package ch.so.agi.gretl.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TestUtil {

    public static void runJob(String jobPath) throws Exception {
        runJob(jobPath, null);
    }

    public static void runJob(String jobPath, GradleVariable[] variables) throws Exception {

        String varText = "";
        if(variables != null && variables.length > 0){
            StringBuffer buf = new StringBuffer();
            for(GradleVariable var: variables){
                buf.append(" ");
                buf.append(var.buildOptionString());
            }
            varText = buf.toString();
        }

        String command = String.format("./gradlew --project-dir %s %s", jobPath, varText);
        Process p = Runtime.getRuntime().exec(command);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        String s;

        // read the output from the command
        System.out.println(String.format("Here is the standard output of the command [%s]:\n", command));
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

        // read any errors from the attempted command
        System.out.println(String.format("Here is the standard error of the command [%s] (if any):\n", command));
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }

        p.waitFor();

        assertThat(p.exitValue(), is(0));
    }
}
