package com.jxy.maker.generator;

import cn.hutool.core.io.FileUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

/**
 * 脚本生成器
 */
public class ScriptGenerator {

    public static void doGenerate(String outputPath, String jarPath) {
        // linux 脚本
        StringBuilder sb = new StringBuilder();
        sb.append("#!/bin/bash").append("\n");
        sb.append(String.format("java -jar %s \"$@\"", jarPath)).append("\n");
        FileUtil.writeBytes(sb.toString().getBytes(StandardCharsets.UTF_8), outputPath);
        // 添加可执行权限
//        try {
//            Files.setPosixFilePermissions(Paths.get(outputPath), PosixFilePermissions.fromString("rwxrwxrwx"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        // windows 脚本
        sb = new StringBuilder();
        sb.append("@echo off").append("\n");
        sb.append(String.format("java -jar %s %%*", jarPath)).append("\n");
        FileUtil.writeBytes(sb.toString().getBytes(StandardCharsets.UTF_8), outputPath + ".bat");
    }

}
