package com.task;

import com.task.exceptions.InvalidArgumentException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.task.ArgumentParser.Mode.ENCRYPT;
import static com.task.ArgumentParser.Mode.HELP;

public record ArgumentParser(String[] args) {
    public enum Mode {
        HELP,
        ENCRYPT,
        DECRYPT
    }

    protected static final List<String> MODE_FLAG = Arrays.asList("-m", "-mode");
    protected static final List<String> MODE_VALUE_ENCRYPT = Arrays.asList("e", "encrypt");
    protected static final List<String> MODE_VALUE_DECRYPT = Arrays.asList("d", "decrypt");
    protected static final List<String> PASSWORD_FLAG = Arrays.asList("-p", "-password");
    protected static final List<String> HELP_FLAG = Arrays.asList("-h", "-help");
    protected static final List<String> NAME_FLAG = Arrays.asList("-n", "-name");
    protected static final List<String> OUTPUT_DIRECTORY_FLAG = Arrays.asList("-d", "-directory");

    public Command parse() throws InvalidArgumentException {

        if (args == null || args.length == 0 || (args.length < 3 && !HELP_FLAG.contains(args[0]))) {
            throw new InvalidArgumentException("Password and file path are required");
        }
        if (HELP_FLAG.contains(args[0])) {
            return new Command(HELP);
        }

        var command = new Command();
        int firstFileIndex = parseFlags(args, command);
        if (command.getPassword() == null) {
            throw new InvalidArgumentException("No password set.");
        }
        setDefaults(command);

        for (var i = firstFileIndex; i < args.length; i++) {
            command.addFile(args[i]);
        }

        return command;
    }

    private void setDefaults(Command command) {
        if (command.getMode() == null) {
            command.setMode(ENCRYPT);
        }
        if (command.getName() == null) {
            command.setName(UUID.randomUUID().toString());
        }
        if (command.getOutputDirectory() == null) {
            command.setDefaultOutputDirectory();
        }
    }

    private int parseFlags(String[] args, Command command) throws InvalidArgumentException {
        for (var i = 0; i < args.length; i += 2) {
            if (!args[i].startsWith("-")) {
                return i;
            }
            if (i + 2 > args.length) {
                throw new InvalidArgumentException("property value missing for " + args[i]);
            }
            command.setProperty(args[i], args[i + 1]);
        }
        throw new InvalidArgumentException("No file path specified.");
    }
}
