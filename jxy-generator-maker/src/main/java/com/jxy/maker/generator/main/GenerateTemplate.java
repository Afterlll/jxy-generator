package com.jxy.maker.generator.main;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import com.jxy.maker.generator.GitGenerator;
import com.jxy.maker.generator.JarGenerator;
import com.jxy.maker.generator.ScriptGenerator;
import com.jxy.maker.generator.file.DynamicFileGenerator;
import com.jxy.maker.meta.Meta;
import com.jxy.maker.meta.MetaManage;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 * 模板方法 父类
 */
public abstract class GenerateTemplate {

    public void doGenerate() throws TemplateException, IOException, InterruptedException {
        Meta meta = MetaManage.getMetaObject();

        // 0. 输出根路径
        // 项目的根路径
        String projectPath = System.getProperty("user.dir");
        // 指定模型数据文件的输出目录
        String outputPath = projectPath + File.separator + "generated" + File.separator + meta.getName();
        // 创建输出目录
        if (!FileUtil.exist(outputPath)) {
            FileUtil.mkdir(outputPath);
        }

        // 1. 复制原始文件
        String sourceCopyDestPath = copySource(meta, outputPath);
        // 2. 代码生成
        generateCode(meta, outputPath);
        // 3， 构建 jar 包
        String jarPath = buildJar(meta, outputPath);
        // 4. 封装脚本
        String shellOutputFilePath = buildScript(outputPath, jarPath);
        // 5. 生成精简版的程序（产物包）
        String distOutputPath = buildDist(outputPath, sourceCopyDestPath, jarPath, shellOutputFilePath);
        // 6. 增加 git 托管
        addGit(meta, outputPath, distOutputPath);
    }

    protected void addGit(Meta meta, String outputPath, String distOutputPath) throws IOException {
        // 支持git托管
        Meta.FileConfig fileConfig = meta.getFileConfig();
        if (fileConfig == null) {
            return;
        }
        String gitRootPath = fileConfig.getGitRootPath();
        if (StrUtil.isBlank(gitRootPath)) {
            return;
        }
        GitGenerator.doGenerator(gitRootPath, distOutputPath);
        GitGenerator.doGenerator(gitRootPath, outputPath);
    }

    /**
     * 生成精简版程序
     * @param outputPath
     * @param sourceCopyDestPath
     * @param jarPath
     * @param shellOutputFilePath
     * @return
     */
    protected String buildDist(String outputPath, String sourceCopyDestPath, String jarPath, String shellOutputFilePath) {
        // 生成精简版的程序（产物包）
        // - 由于所有的代码都是需要生成的，精简版只是将运行代码所必要的抽取出来，因此直接进行复制即可
        String distOutputPath = outputPath + "-dist";
        // - 拷贝jar包
        String targetAbsolutePath = distOutputPath + File.separator + "target";
        FileUtil.mkdir(targetAbsolutePath);
        String jatAbsolutePath = outputPath + File.separator + jarPath;
        FileUtil.copy(jatAbsolutePath, targetAbsolutePath, true);
        // - 拷贝脚本文件
        FileUtil.copy(shellOutputFilePath, distOutputPath, true);
        FileUtil.copy(shellOutputFilePath + ".bat", distOutputPath, true);
        // - 拷贝源代码文件
        FileUtil.copy(sourceCopyDestPath, distOutputPath, true);
        return distOutputPath;
    }

    /**
     * 封装脚本
     *
     * @param outputPath
     * @param jarPath
     * @return
     * @throws IOException
     */
    protected String buildScript(String outputPath, String jarPath) {
        // 生成脚本
        String shellOutputFilePath = outputPath + File.separator + "generator";
        ScriptGenerator.doGenerate(shellOutputFilePath, jarPath);
        return shellOutputFilePath;
    }

    /**
     * 构建jar包
     * @param meta
     * @param outputPath
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    protected String buildJar(Meta meta, String outputPath) throws InterruptedException, IOException {
        // 构建jar包
        JarGenerator.doGenerate(outputPath);
        String jarName = String.format("%s-%s-jar-with-dependencies.jar", meta.getName(), meta.getVersion());
        return "target/" + jarName;
    }

    /**
     * 代码生成
     * @param meta
     * @param outputPath
     * @throws IOException
     * @throws TemplateException
     */
    protected void generateCode(Meta meta, String outputPath) throws IOException, TemplateException {
        // resource 目录的 路径
        String inputResourcePath = new ClassPathResource("").getAbsolutePath();
        // 生成java文件的目录
        String outputBaseJavaPackagePath = outputPath + File.separator + "src/main/java/" + StrUtil.join("/", StrUtil.split(meta.getBasePackage(), "."));

        String inputFilePath;
        String outputFilePath;

        // 模板文件的位置
        inputFilePath = inputResourcePath + File.separator + "templates/java/model/DataModel.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/model/DataModel.java";
        DynamicFileGenerator.deGenerator(inputFilePath, outputFilePath, meta);

        // generator.MainFileGenerator
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/MainGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/MainGenerator.java";
        DynamicFileGenerator.deGenerator(inputFilePath, outputFilePath, meta);

        // cli.command.ConfigCommand
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/ConfigCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/command/ConfigCommand.java";
        DynamicFileGenerator.deGenerator(inputFilePath, outputFilePath, meta);

        // cli.command.GenerateCommand
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/GenerateCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/command/GenerateCommand.java";
        DynamicFileGenerator.deGenerator(inputFilePath, outputFilePath, meta);

        // cli.command.ListCommand
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/ListCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/command/ListCommand.java";
        DynamicFileGenerator.deGenerator(inputFilePath, outputFilePath, meta);

        // cli.CommandExecutor
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/CommandExecutor.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/CommandExecutor.java";
        DynamicFileGenerator.deGenerator(inputFilePath, outputFilePath, meta);

        // Main
        inputFilePath = inputResourcePath + File.separator + "templates/java/Main.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/Main.java";
        DynamicFileGenerator.deGenerator(inputFilePath, outputFilePath, meta);

        // generator.DynamicFileGenerator
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/DynamicGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/DynamicGenerator.java";
        DynamicFileGenerator.deGenerator(inputFilePath, outputFilePath, meta);

        // generator.StaticFileGenerator
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/StaticGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/StaticGenerator.java";
        DynamicFileGenerator.deGenerator(inputFilePath, outputFilePath, meta);

        // pom.xml
        inputFilePath = inputResourcePath + File.separator + "templates/pom.xml.ftl";
        outputFilePath = outputPath + File.separator + "pom.xml";
        DynamicFileGenerator.deGenerator(inputFilePath, outputFilePath, meta);

        // README.md
        inputFilePath = inputResourcePath + File.separator + "templates/README.md.ftl";
        outputFilePath = outputPath + File.separator + "README.md";
        DynamicFileGenerator.deGenerator(inputFilePath, outputFilePath, meta);
    }

    /**
     * 复制原始文件
     * @param meta
     * @param outputPath
     * @return
     */
    protected String copySource(Meta meta, String outputPath) {
        // 根据sourceRootPath在InputRootPath生成代码原始模板
        String sourceRootPath = meta.getFileConfig().getSourceRootPath();
        String sourceCopyDestPath = outputPath + File.separator + ".source";
        FileUtil.copy(sourceRootPath, sourceCopyDestPath, false);
        return sourceCopyDestPath;
    }

}
