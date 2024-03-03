package com.jxy.generator.manager;

import cn.hutool.core.util.StrUtil;
import com.jxy.generator.mapper.GeneratorMapper;
import com.jxy.generator.model.entity.Generator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 注意配置 --spring.profiles.active=local
 * 否则找不到 COS
 */
@SpringBootTest
class CosManagerTest {

    @Resource
    private CosManager cosManager;

    @Resource
    private GeneratorMapper generatorMapper;

    @Test
    void deleteObject() {
        cosManager.deleteObject("/test/test1.jpg");
    }

    @Test
    void deleteObjects() {
        cosManager.deleteObjects(Arrays.asList("test/test2.jpg", "test/test3.jpg"));
    }

    @Test
    void deleteDir() {
        cosManager.deleteDir("/test/");
    }

    // 测试空指针时的影响
    @Test
    void test() {
        List<Generator> generatorList = generatorMapper.listDeletedGenerator();
        List<String> collect = generatorList.stream().map(Generator::getDistPath)
                .filter(StrUtil::isNotBlank)
                // 移除 ‘/’ 前缀，删除目录时特别需要注意的！
                .map(distPath -> distPath.substring(1))
                .collect(Collectors.toList());
        System.out.println(collect);
    }
}