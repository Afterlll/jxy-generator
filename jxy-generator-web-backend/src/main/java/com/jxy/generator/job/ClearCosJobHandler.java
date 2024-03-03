package com.jxy.generator.job;

import cn.hutool.core.util.StrUtil;
import com.jxy.generator.manager.CosManager;
import com.jxy.generator.mapper.GeneratorMapper;
import com.jxy.generator.model.entity.Generator;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 定期清除无用的 COS 资源
 */

@Component
@Slf4j
public class ClearCosJobHandler {

    @Resource
    private CosManager cosManager;

    @Resource
    private GeneratorMapper generatorMapper;

    /**
     * 每天执行
     * @throws Exception
     */
    @XxlJob("clearCosJobHandler")
    public void clearCosJobHandler() throws Exception {
        log.info("clearCosJobHandler start");

        // 编写业务逻辑

        // 1. 清除 模板制作文件（generator_make_template）
        cosManager.deleteDir("/generator_make_template/");
        // 2. 清除已删除的代码生成器对于的产物包文件（generator_dist）
        List<Generator> generatorList = generatorMapper.listDeletedGenerator();
        List<String> keyList = generatorList.stream().map(Generator::getDistPath)
                .filter(StrUtil::isNotBlank)
                // 移除 ‘/’ 前缀，删除目录时特别需要注意的！
                .map(distPath -> distPath.substring(1))
                .collect(Collectors.toList());
        cosManager.deleteObjects(keyList);

        log.info("clearCosJobHandler end");
    }

}
