package ch.so.agi.gretl.jenkins;

import com.google.common.base.Optional;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.*;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/*
 * Run tests in a fix order. This is not a good practice.
 * Needed here to split one big test into smaller parts.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JenkinsTest {

    private static int AMOUNT_OF_CONFIGURED_GRETL_JOBS = 1;

    private JenkinsServer jenkins;

    @Before
    public void init() throws Exception {
        String jenkinsUri = System.getProperty("gretltest_jenkins_uri");
        System.out.println("jenkinsUri: " + jenkinsUri);
        // TODO uri must start with http
        String jenkinsUser = System.getProperty("gretltest_jenkins_user");
        System.out.println("jenkinsUser: " + jenkinsUser);


        try {
            jenkins = new JenkinsServer(new URI(jenkinsUri), jenkinsUser, System.getProperty("gretltest_jenkins_pwd"));
            //jenkins = new JenkinsServer(new URI("http://localhost:8080"), "admin", "admin1234");
            Map<String, Job> jobs = jenkins.getJobs();
            System.out.println(jobs.size() + " jobs: ");
            for (String jobName: jobs.keySet()) {
                System.out.println(jobName);
            }
        } catch (Exception e) {
            System.err.println("=================================================");
            System.err.println("Make sure that Jenkins is running and accessible.");
            System.err.println("Start with Docker: docker/start-jenkins.sh");
            System.err.println("=================================================");
            throw e;
        }
    }

    /**
     * Delete all jobs that are not in administration folder.
     * @throws IOException
     */
    @Test
    public void test01cleanup() throws IOException {
        // when
        Map<String, Job> jobs = jenkins.getJobs();
        for (String jobName: jobs.keySet()) {
            if (!"administration".equals(jobName)) {
                jenkins.deleteJob(jobName);
            }
        }
        jobs = jenkins.getJobs();

        // then
        assertThat(jobs.size(), is(1));
    }

    @Test
    public void test02checkThatAdminJobExists() throws IOException {
        // when
        Job job = jenkins.getJobs().get("administration");

        // then
        Optional<FolderJob> admF = jenkins.getFolderJob(job);
        assertThat(admF.isPresent(), is(true));
        FolderJob admJob = admF.get();
        assertThat(admJob.getJobs().size(), is(1));
        assertThat(admJob.getJobs().containsKey("gretl-job-generator"), is(true));
    }

    @Test
    public void test03shouldGenerateGretlJobs() throws Exception {
        // given
        Job job = jenkins.getJobs().get("administration");
        Optional<FolderJob> admF = jenkins.getFolderJob(job);
        FolderJob admJob = admF.get();
        Job seeder = admJob.getJobs().get("gretl-job-generator");

        // when
        seeder.build();

        // wait for job to complete
        TimeUnit.SECONDS.sleep(5);
        if (jenkins.getJobs().size() == 1) {
            TimeUnit.SECONDS.sleep(10);
        }

        // then
        assertThat(jenkins.getJobs().size(), is(AMOUNT_OF_CONFIGURED_GRETL_JOBS + 1));
    }

    @Test
    public void test04shouldHaveGeneratedIliValidatorJob() throws IOException {
        // when
        Job job = jenkins.getJobs().get("iliValidator");

        // then
        assertThat(job, not(nullValue()));
    }

    @Test
    public void test05shouldBuildIliValidatorJob() throws Exception {
        // given
        Job job = jenkins.getJobs().get("iliValidator");

        // when
        QueueReference queueReference = job.build();

        // then
        assertThat("QueueReference not found.", queueReference, not(nullValue()));
        QueueItem queueItem = jenkins.getQueueItem(queueReference);
        assertThat("QueueItem not found.", queueItem, not(nullValue()));

        TimeUnit.SECONDS.sleep(5);

        Build build = job.details().getLastBuild();
        assertThat("Build not found.", build, not(nullValue()));

        System.out.println("build: " + build.details().getId());

        do {
            TimeUnit.SECONDS.sleep(10);
        } while (build.details().isBuilding());

        assertThat(build.details().getResult(), is(BuildResult.SUCCESS));
    }
}
