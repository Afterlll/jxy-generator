package com.jxy.maker.meta;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;

/**
 * 双检加锁 + 单例模式 获取 Meta 对象
 */
public class MetaManage {
    // 保证 Meta 在多线程下的可见性
    private static volatile Meta meta;

    private MetaManage() {
        // 私有构造函数，防止外部实例化
    }

    /**
     * 双检加锁 获取 Meta 对象
     * @return
     */
    public static Meta getMetaObject() {
        if (meta == null) {
            synchronized (MetaManage.class) {
                if (meta == null) {
                    meta = initMeta();
                }
            }
        }
        return meta;
    }

    /**
     * 读取 json 文件中的信息并且转换为 Meta 对象
     * @return Meta 对象
     */
    private static Meta initMeta() {
        String metaJson = ResourceUtil.readUtf8Str("meta.json");
//        String metaJson = ResourceUtil.readUtf8Str("spring-boot-init-meta.json");
        Meta newMeta = JSONUtil.toBean(metaJson, Meta.class);
        // 校验和处理默认值
        MetaValidator.doValidAndFill(newMeta);
        return newMeta;
    }

}
