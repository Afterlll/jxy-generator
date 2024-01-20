package com.jxy.generator.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求
 *
 * @author 江喜原
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}