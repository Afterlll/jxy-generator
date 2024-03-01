package ${basePackage}.generator;

import ${basePackage}.model.DataModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import cn.hutool.core.io.FileUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DynamicGenerator {

    /**
     * 动态文件生成实现
     * @param inputPath 模板文件输入路径
     * @param outputPath 输出路径
     * @param model 数据模型
     */
    public static void deGenerator(String inputPath, String outputPath, Object model) throws IOException, TemplateException {
        // 创建配置对象
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
        configuration.setDirectoryForTemplateLoading(new File(inputPath).getParentFile());
        configuration.setDefaultEncoding("utf-8");

        // 指定模板
        Template template = configuration.getTemplate(new File(inputPath).getName());

        // 文件不存在，创建文件
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
