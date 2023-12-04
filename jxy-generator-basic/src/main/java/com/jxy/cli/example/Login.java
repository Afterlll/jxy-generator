package com.jxy.cli.example;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;


import java.lang.reflect.Field;
import java.util.concurrent.Callable;

// 命令行交互
@Command(subcommands = {ASCIIArt.class}, mixinStandardHelpOptions = true) // 子命令
class Login implements Callable<Integer> {
    @Option(names = {"-u", "--user"}, description = "User name")
    String user;

    /**
     * interactive 开启交互模式
     * arity = "0..1" 表示即支持直接在选项后面跟值或者交互式
     *                  e.g 1. new CommandLine(new Login()).execute("-u", "jxy", "-p", ”XXXX“, "-cp", "XXXXX");
     *                      2. new CommandLine(new Login()).execute("-u", "jxy", "-p", "-cp");
     */
    @Option(names = {"-p", "--password"}, arity = "0..1", description = "Passphrase", interactive = true)
    String password;

    @Option(names = {"-cp", "--checkPassword"}, arity = "0..1", description = "check password", interactive = true)
    String passwordCheck;

    public Integer call() {
        System.out.println("user : " + user);
        System.out.println("password : " + password);
        System.out.println("passwordCheck : " + passwordCheck);
        return 0;
    }

    public static void main(String[] args) {
        new CommandLine(new Login()).execute("-u", "jxy", "-p", "xxx", "-cp");
//        new CommandLine(new Login()).execute("--help");
//        new CommandLine(new Login()).execute(addArgs(Login.class, new String[]{"-u", "jxy"}));
    }

    /**
     * 给命令添加上所有的交互参数
     * @param clazz 需要添加参数的类
     * @param args 原始参数
     * @return 改变之后的参数
     */
    public static String[] addArgs(Class clazz, String[] args) {
        StringBuilder sb = new StringBuilder(String.join(",", args));
        // 反射获取所有的交互选项
        Field[] fields = clazz.getDeclaredFields();
        for(Field field: fields){
            if(field.isAnnotationPresent(Option.class)) { // 存在选项注解
                Option option = field.getAnnotation(Option.class);
                if (option.interactive()) {
                    if (sb.indexOf(option.names()[0]) <= 0) {
                        sb.append(",").append(option.names()[0]);
                    }
                }
            }
        }
        return sb.toString().split(",");
    }
}