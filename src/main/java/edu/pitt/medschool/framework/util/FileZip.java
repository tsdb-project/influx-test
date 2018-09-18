package edu.pitt.medschool.framework.util;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class FileZip {
    private static final Logger logger = LoggerFactory.getLogger(FileZip.class);

    public static void zip(String targetPath, String destinationFilePath, String password) {
        try {
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

            if (password.length() > 0) {
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
                parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
                parameters.setPassword(password);
            }

            ZipFile zipFile = new ZipFile(destinationFilePath);

            File targetFile = new File(targetPath);
            if (targetFile.isFile()) {
                zipFile.addFile(targetFile, parameters);
            } else if (targetFile.isDirectory()) {
                zipFile.addFolder(targetFile, parameters);
            }

        } catch (Exception e) {
            logger.error("File zip failed: {}", Util.stackTraceErrorToString(e));
        }
    }

    public static void unzip(String targetZipFilePath, String destinationFolderPath, String password) {
        try {
            ZipFile zipFile = new ZipFile(targetZipFilePath);
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(password);
            }
            zipFile.extractAll(destinationFolderPath);

        } catch (Exception e) {
            logger.error("File unzip failed: {}", Util.stackTraceErrorToString(e));
        }
    }

    public static void main(String[] args) {

        String targetPath = "/tsdb/output/Dev_Test_(2018-09-14T16.49.17.094)/";
        File file = new File(targetPath);
        String zipFilePath = file.getParent() + "/" + file.getName() + ".zip";
        String unzippedFolderPath = "destination\\folder\\path";
        String password = "";

        FileZip.zip(targetPath, zipFilePath, password);
        FileZip.unzip(zipFilePath, unzippedFolderPath, password);
    }
}
