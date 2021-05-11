package com.task;

import com.task.exceptions.CipherException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Zipper {

    private static final int BUFFER_SIZE = 1024;

    private final CipherMaker cipher;

    /*
    Using method described here: https://www.baeldung.com/java-compress-and-uncompress
    but wrap File[Input/Output]Stream in Cipher[Input/Output]Stream.
     */
    public Zipper(String password) throws CipherException {
        this.cipher = new CipherMaker(password);
    }

    public void zip(List<File> sourceFiles, String outputFileName, File directory) throws IOException, CipherException {
        var outputFile = new File(directory, outputFileName);
        try (var fileOut = new FileOutputStream(outputFile);
             var cipherOut = cipher.outputStream(fileOut);
             var cipheredZipOut = new ZipOutputStream(cipherOut)) {
            for (File file : sourceFiles) {
                zipFile(file, cipheredZipOut);
            }
        }
    }

    public void unzip(File directory, File zippedFile) throws IOException, CipherException {
        try (var fileIn = new FileInputStream(zippedFile);
             var cipherIn = cipher.inputStream(fileIn);
             var cipheredZipIn = new ZipInputStream(cipherIn)) {
            unzip(directory, cipheredZipIn);
        }
    }

    void unzip(File directory, ZipInputStream zipIn) throws IOException {
        var zipEntry = zipIn.getNextEntry();

        if (zipEntry == null) {
            throw new IOException("File empty and/or failed to decrypt. Check password.");
        }
        var buffer = new byte[BUFFER_SIZE];
        while (zipEntry != null) {
            unzipFile(directory, zipEntry, zipIn, buffer);
            zipEntry = zipIn.getNextEntry();
        }
        zipIn.closeEntry();
    }

    private void zipFile(File fileToZip, ZipOutputStream zipOut) throws IOException {
        try (var fileIn = new FileInputStream(fileToZip)) {
            var zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            writeToFile(fileIn, zipOut, new byte[BUFFER_SIZE]);
        }
    }

    private void unzipFile(File destinationDir, ZipEntry zipEntry, ZipInputStream zipIn, byte[] buffer) throws IOException {
        var newFile = newFile(destinationDir, zipEntry);
        if (zipEntry.isDirectory()) {
            if (!newFile.isDirectory() && !newFile.mkdirs()) {
                throw new IOException("Failed to create directory " + newFile);
            }
        } else {
            // fix for Windows-created archives
            var parent = newFile.getParentFile();
            if (!parent.isDirectory() && !parent.mkdirs()) {
                throw new IOException("Failed to create directory " + parent);
            }

            try (var fileOut = new FileOutputStream(newFile)) {
                writeToFile(zipIn, fileOut, buffer);
            }
        }
    }

    private void writeToFile(InputStream in, OutputStream out, byte[] buffer) throws IOException {
        int readBytes;
        while ((readBytes = in.read(buffer)) > 0) {
            out.write(buffer, 0, readBytes);
        }
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        var destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }
}