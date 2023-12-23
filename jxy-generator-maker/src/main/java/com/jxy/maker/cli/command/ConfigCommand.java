package com.jxy.maker.cli.command;

import cn.hutool.core.util.ReflectUtil;
import com.jxy.maker.model.DataModel;
import picocli.CommandLine.Command;

import java.lang.reflect.Field;

/**
 * 查看允许用户传入的动态参数信息
 * 不使用交互式的命令，实现 Runnable 接口
 */
@Command(name = "config", description = "查看动态参数信息", mixinStandardHelpOptions = true)
public class ConfigCommand implements Runnable {
    @Override
    public void run() {
        // 通过反射获取 DataModel 类的属性和类型
        Field[] fields = ReflectUtil.getFields(DataModel.class);
        for (Field field : fields) {
            System.out.println("字段名称：" + field.getName());
            System.out.println("字段类型：" + field.getType());
            System.out.println("---");
        }
    }
}
