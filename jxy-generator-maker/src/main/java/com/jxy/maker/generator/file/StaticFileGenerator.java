package com.jxy.maker.generator.file;

import cn.hutool.core.io.FileUtil;

/**
 * 静态代码生成器
 */
public class StaticFileGenerator {
    /**
     * 使用hutool复制文件夹
     * @param inputPath 要复制的文件路径
     * @param outputPath 复制到哪里去
     */
    public static void copyFileByHutool(String inputPath, String outputPath) {
        FileUtil.copy(inputPath, outputPath, true);
    }
}
