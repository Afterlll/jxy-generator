package com.jxy.maker;

import com.jxy.maker.generator.main.ZipGenerator;
import freemarker.template.TemplateException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws TemplateException, IOException, InterruptedException {
        // 执行 代码生成器 的步骤 （模板方法）
//        new MainGenerator().doGenerate();
        new ZipGenerator().doGenerate();
    }
}
