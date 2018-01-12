package ch.so.agi.gretl.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TestUtil {
    private static final String newline=System.getProperty("line.separator");
    public static void runJob(String jobPath) throws Exception {
        runJob(jobPath, null);
    }

    public static void runJob(String jobPath, GradleVariable[] variables) throws Exception {
        int ret=runJob(jobPath,variables,new StringBuffer(),new StringBuffer());
        assertThat(ret, is(0));
    }
    public static int runJob(String jobPath, GradleVariable[] variables,StringBuffer stderr,StringBuffer stdout) throws Exception {
        if(stderr==null) {
            throw new IllegalArgumentException("stderr must not be null");
        }
        if(stdout==null) {
            throw new IllegalArgumentException("stdout must not be null");
        }
        String varText = "";
        if(variables != null && variables.length > 0){
            StringBuffer buf = new StringBuffer();
            for(GradleVariable var: variables){
                buf.append(" ");
                buf.append(var.buildOptionString());
            }
            varText = buf.toString();
        }
        String tool="gradlew";
        if(System.getProperty("os.name").contains("Windows")){
            tool="gradlew.bat";
        }
        String command = String.format("./%s --init-script ../init.gradle --project-dir %s %s", tool,jobPath, varText);
       // String command = String.format("./gradlew --init-script ../init.gradle --project-dir %s -Pgretltest_dburi=jdbc:postgresql://localhost:5432/gretl", jobPath);
        System.out.println("command:" + command);
        Process p = Runtime.getRuntime().exec(command);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));


        // read the output from the command
        new Thread() {
            public void run() {
                try {
                    String s;
                    while ((s = stdInput.readLine()) != null) {
                        System.out.println(s);
                        if(stdout!=null) {
                            stdout.append(s);
                            stdout.append(newline);
                        }
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();

        // read any errors from the attempted command
        new Thread() {
            public void run() {
                try {
                    String s;
                    while ((s = stdError.readLine()) != null) {
                        stderr.append(s);
                        stderr.append(newline);
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();

        p.waitFor();
        System.out.println(String.format("Here is the standard output of the command [%s]:\n", command));
        System.out.print(stdout);
        System.out.println(String.format("Here is the standard error of the command [%s] (if any):\n", command));
        System.out.print(stderr);

        return p.exitValue();
    }
}
