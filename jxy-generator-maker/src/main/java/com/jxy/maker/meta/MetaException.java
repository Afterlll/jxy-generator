package com.jxy.maker.meta;

/**
 * 元信息异常处理类
 */
public class MetaException extends RuntimeException{
    public MetaException(String message) {
        super(message);
    }

    public MetaException(String message, Throwable cause) {
        super(message, cause);
    }
}
