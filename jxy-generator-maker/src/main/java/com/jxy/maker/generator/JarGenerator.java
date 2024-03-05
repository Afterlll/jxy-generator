package com.jxy.maker.generator;

import javax.swing.*;
import java.io.*;

/**
 * jar包构建
 */
public class JarGenerator {

    public static void doGenerate(String projectDir) throws InterruptedException, IOException {
        // 记得改回来
        // Windows maven 命令
        String windowsMavenCommand = "mvn.cmd clean package -DskipTests=true";
        // 其他操作系统的 maven 命令
        String otherMavenCommand = "mvn clean package -DskipTests=true";

        // 使用windows 命令测试
        String mavenCommand = otherMavenCommand;

        // 执行 maven 命令 注意 ： 命令需要使用空格分割开，否则就会被识别为一个字符串
        ProcessBuilder processBuilder = new ProcessBuilder(mavenCommand.split(" "));
        // 设置输出目录
        processBuilder.directory(new File(projectDir));

        Process process = processBuilder.start();

        // 读取命令的输出
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        int exitCode = process.waitFor();
        System.out.println("命令执行结束，退出码：" + exitCode);

    }

}
