package com.jxy.generator.controller;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jxy.generator.annotation.AuthCheck;
import com.jxy.generator.common.BaseResponse;
import com.jxy.generator.common.DeleteRequest;
import com.jxy.generator.common.ErrorCode;
import com.jxy.generator.common.ResultUtils;
import com.jxy.generator.constant.UserConstant;
import com.jxy.generator.exception.BusinessException;
import com.jxy.generator.exception.ThrowUtils;
import com.jxy.generator.manager.CacheManager;
import com.jxy.generator.manager.CosManager;
import com.jxy.maker.generator.main.GenerateTemplate;
import com.jxy.maker.generator.main.ZipGenerator;
import com.jxy.maker.meta.Meta;
import com.jxy.generator.model.dto.generator.*;
import com.jxy.generator.model.entity.Generator;
import com.jxy.generator.model.entity.User;
import com.jxy.generator.model.vo.GeneratorVO;
import com.jxy.generator.service.GeneratorService;
import com.jxy.generator.service.UserService;
import com.jxy.maker.meta.MetaValidator;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 代码生成器接口
 *
 * @author 江喜原
 */
@RestController
@RequestMapping("/generator")
@Slf4j
public class GeneratorController {

    @Resource
    private GeneratorService generatorService;

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Resource
    private CacheManager cacheManager;

    // region 增删改查

