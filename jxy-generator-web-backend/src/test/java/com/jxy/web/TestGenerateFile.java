package com.jxy.web;

import com.jxy.generator.common.ErrorCode;
import com.jxy.generator.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class TestGenerateFile {
    @Test
    void testScriptGenerateFile() {
        // D:\code\project\jxy-generator1\generated\acm-template-pro-generator-dist
        // 将用户输入的参数写到 json 文件中
        String dataModelFilePath = "D:/code/project/jxy-generator/.temp/use/11/dataModel.json";

        File scriptFile = new File("D:/code/project/jxy-generator1/generated/acm-template-pro-generator-dist/generator.bat");

        // 添加可执行权限(Linux、Mac)
        try {
            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxrwxrwx");
            Files.setPosixFilePermissions(scriptFile.toPath(), permissions);
        } catch (Exception e) {

        }

        // 构造命令
        File scriptDir = scriptFile.getParentFile();
        // 注意，如果是 mac / linux 系统，要用 "./generator"
        String scriptAbsolutePath = scriptFile.getAbsolutePath().replace("/", "/");
        String[] commands = new String[] {scriptAbsolutePath, "json-generate", "--file=" + dataModelFilePath};

        // 这里一定要拆分！
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(scriptDir);

        try {
            Process process = processBuilder.start();

            // 读取命令的输出
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 等待命令执行完成
            int exitCode = process.waitFor();
            System.out.println("命令执行结束，退出码：" + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "执行生成器脚本错误");
        }
    }

}
