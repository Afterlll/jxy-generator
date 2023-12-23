package com.jxy.maker.cli.command;

import cn.hutool.core.io.FileUtil;
import picocli.CommandLine.Command;

import java.io.File;
import java.util.List;

/**
 * 查看要生成的原始文件列表信息
 * 不使用交互式的命令，实现 Runnable 接口
 */
@Command(name = "list", description = "查看文件列表信息", mixinStandardHelpOptions = true)
public class ListCommand implements Runnable {
    @Override
    public void run() {
        // 项目的根路径 D:\code\project\generator\jxy-generator\jxy-generator-basic
        String projectPath = System.getProperty("user.dir");
        String parentPath = new File(projectPath).getParent();
        // 文件模板的位置
        String inputPath = parentPath + File.separator + "jxy-generator-demo" + File.separator + "acm-template";
        // 遍历所有文件
        List<File> files = FileUtil.loopFiles(inputPath);
        for (File file : files) {
            System.out.println(file);
        }
    }
}
