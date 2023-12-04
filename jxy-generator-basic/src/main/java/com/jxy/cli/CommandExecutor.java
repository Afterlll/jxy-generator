package com.jxy.cli;

import com.jxy.cli.command.ConfigCommand;
import com.jxy.cli.command.GenerateCommand;
import com.jxy.cli.command.ListCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * 命令执行器，负责绑定所有子命令，并且提供执行其他子命令的方式
 */
@Command(name = "jxy", description = "命令执行器", mixinStandardHelpOptions = true)
public class CommandExecutor implements Runnable {

    private final CommandLine commandLine;

    {
        // 绑定子命令
        commandLine = new CommandLine(this)
                .addSubcommand(ConfigCommand.class)
                .addSubcommand(GenerateCommand.class)
                .addSubcommand(ListCommand.class);
    }

    @Override
    public void run() {
        // 在没有输入命令时，给出友好提示
        System.out.println("请输入具体命令，或者输入 --help 查看命令提示");
    }

    /**
     * 执行命令
     * @param args 参数
     * @return 执行结果
     */
    public Integer doExecute(String[] args) {
        return commandLine.execute(args);
    }

}
