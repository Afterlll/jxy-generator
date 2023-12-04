package com.jxy;

import com.jxy.cli.CommandExecutor;

public class Main {
    public static void main(String[] args) {
        CommandExecutor commandExecutor = new CommandExecutor();
        // 测试
//        args = new String[]{"generate", "-a", "-l", "-o"};
//        args = new String[]{"config"};
//        args = new String[]{"list"};
        commandExecutor.doExecute(args);
    }
}
