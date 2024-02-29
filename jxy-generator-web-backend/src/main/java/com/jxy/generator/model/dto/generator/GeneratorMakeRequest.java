package com.jxy.generator.model.dto.generator;

import com.jxy.maker.meta.Meta;
import lombok.Data;

/**
 * 制作代码生成器请求
 *
 * @author 江喜原
 */
@Data
public class GeneratorMakeRequest {
    /**
     * 元信息
     */
    private Meta meta;

    /**
     * 模板文件压缩包路径
     */
    private String zipFilePath;

    private static final long serialVersionUID = 1L;
}