    /**
     * 创建
     *
     * @param generatorAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addGenerator(@RequestBody GeneratorAddRequest generatorAddRequest, HttpServletRequest request) {
        if (generatorAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = new Generator();
        BeanUtils.copyProperties(generatorAddRequest, generator);
        List<String> tags = generatorAddRequest.getTags();
        generator.setTags(JSONUtil.toJsonStr(tags));
        Meta.FileConfig fileConfig = generatorAddRequest.getFileConfig();
        generator.setFileConfig(JSONUtil.toJsonStr(fileConfig));
        Meta.ModelConfig modelConfig = generatorAddRequest.getModelConfig();
        generator.setModelConfig(JSONUtil.toJsonStr(modelConfig));

        generatorService.validGenerator(generator, true);
        User loginUser = userService.getLoginUser(request);
        generator.setUserId(loginUser.getId());
        generator.setStatus(0);
        boolean result = generatorService.save(generator);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newGeneratorId = generator.getId();
        return ResultUtils.success(newGeneratorId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteGenerator(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Generator oldGenerator = generatorService.getById(id);
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldGenerator.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = generatorService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param generatorUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateGenerator(@RequestBody GeneratorUpdateRequest generatorUpdateRequest) {
        if (generatorUpdateRequest == null || generatorUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = new Generator();
        BeanUtils.copyProperties(generatorUpdateRequest, generator);
        List<String> tags = generatorUpdateRequest.getTags();
        generator.setTags(JSONUtil.toJsonStr(tags));
        Meta.FileConfig fileConfig = generatorUpdateRequest.getFileConfig();
        generator.setFileConfig(JSONUtil.toJsonStr(fileConfig));
        Meta.ModelConfig modelConfig = generatorUpdateRequest.getModelConfig();
        generator.setModelConfig(JSONUtil.toJsonStr(modelConfig));
        // 参数校验
        generatorService.validGenerator(generator, false);
        long id = generatorUpdateRequest.getId();
        // 判断是否存在
        Generator oldGenerator = generatorService.getById(id);
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = generatorService.updateById(generator);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<GeneratorVO> getGeneratorVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(generatorService.getGeneratorVO(generator, request));
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param generatorQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Generator>> listGeneratorByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest) {
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        return ResultUtils.success(generatorPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param generatorQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<GeneratorVO>> listGeneratorVOByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest,
                                                                 HttpServletRequest request) {
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        return ResultUtils.success(generatorService.getGeneratorVOPage(generatorPage, request));
    }

    /**
     * 快速分页获取列表（封装类）
     *
     * @param generatorQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo/fast")
    public BaseResponse<Page<GeneratorVO>> listGeneratorVOByPageFast(@RequestBody GeneratorQueryRequest generatorQueryRequest,
                                                                 HttpServletRequest request) {
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        
        // 优先从缓存中获取（多级缓存）
        String cacheKey = getPageCacheKey(generatorQueryRequest);
        Object cacheValue = cacheManager.get(cacheKey);
        if (cacheValue != null) {
            // 缓存命中
            return ResultUtils.success((Page<GeneratorVO>) cacheValue);
        }

        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        // 优化 SQL
        QueryWrapper<Generator> queryWrapper = generatorService.getQueryWrapper(generatorQueryRequest);
        queryWrapper.select("id","name","description","basePackage","version","author","tags","picture","status","userId","createTime","updateTime");
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                queryWrapper);
        // 精简字段
        generatorPage.getRecords().forEach(generator -> {
            generator.setFileConfig(null);
            generator.setModelConfig(null);
        });

        // 写入多级缓存
        cacheManager.put(cacheKey, generatorPage);

        return ResultUtils.success(generatorService.getGeneratorVOPage(generatorPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param generatorQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<GeneratorVO>> listMyGeneratorVOByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest,
                                                                   HttpServletRequest request) {
        if (generatorQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        generatorQueryRequest.setUserId(loginUser.getId());
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        return ResultUtils.success(generatorService.getGeneratorVOPage(generatorPage, request));
    }


    /**
     * 编辑（用户）
     *
     * @param generatorEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editGenerator(@RequestBody GeneratorEditRequest generatorEditRequest, HttpServletRequest request) {
        if (generatorEditRequest == null || generatorEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = new Generator();
        BeanUtils.copyProperties(generatorEditRequest, generator);
        List<String> tags = generatorEditRequest.getTags();
        generator.setTags(JSONUtil.toJsonStr(tags));
        Meta.FileConfig fileConfig = generatorEditRequest.getFileConfig();
        generator.setFileConfig(JSONUtil.toJsonStr(fileConfig));
        Meta.ModelConfig modelConfig = generatorEditRequest.getModelConfig();
        generator.setModelConfig(JSONUtil.toJsonStr(modelConfig));

        // 参数校验
        generatorService.validGenerator(generator, false);
        User loginUser = userService.getLoginUser(request);
        long id = generatorEditRequest.getId();
        // 判断是否存在
        Generator oldGenerator = generatorService.getById(id);
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldGenerator.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = generatorService.updateById(generator);
        // 清理缓存
        if (result) {
            String cacheFileDir = getCacheFileDir(id);
            FileUtil.del(cacheFileDir);
        }
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 下载
     *
     * @param id
     * @return
     */
    @GetMapping("/download")
    public void downloadGeneratorById(long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        String filepath = generator.getDistPath();
        if (StrUtil.isBlank(filepath)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "产物包不存在");
        }

        // 追踪事件
        log.info("用户 {} 下载了 {}", loginUser, filepath);

        // 优先从缓存中获取安装包
        String zipFilePath = getCacheFilePath(id, filepath);
        if (FileUtil.exist(zipFilePath)) {
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应流
            Files.copy(Paths.get(zipFilePath), response.getOutputStream());
            return;
        }

