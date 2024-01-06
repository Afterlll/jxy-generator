package com.jxy.maker.meta;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class Meta {

    public String name;
    public String description;
    public String basePackage;
    public String version;
    public String author;
    public String createTime;
    public FileConfig fileConfig;
    public ModelConfig modelConfig;

    @NoArgsConstructor
    @Data
    public static class FileConfig {
        public String inputRootPath;
        public String outputRootPath;
        public String sourceRootPath;
        public String gitRootPath;
        public String type;
        public List<FileInfo> files;

        @NoArgsConstructor
        @Data
        public static class FileInfo {
            public String inputPath;
            public String outputPath;
            public String type;
            public String generateType;
            public String condition;
            public String groupKey;
            public String groupName;
            public List<FileInfo> files;
        }
    }

    @NoArgsConstructor
    @Data
    public static class ModelConfig {
        public List<ModelInfo> models;

        @NoArgsConstructor
        @Data
        public static class ModelInfo {
            public String fieldName;
            public String type;
            public String description;
            public Object defaultValue;
            public String abbr;
            public String groupKey;
            public String groupName;
            public List<ModelInfo> models;
            public String condition;

            // 中间参数
            // 该分组下所有参数拼接字符串
            public String allArgsStr;
        }
    }

}