package ch.so.agi.gretl.util;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

public class SqlReaderTest {
    private static GretlLogger log;

    static{
        LogEnvironment.initStandalone();
        log = LogEnvironment.getLogger(SqlReaderTest.class);
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void lineCommentsCleanRemoved() throws Exception {

        String lineComment = "--This is a singleline comment.";
        String statement = "select \"user\", \"admin\", \"alias\" from sqlkeywords";

        String wholeStatement = lineComment + "\n" + statement;

        File sqlFile = TestUtil.createFile(folder, wholeStatement, "statementIsUnchanged.sql");

        String parsedStatement = SqlReader.readSqlStmt(sqlFile);

        Assert.assertEquals("Line comment must be removed without changing the statement itself", statement, parsedStatement);
    }

    @Ignore
    @Test
    public void blockCommentsCleanRemoved() throws Exception {

        String blockComment = "/*This is a multiline\nblock comment. */";
        String statement = "select \"user\", \"admin\", \"alias\" from sqlkeywords";

        String wholeStatement = blockComment + "\n" + statement;

        File sqlFile = TestUtil.createFile(folder, wholeStatement, "statementIsUnchanged.sql");

        String parsedStatement = SqlReader.readSqlStmt(sqlFile);

        Assert.assertEquals("Block comment must be removed without changing the statement itself", statement, parsedStatement);
    }
}
