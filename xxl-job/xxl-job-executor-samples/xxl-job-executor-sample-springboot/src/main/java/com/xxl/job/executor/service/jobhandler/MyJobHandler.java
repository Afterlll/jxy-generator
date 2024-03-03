package com.xxl.job.executor.service.jobhandler;

import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;


/**
 * 示例任务
 */
@Component
public class MyJobHandler {

    @XxlJob("myJobHandler")
    public void myJobHandler() throws Exception {
        Thread.sleep(3);
        System.out.println("你好，我是江喜原");
    }

}
