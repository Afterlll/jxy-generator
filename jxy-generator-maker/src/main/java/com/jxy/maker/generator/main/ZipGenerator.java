package com.jxy.maker.generator.main;

import com.jxy.maker.meta.Meta;

import java.io.IOException;

/**
 * 构建 zip 包
 */
public class ZipGenerator extends GenerateTemplate {
    @Override
    protected String buildDist(String outputPath, String sourceCopyDestPath, String jarPath, String shellOutputFilePath) {
        String path = super.buildDist(outputPath, sourceCopyDestPath, jarPath, shellOutputFilePath);
        return super.buildZip(path);
    }

    @Override
    protected void addGit(Meta meta, String outputPath, String distOutputPath) throws IOException {

    }
}