        // 缓存中不存在安装包，从对象存储中下载
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }

    /**
     * 在线使用生成器
     *
     * @param generatorUseRequest 使用代码生成器时前端上传的参数
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/use")
    public void useGenerator(@RequestBody GeneratorUseRequest generatorUseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 获取用户输入的请求参数
        Long id = generatorUseRequest.getId();
        Map<String, Object> dataModel = generatorUseRequest.getDataModel();

        // 需要用户登录
        User loginUser = userService.getLoginUser(request);
        log.info("userId = {} 使用了生成器 id = {}", loginUser.getId(), id);

        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 生成器的存储路径
        String distPath = generator.getDistPath();
        if (StrUtil.isBlank(distPath)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "产物包不存在");
        }

        // 从对象存储中下载生成器的压缩包

        // 定义独立的工作空间
        String projectPath = System.getProperty("user.dir");
        // 必须要用 userId 区分，否则可能会导致输入参数文件冲突
        String tempDirPath = String.format("%s/.temp/use/%s/%s", projectPath, id, loginUser.getId());
        String zipFilePath = tempDirPath + "/dist.zip";

        if (!FileUtil.exist(zipFilePath)) {
            FileUtil.touch(zipFilePath);
        }

        // 目录不存在则创建
        if (!FileUtil.exist(tempDirPath)) {
            FileUtil.mkdir(tempDirPath);
        }

        try {
            cosManager.download(distPath, zipFilePath);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成器下载失败");
        }

        // 使用文件缓存
        String cacheFilePath = getCacheFilePath(id, distPath);
        Path cacheFilePathObj = Paths.get(cacheFilePath);
        Path zipFilePathObj = Paths.get(zipFilePath);

        if (!FileUtil.exist(zipFilePath)) {
            // 有缓存，复制文件
            if (FileUtil.exist(cacheFilePath)) {
                Files.copy(cacheFilePathObj, zipFilePathObj);
            } else {
                // 没有缓存，从对象存储下载文件
                FileUtil.touch(zipFilePath);
                try {
                    cosManager.download(distPath, zipFilePath);
                } catch (Exception e) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成器下载失败");
                }
                // 写文件缓存
                File parentFile = cacheFilePathObj.toFile().getParentFile();
                if (!FileUtil.exist(parentFile)) {
                    FileUtil.mkdir(parentFile);
                }
                Files.copy(zipFilePathObj, cacheFilePathObj);
            }
        }

        // 解压压缩包，得到脚本文件
        File unzipDistDir = ZipUtil.unzip(zipFilePath);

        // 将用户输入的参数写到 json 文件中
        String dataModelFilePath = tempDirPath + "/dataModel.json";
        String jsonStr = JSONUtil.toJsonStr(dataModel);
        FileUtil.writeUtf8String(jsonStr, dataModelFilePath);

        // 执行脚本文件

        // 找到脚本位置
        // 要注意，如果不是 windows 系统，找 generator 文件而不是 bat
        File scriptFile = FileUtil.loopFiles(unzipDistDir, 2, null)
                .stream()
                // 记得改回来
                .filter(file -> file.isFile() && "generator".equals(file.getName()))
                .findFirst()
                .orElseThrow(RuntimeException::new);

        // 添加可执行权限(Linux、Mac)
        try {
            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxrwxrwx");
            Files.setPosixFilePermissions(scriptFile.toPath(), permissions);
        } catch (Exception e) {

        }

        // 构造命令
        File scriptDir = scriptFile.getParentFile();
        // 注意，如果是 mac / linux 系统，要用 "./generator"
        String scriptAbsolutePath = scriptFile.getAbsolutePath().replace("\\", "/");
        String[] commands = new String[] {scriptAbsolutePath, "json-generate", "--file=" + dataModelFilePath};

        // 这里一定要拆分！
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(scriptDir);

        try {
            Process process = processBuilder.start();

            // 读取命令的输出
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 等待命令执行完成
            int exitCode = process.waitFor();
            System.out.println("命令执行结束，退出码：" + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "执行生成器脚本错误");
        }

        // 压缩得到的生成结果，返回给前端
        String generatedPath = scriptDir.getAbsolutePath() + "/generated";
        String resultPath = tempDirPath + "/result.zip";
        File resultFile = ZipUtil.zip(generatedPath, resultPath);

        // 设置响应头
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + resultFile.getName());
        Files.copy(resultFile.toPath(), response.getOutputStream());

        // 异步清理文件
        CompletableFuture.runAsync(() -> {
            FileUtil.del(tempDirPath);
        });
    }

    /**
     * 制作代码生成器
     *
     * @param generatorMakeRequest
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/make")
    public void makeGenerator(@RequestBody GeneratorMakeRequest generatorMakeRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1) 输入参数
        Meta meta = generatorMakeRequest.getMeta();
        String zipFilePath = generatorMakeRequest.getZipFilePath();

        // 需要用户登录
        User loginUser = userService.getLoginUser(request);
        log.info("userId = {} 在线制作生成器", loginUser.getId());

        // 2) 创建独立的工作空间，下载压缩包到本地
        String projectPath = System.getProperty("user.dir");
        String id = IdUtil.getSnowflakeNextId() + RandomUtil.randomString(6);
        String tempDirPath = String.format("%s/.temp/make/%s", projectPath, id);
        String localZipFilePath = tempDirPath + "/project.zip";

        if (!FileUtil.exist(localZipFilePath)) {
            FileUtil.touch(localZipFilePath);
        }

        // 下载文件
        try {
            cosManager.download(zipFilePath, localZipFilePath);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩包下载失败");
        }

        // 3）解压，得到项目模板文件
        File unzipDistDir = ZipUtil.unzip(localZipFilePath);

        // 4）构造 meta 对象和生成器的输出路径
        String sourceRootPath = unzipDistDir.getAbsolutePath();
        meta.getFileConfig().setSourceRootPath(sourceRootPath);
        // 校验和处理默认值
        MetaValidator.doValidAndFill(meta);
        String outputPath = tempDirPath + "/generated/" +meta.getName() ;

        // 5）调用 maker 方法制作生成器
        GenerateTemplate generateTemplate = new ZipGenerator();
        try {
            generateTemplate.doGenerate(meta, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "制作失败");
        }

        // 6）下载制作好的生成器压缩包
        String suffix = "-dist.zip";
        String zipFileName = meta.getName() + suffix;
        // 生成器压缩包的绝对路径
        String distZipFilePath = outputPath + suffix;

        // 设置响应头
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + zipFileName);
        Files.copy(Paths.get(distZipFilePath), response.getOutputStream());

        // 7）清理工作空间的文件
        CompletableFuture.runAsync(() -> {
            FileUtil.del(tempDirPath);
        });
    }

    /**
     * 缓存代码生成器
     *
     * @param generatorCacheRequest
     * @return
     */
    @PostMapping("/cache")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public void cacheGenerator(@RequestBody GeneratorCacheRequest generatorCacheRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (generatorCacheRequest == null || generatorCacheRequest.getId() == null || generatorCacheRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取生成器
        Long id = generatorCacheRequest.getId();
        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        String distpath = generator.getDistPath();
        if (StrUtil.isBlank(distpath)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "产物包不存在");
        }

        String zipFilePath = getCacheFilePath(id, distpath);

        // 新建文件
        if (!FileUtil.exist(zipFilePath)) {
            FileUtil.touch(zipFilePath);
        }

        // 下载生成器
        try {
            cosManager.download(distpath, zipFilePath);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩包下载失败");
        }
    }

    /**
     * 获取缓存路径（缓存key的设计）
     * .temp/cache/11/generator_dist/1748696431144792066/4klwqKXq-acm-template-pro-generator.zip
     * .temp/cache/id/distPath
     * @param id 生成器的id
     * @param distPath 产物包的路径
     * @return
     */
    public String getCacheFilePath(long id, String distPath) {
        String projectPath = System.getProperty("user.dir");
        String tempDirPath = String.format("%s/.temp/cache/%s", projectPath, id);
        return String.format("%s%s", tempDirPath, distPath);
    }

    public static String getPageCacheKey(GeneratorQueryRequest generatorQueryRequest) {
        String jsonStr = JSONUtil.toJsonStr(generatorQueryRequest);
        String base64 = Base64Encoder.encode(jsonStr);
        String key = "generator:page:" + base64;
        return key;
    }

    /**
     * 获取缓存文件所在的目录
     *
     * @param id 生成器 id
     * @return
     */
    public String getCacheFileDir(long id) {
        String projectPath = System.getProperty("user.dir");
        String tempDirPath = String.format("%s/.temp/cache/%s", projectPath, id);
        return tempDirPath;
    }

}
