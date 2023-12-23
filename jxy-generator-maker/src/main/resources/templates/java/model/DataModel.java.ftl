package ${basePackage}.model;

import lombok.Data;

/**
* 数据模型
*/
@Data
public class DataModel {
<#list modelConfig.models as ModelInfo>

    <#if ModelInfo.description??>
    /**
    * ${ModelInfo.description}
    */
    </#if>
    private ${ModelInfo.type} ${ModelInfo.fieldName} <#if ModelInfo.defaultValue??>= <#if ModelInfo.type == "String">"</#if>${ModelInfo.defaultValue}<#if ModelInfo.type == "String">"</#if></#if>;
</#list>
}