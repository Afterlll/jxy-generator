package com.jxy.maker.template;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.jxy.maker.template.enums.FileFilterRangeEnum;
import com.jxy.maker.template.enums.FileFilterRuleEnum;
import com.jxy.maker.template.model.FileFilterConfig;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文件过滤
 */
public class FIleFilter {

    /**
     * 对某个文件或者目录进行过滤，访问文件列表
     *
     * @param filePath 文件路径（可以是目录）
     * @param filterConfigList 文件过滤规则
     * @return 返回满足过滤规则的所有文件
     */
    public static List<File> doFilter(String filePath, List<FileFilterConfig> filterConfigList) {
        // 根据路径获取所有文件
        List<File> files = FileUtil.loopFiles(filePath);
        return files.stream()
                .filter(file -> doSingleFileFiler(filterConfigList, file))
                .collect(Collectors.toList());
    }

    /**
     * 单个文件过滤
     *
     * @param filterConfigList 过滤规则
     * @param file 进行过滤的单个文件
     * @return 是否保留该文件
     */
    public static boolean doSingleFileFiler(List<FileFilterConfig> filterConfigList, File file) {
        String fileName = file.getName();
        String fileContent = FileUtil.readUtf8String(file);

        // 所有过滤器校验之后的结果
        boolean result = true;

        // 没有过滤规则
        if (CollUtil.isEmpty(filterConfigList)) {
            return true;
        }

        for (FileFilterConfig fileFilterConfig : filterConfigList) {
            String range = fileFilterConfig.getRange();
            String rule = fileFilterConfig.getRule();
            String value = fileFilterConfig.getValue();

            // 没有过滤范围，跳过此次循环
            FileFilterRangeEnum fileFilterRangeEnum = FileFilterRangeEnum.getEnumByValue(range);
            if (ObjectUtil.isEmpty(fileFilterRangeEnum)) {
                continue;
            }

            // 统一过滤的内容
            String content = fileName;
            switch (Objects.requireNonNull(fileFilterRangeEnum)) {
                case FILE_NAME:
                    content = fileName;
                    break;
                case FILE_CONTENT:
                    content = fileContent;
                    break;
                default:
            }

            // 没有过滤规则，跳过此次循环
            FileFilterRuleEnum fileFilterRuleEnum = FileFilterRuleEnum.getEnumByValue(rule);
            if (ObjectUtil.isEmpty(fileFilterRuleEnum)) {
                continue;
            }

            switch (Objects.requireNonNull(fileFilterRuleEnum)) {
                case CONTAINS:
                    result = content.contains(value);
                    break;
                case STARTS_WITH:
                    result = content.startsWith(value);
                    break;
                case ENDS_WITH:
                    result = content.endsWith(value);
                    break;
                case REGEX:
                    result = content.matches(value);
                    break;
                case EQUALS:
                    result = content.equals(value);
                    break;
                default:
            }

            // 有一个不满足，直接返回
            if (!result) {
                return false;
            }
        }

        // 全都满足
        return true;
    }


}
