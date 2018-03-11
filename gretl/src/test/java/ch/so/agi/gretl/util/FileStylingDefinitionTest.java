package ch.so.agi.gretl.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class FileStylingDefinitionTest {
	private static final String TEST_IN = "src/test/data/FileStylingDefinition/";

    @Test
    public void wrongEncodingThrowsException() throws Exception {
        File inputfile = new File(TEST_IN + "test.txt");
        try {
            FileStylingDefinition.checkForUtf8(inputfile);
            Assert.fail();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void rightEncoding() throws Exception {
        File inputfile = new File(TEST_IN + "test_utf8.txt");
        FileStylingDefinition.checkForUtf8(inputfile);
    }

    @Test
    public void fileWithBOMThrowsException() throws Exception {
        File inputfile = new File(TEST_IN + "query_with_bom.sql");
        try {
            FileStylingDefinition.checkForBOMInFile(inputfile);
        } catch (GretlException e) {
            Assert.assertEquals("file with unallowed BOM", e.getType());
        }
    }

    @Test
    public void passingOnFileWithoutBOM() throws Exception {
        File inputfile = new File(TEST_IN + "test_utf8.txt");
        FileStylingDefinition.checkForBOMInFile(inputfile);
    }
}
