package com.jxy.maker.generator.file;

import cn.hutool.core.io.FileUtil;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * 动态代码生成器
 */
public class DynamicFileGenerator {
    /**
     * 动态文件生成实现
     * @param relativeInputPath 模板文件输入路径
     * @param outputPath 输出路径
     * @param model 数据模型
     */
    public static void deGenerator(String relativeInputPath, String outputPath, Object model) throws IOException, TemplateException {
        // 创建配置对象
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);

        // 获取模板文件所属包和模板名称
        int lastSplitIndex = relativeInputPath.lastIndexOf("/");
        String basePackagePath = relativeInputPath.substring(0, lastSplitIndex);
        String templateName = relativeInputPath.substring(lastSplitIndex + 1);

        // 通过类加载器读取模板
        ClassTemplateLoader templateLoader = new ClassTemplateLoader(DynamicFileGenerator.class, basePackagePath);
        configuration.setTemplateLoader(templateLoader);

        // 设置模板文件使用的字符集
        configuration.setDefaultEncoding("utf-8");

        // 创建模板对象，加载指定模板
        Template template = configuration.getTemplate(templateName);

        // 文件不存在则创建文件和父目录
        if (!FileUtil.exist(outputPath)) {
            FileUtil.touch(outputPath);
        }

        // 生成
        Writer out = new FileWriter(outputPath);
        template.process(model, out);

        // 生成文件后别忘了关闭哦
        out.close();
    }

    /**
     * 动态文件生成实现
     * @param inputPath 模板文件输入路径
     * @param outputPath 输出路径
     * @param model 数据模型
     */
    public static void doGenerateByPath(String inputPath, String outputPath, Object model) throws IOException, TemplateException {
        // 创建配置对象
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
        configuration.setDirectoryForTemplateLoading(new File(inputPath).getParentFile());
        configuration.setDefaultEncoding("utf-8");

        // 指定模板
        Template template = configuration.getTemplate(new File(inputPath).getName());

        if (!FileUtil.exist(outputPath)) {
            FileUtil.touch(outputPath);
        }

        // 生成动态文件
        FileWriter writer = new FileWriter(outputPath);
        template.process(model, writer);

        // 关闭流
        writer.close();
    }
}
