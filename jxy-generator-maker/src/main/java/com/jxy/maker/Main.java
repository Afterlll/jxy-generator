package com.jxy.maker;

import com.jxy.maker.cli.CommandExecutor;

public class Main {
    public static void main(String[] args) {
        CommandExecutor commandExecutor = new CommandExecutor();
        commandExecutor.doExecute(args);
    }
}
