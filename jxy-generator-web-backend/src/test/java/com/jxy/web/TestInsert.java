package com.jxy.web;

import com.jxy.generator.MainApplication;
import com.jxy.generator.mapper.GeneratorMapper;
import com.jxy.generator.model.entity.Generator;
import generator.service.GeneratorService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest(classes = MainApplication.class)
public class TestInsert {

    @Resource
    GeneratorMapper generatorMapper;

    @Test
    void testInsert() {
//        Generator generator = generatorService.getById(11);
//        generator.setId(null);
        Generator generator = generatorMapper.selectById(12);
        for (int i = 0; i < 100000; i++) {
            generator.setId(null);
            generatorMapper.insert(generator);
        }
    }
}
