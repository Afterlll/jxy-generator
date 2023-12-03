import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import com.jxy.model.MainTemplateConfig;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class CopyTest {
    // 生成静态文件
    @Test
    public void generatorStaticFile() {
        String projectPath = System.getProperty("user.dir"); // D:\code\project\generator\jxy-generator\jxy-generator-basic
        String parentPath = new File(projectPath).getParent(); // D:\code\project\generator\jxy-generator
        String inputPath = parentPath + File.separator + "jxy-generator-demo" + File.separator + "acm-template"; // D:\code\project\generator\jxy-generator\jxy-generator-demo\acm-template
//        copyFileByHutool(inputPath, projectPath);
        copyFilesBtRecursive(inputPath, projectPath);
    }
    // 生成动态文件
    @Test
    public void generatorDynamicFile() throws IOException, TemplateException {
        // 创建配置对象
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
        configuration.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));
        configuration.setDefaultEncoding("utf-8");

        // 指定模板
        Template template = configuration.getTemplate("MainTemplate.java.ftl");

        // 创建模型数据
        MainTemplateConfig data = new MainTemplateConfig();
        data.setAuthor("江喜原");
        data.setLoop(false);
        data.setOutputText("计算结果为：");

        // 生成动态文件
        FileWriter writer = new FileWriter("MainTemplate");
        template.process(data, writer);

        // 关闭流
        writer.close();
    }

    // 复制文件夹
    public static void copyFileByHutool(String inputPath, String outputPath) {
        FileUtil.copy(inputPath, outputPath, true);
    }

    // 递归复制文件夹
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
}
