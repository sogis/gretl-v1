package ch.so.agi.gretl.util;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertFalse;

/**
 * Tests for FileExtension-Class
 */
public class FileExtensionTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void getFileExtension() throws Exception {
        File sqlFile =  folder.newFile("query.sql");

        //todo Assert.assertEquals(....)
        if(FileExtension.getFileExtension(sqlFile).equals("sql")){

        }  else {
            assertFalse("FileExtension not working properly", true);
        }
    }

    @Test
    public void missingFileExtension() throws Exception {
        File sqlFile = folder.newFile("file");
        try {
            FileExtension.getFileExtension(sqlFile);
            Assert.fail();
        } catch (Exception e) {
//todo welche Exception wird erwartet - generelles "catch all" ist nicht genau genug....
        }

    }

    @Test
    public void multipleFileExtension() throws Exception {
        File sqlFile = folder.newFile("file.ext1.ext2");

        //todo Assert.assertEquals(....)

        if(FileExtension.getFileExtension(sqlFile).equals("ext2")){

        } else {
            assertFalse("FileExtension not working properly", true);
        }

    }

    @Test
    public void strangeFileNameExtension() throws Exception {
        File sqlFile = folder.newFile("c:\\file");
        try {
            FileExtension.getFileExtension(sqlFile);
            Assert.fail();
        } catch (Exception e) {
//todo welche Exception wird erwartet - generelles "catch all" ist nicht genau genug....
        }

    }

}