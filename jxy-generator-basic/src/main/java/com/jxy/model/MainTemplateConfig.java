package com.jxy.model;

import lombok.Data;

@Data
public class MainTemplateConfig {
    /**
     * 作者
     */
    private String author = "江喜原";
    /**
     * 输出信息
     */
    private String outputText = "sum : ";
    /**
     * 是否开启循环
     */
    private boolean loop;
}
