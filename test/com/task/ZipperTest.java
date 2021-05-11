package com.task;

import com.task.exceptions.CipherException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZipperTest {

    private static final Random RANDOM = new Random();
    private static final String PASSWORD = "hunter2";
    private static final String ZIP_FILE_NAME = "zipped";
    private File tmpDir;

    @BeforeEach
    void setUp(@TempDir Path path) {
        tmpDir = path.toAbsolutePath().toFile();
    }

    @Test
    void zippingEncryptsFile() throws IOException, CipherException {
        var files = generateFiles(1, 16);
        var zipper = new Zipper(PASSWORD);
        zipper.zip(files, ZIP_FILE_NAME, tmpDir);
        assertThrows(IOException.class, () -> unencryptedUnzip(tmpDir, new File(tmpDir, ZIP_FILE_NAME), zipper));
    }

    /*
    Generates files named 'fileNr[file_number]'.
    zips, and unzips.
    verifies that decrypted files are unchanged.
    */
    @Test
    void encryptionRoundTripUnchanged() throws IOException, CipherException {
        int nrOfFiles = 20;
        long fileSize = 60_000;

        var files = generateFiles(nrOfFiles, fileSize);

        var zipper = new Zipper(PASSWORD);
        zipper.zip(files, ZIP_FILE_NAME, tmpDir);
        zipper.unzip(new File(tmpDir, "decrypted_zipped"), new File(tmpDir, ZIP_FILE_NAME));

        verifyUnchanged(files);
    }

    @Test
    void failsToDecryptWithIncorrectPassword() throws IOException, CipherException {
        var files = generateFiles(1, 16);
        var zipper = new Zipper(PASSWORD);
        zipper.zip(files, ZIP_FILE_NAME, tmpDir);

        var incorrectPasswordZipper = new Zipper("Password123");
        assertThrows(IOException.class, () ->
                incorrectPasswordZipper.unzip(new File(tmpDir, "decrypted_zipped"), new File(tmpDir, ZIP_FILE_NAME)));
    }

    private void verifyUnchanged(List<File> files) throws IOException {
        for (var file : files) {
            try (var originalInputStream = new FileInputStream(file);
                 var decryptedInputStream = new FileInputStream(new File(new File(tmpDir, "decrypted_zipped"), file.getName()))) {
                byte[] originalBuffer = new byte[1024];
                byte[] decryptedBuffer = new byte[1024];
                int originalBytes;
                int decryptedBytes;
                while ((originalBytes = originalInputStream.read(originalBuffer)) > 0) {
                    decryptedBytes = decryptedInputStream.read(decryptedBuffer);
                    assertEquals(originalBytes, decryptedBytes);
                    assertArrayEquals(originalBuffer, decryptedBuffer);
                }
            }
        }
    }

    private ArrayList<File> generateFiles(int nrOfFiles, long fileSize) throws IOException {
        var files = new ArrayList<File>();
        for (var i = 0; i < nrOfFiles; i++) {
            String fileName = "fileNr" + i;
            writeFile(fileSize, fileName);
            files.add(new File(tmpDir, fileName));
        }
        return files;
    }

    private void writeFile(long fileSize, String fileName) throws IOException {
        var outputStream = new FileOutputStream(new File(tmpDir, fileName));
        long bytesWritten = 0;
        int bufferSize = 1024;
        while (bytesWritten <= fileSize) {
            int bytesToWrite = (int) Math.min(fileSize, bufferSize);
            outputStream.write(randomBytes(bytesToWrite));
            bytesWritten += bytesToWrite;
        }
    }

    private void unencryptedUnzip(File directory, File zippedFile, Zipper zipper) throws IOException {
        try (var fileIn = new FileInputStream(zippedFile);
             var zipIn = new ZipInputStream(fileIn)) {
            zipper.unzip(directory, zipIn);
        }
    }

    private byte[] randomBytes(long nrBytes) {
        byte[] bytes = new byte[(int) nrBytes];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
}