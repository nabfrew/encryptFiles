package com.task;

import com.task.exceptions.InvalidArgumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

import static com.task.ArgumentParser.HELP_FLAG;
import static com.task.ArgumentParser.MODE_FLAG;
import static com.task.ArgumentParser.MODE_VALUE_DECRYPT;
import static com.task.ArgumentParser.Mode.DECRYPT;
import static com.task.ArgumentParser.Mode.ENCRYPT;
import static com.task.ArgumentParser.Mode.HELP;
import static com.task.ArgumentParser.NAME_FLAG;
import static com.task.ArgumentParser.OUTPUT_DIRECTORY_FLAG;
import static com.task.ArgumentParser.PASSWORD_FLAG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArgumentParserTest {

    private static final String MODE_FLAG_TERSE = MODE_FLAG.get(0);
    private static final String PASSWORD_FLAG_TERSE = PASSWORD_FLAG.get(0);
    private static final String DECRYPT_MODE_TERSE = MODE_VALUE_DECRYPT.get(0);
    private static final String NAME_FLAG_TERSE = NAME_FLAG.get(0);
    private static final String HELP_FLAG_TERSE = HELP_FLAG.get(0);
    private static final String DIRECTORY_FLAG_TERSE = OUTPUT_DIRECTORY_FLAG.get(0);
    private static final String PASSWORD = "hunter2";
    private static final String NAME = "secret_Archive";
    private static final String DUMMY_FILE_PATH = "filepath";
    private static final String DIRECTORY = "directory";
    private File tmpDir;

    @BeforeEach
    void setUp(@TempDir Path path) {
        tmpDir = path.toAbsolutePath().toFile();
    }
    // Valid inputs:

    @Test
    void defaultsToEnrypt() throws InvalidArgumentException {
        String[] args = {PASSWORD_FLAG_TERSE, PASSWORD, DUMMY_FILE_PATH};
        var command = new ArgumentParser(args).parse();
        assertEquals(ENCRYPT, command.getMode());
        assertEquals(PASSWORD, command.getPassword());
    }

    @Test
    void setsDecrypt() throws InvalidArgumentException {
        String[] args = {MODE_FLAG_TERSE, DECRYPT_MODE_TERSE, PASSWORD_FLAG_TERSE, PASSWORD, DUMMY_FILE_PATH};

        var command = new ArgumentParser(args).parse();
        assertEquals(DECRYPT, command.getMode());
        assertEquals(PASSWORD, command.getPassword());
        assertEquals(Collections.singletonList(new File(DUMMY_FILE_PATH)), command.getFiles());
    }

    @Test
    void setsDirectory() throws InvalidArgumentException {
        String[] args = {DIRECTORY_FLAG_TERSE, new File(tmpDir, DIRECTORY).getAbsolutePath(), PASSWORD_FLAG_TERSE, PASSWORD, DUMMY_FILE_PATH};

        var command = new ArgumentParser(args).parse();
        assertEquals(new File(tmpDir, DIRECTORY).getAbsolutePath(), command.getOutputDirectory().toString());
        assertEquals(PASSWORD, command.getPassword());
        assertEquals(Collections.singletonList(new File(DUMMY_FILE_PATH)), command.getFiles());
    }

    @Test
    void setsName() throws InvalidArgumentException {
        String[] args = {MODE_FLAG_TERSE, DECRYPT_MODE_TERSE, NAME_FLAG_TERSE, NAME, PASSWORD_FLAG_TERSE, PASSWORD, DUMMY_FILE_PATH};

        var command = new ArgumentParser(args).parse();
        assertEquals(DECRYPT, command.getMode());
        assertEquals(NAME, command.getName());
        assertEquals(PASSWORD, command.getPassword());
        assertEquals(Collections.singletonList(new File(DUMMY_FILE_PATH)), command.getFiles());
    }

    @Test
    void setsHelp() throws InvalidArgumentException {
        String[] args = {HELP_FLAG_TERSE};

        var command = new ArgumentParser(args).parse();
        assertEquals(HELP, command.getMode());
        assertNull(command.getPassword());
        assertEquals(new ArrayList<>(), command.getFiles());
    }

    // Invalid inputs:

    @Test
    void noArgument() {
        String[] args = {};
        assertThrows(InvalidArgumentException.class, () -> new ArgumentParser(args).parse());
    }

    @Test
    void missingPasswordFlag() {
        String[] args = {DUMMY_FILE_PATH};
        assertThrows(InvalidArgumentException.class, () -> new ArgumentParser(args).parse());
    }

    @Test
    void missingPasswordFlagWithManyArgs() { //So that invalid input cannot be deduced from nr. of args.
        String[] args = {MODE_FLAG_TERSE, DECRYPT_MODE_TERSE, DUMMY_FILE_PATH, DUMMY_FILE_PATH};
        assertThrows(InvalidArgumentException.class, () -> new ArgumentParser(args).parse());
    }

    @Test
    void missingFilePath() {
        String[] args = {PASSWORD_FLAG_TERSE, PASSWORD};
        assertThrows(InvalidArgumentException.class, () -> new ArgumentParser(args).parse());
    }

    @Test
    void missingFilePathWithManyArguments() { //So that invalid input cannot be deduced from nr. of args.
        String[] args = {PASSWORD_FLAG_TERSE, PASSWORD, MODE_FLAG_TERSE, DECRYPT_MODE_TERSE};
        assertThrows(InvalidArgumentException.class, () -> new ArgumentParser(args).parse());
    }

    @Test
    void missingArgumentValue() {
        String[] args = {PASSWORD_FLAG_TERSE, PASSWORD, MODE_FLAG_TERSE};
        assertThrows(InvalidArgumentException.class, () -> new ArgumentParser(args).parse());
    }

    /*
    TODO paramaterise for terse/verbose args?
     */
}