package ch.so.agi.gretl.jenkins;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.QueueReference;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class JenkinsTest {

    private JenkinsServer jenkins;

    @Before
    public void init() throws Exception {
        try {
            jenkins = new JenkinsServer(new URI("http://localhost:8080"), "admin", "admin1234");
            jenkins.getJobs();
        } catch (Exception e) {
            System.err.println("=================================================");
            System.err.println("Make sure that Jenkins is running and accessible.");
            System.err.println("Start with Docker: docker/start-jenkins.sh");
            System.err.println("=================================================");
            throw e;
        }
    }

    @Test
    public void testGetJobs() throws IOException {
        // when
        Map<String, Job> jobs = jenkins.getJobs();

        // then
        assertThat(jobs.size(), is(1));
    }

    @Test
    public void testGetJob() throws IOException {
        // when
        Job job = jenkins.getJobs().get("administration/gretl-job-generator");

        // then
        assertThat(job.getName(), is("administration/gretl-job-generator"));
    }

    @Test
    public void testBuildJob() throws IOException {
        // when
        QueueReference result = jenkins.getJobs().get("administration/gretl-job-generator").build();

        // then
        assertThat(result.getQueueItemUrlPart(), is("http://localhost:8080/queue/item/1/"));
    }
}
