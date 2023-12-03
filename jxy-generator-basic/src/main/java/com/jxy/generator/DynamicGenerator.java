package com.jxy.generator;

import com.jxy.model.MainTemplateConfig;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 动态代码生成器
 */
public class DynamicGenerator {

    /**
     * 动态文件生成实现
     * @param inputPath 模板文件输入路径
     * @param outputPath 输出路径
     * @param model 数据模型
     */
    public static void deGenerator(String inputPath, String outputPath, Object model) throws IOException, TemplateException {
        // 创建配置对象
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
        configuration.setDirectoryForTemplateLoading(new File(inputPath).getParentFile());
        configuration.setDefaultEncoding("utf-8");

        // 指定模板
        Template template = configuration.getTemplate(new File(inputPath).getName());

        // 生成动态文件
        FileWriter writer = new FileWriter(outputPath);
        template.process(model, writer);

        // 关闭流
        writer.close();
    }

    public static void main(String[] args) throws TemplateException, IOException {
        String projectPath = System.getProperty("user.dir");
        String inputPath = projectPath + File.separator + "jxy-generator-basic/src/main/resources/templates/MainTemplate.java.ftl";
        String outputPath = projectPath + File.separator + "jxy-generator-basic/MainTemplate.java";
        // 创建模型数据
        MainTemplateConfig data = new MainTemplateConfig();
        data.setAuthor("江喜原");
        data.setLoop(false);
        data.setOutputText("sum：");
        deGenerator(inputPath, outputPath, data);
    }

}
