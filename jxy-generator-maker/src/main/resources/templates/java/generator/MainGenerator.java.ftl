package ${basePackage}.generator;

import ${basePackage}.generator.DynamicGenerator;
import ${basePackage}.generator.StaticGenerator;
import ${basePackage}.model.DataModel;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

<#macro generateFile indent fileInfo>
${indent}inputPath = new File(inputRootPath, "${fileInfo.inputPath}").getAbsolutePath();
${indent}outputPath = new File(outputRootPath, "${fileInfo.outputPath}").getAbsolutePath();
<#if fileInfo.generateType == "static">
${indent}StaticGenerator.copyFileByHutool(inputPath, outputPath);
<#else>
${indent}DynamicGenerator.deGenerator(inputPath, outputPath, model);
</#if>
</#macro>

/**
* 核心生成器
**/
public class MainGenerator {

    public static void doGenerate(DataModel model) throws TemplateException, IOException {
        String inputRootPath = "${fileConfig.inputRootPath}";
        String outputRootPath = "${fileConfig.outputRootPath}";

        String inputPath;
        String outputPath;

    <#-- 获取模型变量 -->
    <#list modelConfig.models as modelInfo>
    <#-- 有分组 -->
        <#if modelInfo.groupKey??>
            <#list modelInfo.models as subModelInfo>
        ${subModelInfo.type} ${subModelInfo.fieldName} = model.${modelInfo.groupKey}.${subModelInfo.fieldName};
            </#list>
        <#else>
        ${modelInfo.type} ${modelInfo.fieldName} = model.${modelInfo.fieldName};
        </#if>
    </#list>

    <#list fileConfig.files as fileInfo>
        <#--有分组-->
        <#if fileInfo.groupKey??>
        // groupKey = ${fileInfo.groupKey}
        <#--条件成立-->
        <#if fileInfo.condition??>
        if (${fileInfo.condition}) {
            <#list fileInfo.files as fileInfo>
            <@generateFile fileInfo=fileInfo indent="\t\t\t" />
            </#list>
        }
        <#else >
        <#list fileInfo.files as fileInfo>
        <@generateFile fileInfo=fileInfo indent="\t" />
        </#list>
        </#if>
        <#else >
        <#if fileInfo.condition??>
        if (${fileInfo.condition}) {
            <@generateFile fileInfo=fileInfo indent="\t\t" />
        }
        <#else >
        <@generateFile fileInfo=fileInfo indent="\t\t" />
        </#if>
        </#if>
    </#list>

    }

}
