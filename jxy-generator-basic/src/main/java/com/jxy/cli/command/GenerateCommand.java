package com.jxy.cli.command;

import cn.hutool.core.bean.BeanUtil;
import com.jxy.generator.MainGenerator;
import com.jxy.model.MainTemplateConfig;
import lombok.Data;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

/**
 * 生成文件子命令
 * 对于需要交互式的命令，实现 Callable 接口
 */
@Command(name = "generate", description = "生成文件", mixinStandardHelpOptions = true)
@Data
public class GenerateCommand implements Callable<Integer> {

    @Option(names = {"-l", "--loop"}, arity = "0..1", echo = true, description = "是否循环", interactive = true)
    private boolean loop;

    @Option(names = {"-a", "--author"}, arity = "0..1", echo = true, description = "作者", interactive = true)
    private String author;

    @Option(names = {"-o", "--outputText"}, arity = "0..1", echo = true, description = "输出文本", interactive = true)
    private String outputText;

    @Override
    public Integer call() throws Exception {
        // 模板数据
        MainTemplateConfig model = new MainTemplateConfig();
        BeanUtil.copyProperties(this, model);
        // 调用实现好的 静态 + 动态 代码生成器
        MainGenerator.doGenerator(model);
        return 0;
    }
}
