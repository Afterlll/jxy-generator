package com.jxy.maker.generator;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * 支持git托管
 */
public class GitGenerator {

    // TODO git功能暂时只实现了 `git init` 可能还可以实现一体化（用户输入github的账号密码，创建的仓库名称），系统直接生成一个git仓库（注意命令的原子性）
    public static void doGenerator(String inputPath, String outputPath) throws IOException {
        // 1. 使用 process 执行命令 git init
        String gitCommand = "git init";
        // - 注意设置git命令的执行目录（在这里也就是 .gitignore 文件的输出目录）
        new ProcessBuilder(gitCommand.split(" ")).directory(new File(outputPath)).start();
        // 2. 复制 .gitignore 到当前项目根路径下
        FileUtil.copy(inputPath, outputPath, true);
    }

}
