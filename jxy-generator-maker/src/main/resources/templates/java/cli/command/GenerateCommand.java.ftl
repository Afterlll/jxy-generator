package ${basePackage}.cli.command;

import cn.hutool.core.bean.BeanUtil;
import ${basePackage}.generator.MainGenerator;
import ${basePackage}.model.DataModel;
import lombok.Data;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "generate", description = "生成文件", mixinStandardHelpOptions = true)
@Data
public class GenerateCommand implements Callable<Integer> {
<#list modelConfig.models as modelInfo>
    @Option(names = {<#if modelInfo.abbr??>"-${modelInfo.abbr}",</#if> "--${modelInfo.fieldName}"}, arity = "0..1", echo = true, <#if modelInfo.description??>description = "${modelInfo.description}",</#if> interactive = true)
    private ${modelInfo.type} ${modelInfo.fieldName} <#if modelInfo.defaultValue??>= <#if modelInfo.type == "String">"</#if>${modelInfo.defaultValue}<#if modelInfo.type == "String">"</#if></#if>;

</#list>

    @Override
    public Integer call() throws Exception {
        // 模板数据
        DataModel model = new DataModel();
        BeanUtil.copyProperties(this, model);
        // 调用实现好的 静态 + 动态 代码生成器
        MainGenerator.doGenerator(model);
        return 0;
    }
}