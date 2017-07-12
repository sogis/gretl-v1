package ch.so.agi.gretl.util;

import java.io.File;

/**
 * Utility-Class to get File extensions
 */
public class FileExtension {

    private FileExtension() {
    }

    /**
     * Gets the extension of the given file.
     *
     * @param inputFile File, which should be checked for the extension
     * @return          file extension (e.g. ".sql")
     */
    public static String getFileExtension(File inputFile)
            throws Exception {

        String filePath =inputFile.getAbsolutePath();
        String[] splittedFilePath = filePath.split("\\.");
        Integer arrayLength=splittedFilePath.length;
        if (arrayLength >=2) {
            String FileExtension = splittedFilePath[arrayLength - 1];
            return FileExtension;
        } else  {
            throw new Exception("Error: File without Fileextension");
        }
    }
}



