package com.jxy.generator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jxy.generator.model.entity.Generator;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author 13547
 * @description 针对表【generator(代码生成器)】的数据库操作Mapper
 * @createDate 2024-01-20 20:13:07
 * @Entity generator.domain.Generator
 */
public interface GeneratorMapper extends BaseMapper<Generator> {
    /**
     *返回所有已删除的生成器
     * @return
     */
    @Select("SELECT id, distPath FROM generator where isDelete = 1;")
    List<Generator> listDeletedGenerator();
}




