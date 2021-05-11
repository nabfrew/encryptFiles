package com.task;

import com.task.ArgumentParser.Mode;
import com.task.exceptions.CipherException;
import com.task.exceptions.InvalidArgumentException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.task.ArgumentParser.MODE_FLAG;
import static com.task.ArgumentParser.MODE_VALUE_DECRYPT;
import static com.task.ArgumentParser.MODE_VALUE_ENCRYPT;
import static com.task.ArgumentParser.Mode.DECRYPT;
import static com.task.ArgumentParser.Mode.ENCRYPT;
import static com.task.ArgumentParser.NAME_FLAG;
import static com.task.ArgumentParser.OUTPUT_DIRECTORY_FLAG;
import static com.task.ArgumentParser.PASSWORD_FLAG;

public class Command {
    private Mode mode;
    private final List<File> files = new ArrayList<>();
    private String password;
    private String name;
    private File outputDirectory;

    protected Command() {
    }

    protected Command(Mode mode) {
        this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }

    public List<File> getFiles() {
        return files;
    }

    protected void addFile(String file) {
        files.add(new File(file));
    }

    public String getPassword() {
        return password;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    private void setMode(String mode) throws InvalidArgumentException {
        if (MODE_VALUE_ENCRYPT.contains(mode)) {
            this.mode = ENCRYPT;
            return;
        } else if (MODE_VALUE_DECRYPT.contains(mode)) {
            this.mode = DECRYPT;
            return;
        }
        throw new InvalidArgumentException("Invalid mode");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProperty(String propertyFlag, String propertyValue) throws InvalidArgumentException {
        if (MODE_FLAG.contains(propertyFlag)) {
            setMode(propertyValue);
        } else if (PASSWORD_FLAG.contains(propertyFlag)) {
            password = propertyValue;
        } else if (NAME_FLAG.contains(propertyFlag)) {
            name = propertyValue;
        } else if (OUTPUT_DIRECTORY_FLAG.contains(propertyFlag)) {
            outputDirectory = new File(propertyValue).getAbsoluteFile();
        } else {
            throw new InvalidArgumentException("Unrecognised argument: " + propertyFlag + ".");
        }
    }

    public void execute() throws CipherException, IOException {
        var zipper = new Zipper(password);

        if (mode.equals(ENCRYPT)) {
            zipper.zip(files, name, outputDirectory);
        }
        if (mode.equals(DECRYPT)) {
            for (var file : files) {
                zipper.unzip(new File(outputDirectory, file.getName()), file);
            }
        }
    }

    public void setDefaultOutputDirectory() {
        if (mode == ENCRYPT) {
            outputDirectory = new File("").getAbsoluteFile();
        }
        if (mode == DECRYPT) {
            outputDirectory = new File("decrypted").getAbsoluteFile();
        }
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public String toString() {
        var string = new StringBuilder();
        if (mode != null) {
            string.append("Mode:\n\t").append(mode);
        }
        if (password != null) {
            string.append("\nPassword:\n\t").append("*".repeat(password.length()));
        }
        if (outputDirectory != null) {
            string.append("\nOutput directory:\n\t").append(outputDirectory);
        }
        if (name != null && ENCRYPT.equals(mode)) {
            string.append("\nOutput filename:\n\t").append(name);
        }
        if (!files.isEmpty()) {
            string.append("\nSource file(s):");
            for (File file : files) {
                string.append("\n\t").append(file.getName());
            }
        }

        return string.toString();
    }
}
