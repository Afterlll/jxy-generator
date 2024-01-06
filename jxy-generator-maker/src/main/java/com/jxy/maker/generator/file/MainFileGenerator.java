package com.jxy.maker.generator.file;

import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 * 动静结合生成器
 */
public class MainFileGenerator {
    /**
     * 静态 + 动态代码生成器实现
     * @param model 模板数据
     * @throws TemplateException
     * @throws IOException
     */
    public static void doGenerator(Object model) throws TemplateException, IOException {
        // 项目根路径
        String projectPath = System.getProperty("user.dir");
        String parentPath = new File(projectPath).getParent();
        // 文件模板输入路径
        String inputPath = parentPath + File.separator + "jxy-generator-demo" +  File.separator + "acm-template";
        // 代码生成器生成代码位置
        String outputPath = projectPath;

        // 生成静态文件
        StaticFileGenerator.copyFileByHutool(inputPath, outputPath);
        // 生成动态文件
        inputPath = projectPath + File.separator + "src/main/resources/templates/MainTemplate.java.ftl";
        outputPath = projectPath + File.separator + "acm-template/src/com/jxy/acm/MainTemplate.java";
        DynamicFileGenerator.deGenerator(inputPath, outputPath, model);
    }

    public static void main(String[] args) {
        // 项目根路径
        String projectPath = System.getProperty("user.dir");
        String parentPath = new File(projectPath).getParent();
        // 文件模板输入路径
        String inputPath = parentPath + File.separator + "jxy-generator-demo" +  File.separator + "acm-template";
        // 代码生成器生成代码位置
        String outputPath = projectPath;
        System.out.println(inputPath);
        System.out.println(outputPath);
    }
}
