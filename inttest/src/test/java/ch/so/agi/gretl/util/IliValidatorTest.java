package ch.so.agi.gretl.util;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class IliValidatorTest {

    @Test
    public void test() {

        String s = null;

        try {
            Process p = Runtime.getRuntime().exec("../gradlew --project-dir jobs/iliValidator");

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            // read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }

            p.waitFor();
            System.exit(p.exitValue());
        }
        catch (Exception e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
