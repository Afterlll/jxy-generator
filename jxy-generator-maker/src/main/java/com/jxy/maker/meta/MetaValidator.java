package com.jxy.maker.meta;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.jxy.maker.meta.enums.FileGenerateTypeEnum;
import com.jxy.maker.meta.enums.FileTypeEnum;
import com.jxy.maker.meta.enums.ModelTypeEnum;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 元信息校验类
 */
public class MetaValidator {
    public static void doValidAndFill(Meta meta) {
        // 基础信息校验
        validAndFillMetaRoot(meta);
        // fileConfig 信息校验
        validAndFillFileConfig(meta);
        // modelConfig 信息校验
        validAndFillModelConfig(meta);
    }

    private static void validAndFillModelConfig(Meta meta) {
        Meta.ModelConfig modelConfig = meta.getModelConfig();
        if (modelConfig == null) {
            return;
        }
        List<Meta.ModelConfig.ModelInfo> models = modelConfig.getModels();
        if (CollectionUtil.isEmpty(models)) {
            return;
        }
        for (Meta.ModelConfig.ModelInfo model : models) {
            // 为 group 时 不校验
            if (StrUtil.isNotEmpty(model.getGroupKey())) {
                // 生成中间参数
                String allArgsStr = model.getModels().stream()
                        .map(subModelInfo -> String.format("\"--%s\"", subModelInfo.getFieldName()))
                        .collect(Collectors.joining(", "));
                model.setAllArgsStr(allArgsStr);
                continue;
            }
            String fieldName = model.getFieldName();
            if (StrUtil.isBlank(fieldName)) {
                throw new MetaException("未填写 fieldName");
            }
            String type = model.getType();
            type = StrUtil.emptyToDefault(type, ModelTypeEnum.STRING.getValue());
            model.setType(type);
        }
    }

    private static void validAndFillFileConfig(Meta meta) {
        Meta.FileConfig fileConfig = meta.getFileConfig();
        if (fileConfig == null) {
            return;
        }
        // sourceRootPath 必填，没填就抛出异常
        String sourceRootPath = fileConfig.getSourceRootPath();
        if (StrUtil.isBlank(sourceRootPath)) {
            throw new MetaException("未填写 sourceRootPath");
        }
        String inputRootPath = fileConfig.getInputRootPath();
        // .source/sourceRootPath 最后一个层级目录
        inputRootPath = StrUtil.emptyToDefault(inputRootPath, FileUtil.getLastPathEle(Paths.get(sourceRootPath)).getFileName().toString());
        String outputRootPath = fileConfig.getOutputRootPath();
        outputRootPath = StrUtil.emptyToDefault(outputRootPath, "generated");
        String fileConfigType = fileConfig.getType();
        fileConfigType = StrUtil.emptyToDefault(fileConfigType, FileTypeEnum.DIR.getValue());

        fileConfig.setInputRootPath(inputRootPath);
        fileConfig.setOutputRootPath(outputRootPath);
        fileConfig.setType(fileConfigType);

        // fileInfo 默认值
        List<Meta.FileConfig.FileInfo> files = fileConfig.getFiles();
        if (CollectionUtil.isEmpty(files)) {
            return;
        }
        for (Meta.FileConfig.FileInfo file : files) {
            // 类型为 group 不需要校验
            if (FileTypeEnum.GROUP.getValue().equals(file.getType())) {
                continue;
            }
            // inputPath 必填
            String inputPath = file.getInputPath();
            if (StrUtil.isBlank(inputPath)) {
                throw new MetaException("未填写 inputPath");
            }
            String outputPath = file.getOutputPath();
            outputPath = StrUtil.emptyToDefault(outputPath, inputPath);
            file.setOutputPath(outputPath);
            // 文件有后缀 -> file 无后缀 -> dir
            String type = file.getType();
            if (StrUtil.isBlank(FileUtil.getSuffix(inputPath))) {
                file.setType(FileTypeEnum.DIR.getValue());
            } else {
                file.setType(FileTypeEnum.FILE.getValue());
            }
            // generateType 值的确定：当文件后缀为 .ftl 时，generateType 为 dynamic，否则为 static
            String generateType = file.getGenerateType();
            if (StrUtil.isBlank(generateType)) {
                if (inputPath.endsWith(".ftl")) {
                    file.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
                } else {
                    file.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
                }
            }
        }
    }

    private static void validAndFillMetaRoot(Meta meta) {
        String name = meta.getName();
        name = StrUtil.blankToDefault(name, "my-generator");
        String description = meta.getDescription();
        description = StrUtil.emptyToDefault(description, "我的代码生成器模板");
        String basePackage = meta.getBasePackage();
        basePackage = StrUtil.emptyToDefault(basePackage, "com.jxy");
        String version = meta.getVersion();
        version = StrUtil.emptyToDefault(version, "1.0");
        String author = meta.getAuthor();
        author = StrUtil.emptyToDefault(author, "jxy");
        String createTime = meta.getCreateTime();
        createTime = StrUtil.emptyToDefault(createTime, DateUtil.now());

        meta.setName(name);
        meta.setDescription(description);
        meta.setBasePackage(basePackage);
        meta.setVersion(version);
        meta.setAuthor(author);
        meta.setCreateTime(createTime);
    }
}