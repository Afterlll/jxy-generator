package com.jxy;

import com.jxy.generator.DynamicGenerator;
import com.jxy.generator.StaticGenerator;
import com.jxy.model.MainTemplateConfig;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 * 动静结合生成器
 */
public class MainGenerator {

    /**
     * 静态 + 动态代码生成器实现
     * @param model 模板数据
     * @throws TemplateException
     * @throws IOException
     */
    public static void doGenerator(Object model) throws TemplateException, IOException {
        // 项目根路径
        String projectPath = System.getProperty("user.dir");
        // 文件模板输入路径
        String inputPath = projectPath + File.separator + "jxy-generator-demo/acm-template";
        // 代码生成器生成代码位置
        String outputPath = projectPath + File.separator + "jxy-generator-basic";
        // 生成静态文件
        StaticGenerator.copyFilesBtRecursive(inputPath, outputPath);
        // 生成动态文件
        inputPath = projectPath + File.separator + "jxy-generator-basic/src/main/resources/templates/MainTemplate.java.ftl";
        outputPath = projectPath + File.separator + "jxy-generator-basic/acm-template/src/com/yupi/acm/MainTemplate.java";
        DynamicGenerator.deGenerator(inputPath, outputPath, model);
    }

    public static void main(String[] args) throws TemplateException, IOException {
        MainTemplateConfig data = new MainTemplateConfig();
        data.setAuthor("江喜原");
        data.setLoop(false);
        data.setOutputText("sum：");
        doGenerator(data);
    }

}
