package com.jxy.generator;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 静态代码生成器
 */
public class StaticGenerator {
    /**
     * 使用hutool复制文件夹
     * @param inputPath 要复制的文件路径
     * @param outputPath 复制到哪里去
     */
    public static void copyFileByHutool(String inputPath, String outputPath) {
        FileUtil.copy(inputPath, outputPath, true);
    }

    /**
     * 递归复制文件夹
     * @param inputPath 要复制的文件路径
     * @param outputPath 复制到哪里去
     */
    public static void copyFilesBtRecursive(String inputPath, String outputPath) {
        File inputFile = new File(inputPath);
        File outputFile = new File(outputPath);
        try {
            copyFileBtRecursive(inputFile, outputFile);
        } catch (Exception e) {
            System.out.println("文件复制失败");
            e.printStackTrace();
        }
    }

    private static void copyFileBtRecursive(File inputFile, File outputFile) throws IOException {
        // 判断是文件还是目录
        if (inputFile.isDirectory()) {
            System.out.println(inputFile.getName());
            // 是目录，目标路径需要创建出新目录
            File destOutputFile = new File(outputFile, inputFile.getName());
            if (!destOutputFile.exists()) {
                destOutputFile.mkdirs();
            }
            // 获取该文件夹下的所有子文件
            File[] files = inputFile.listFiles();
            if (ArrayUtil.isEmpty(files)) {
                return;
            }
            for (File file : files) {
                copyFileBtRecursive(file, destOutputFile);
            }
        } else {
            // 是文件，直接复制到目标路径下
            Path destPath = outputFile.toPath().resolve(inputFile.getName());
            Files.copy(inputFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void main(String[] args) {
        String projectPath = System.getProperty("user.dir"); // D:\code\project\generator\jxy-generator
        String inputPath = projectPath + File.separator + "jxy-generator-demo" + File.separator + "acm-template"; // D:\code\project\generator\jxy-generator\jxy-generator-demo\acm-template
        String outputPath = projectPath + File.separator + "jxy-generator-basic";
        copyFileByHutool(inputPath, projectPath);
//        copyFilesBtRecursive(inputPath, projectPath);
    }
}
