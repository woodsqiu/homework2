package com.lagou.edu.test;

import com.lagou.edu.config.ApplicationContext;
import com.lagou.edu.config.ClassPathXmlApplicationContext;
import com.lagou.edu.service.TransferService;
import org.junit.Test;

/**
 * 测试类
 *
 * @author wangzhiqiu
 * @since 20/7/7 下午9:22
 */
public class TestService {

    @Test
    public void testAnnotation() throws Exception {
        // 初始化所有类的bean
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext();
        //测试
        TransferService transferService = (TransferService) applicationContext.getBean("transferService");
        transferService.transfer("6029621011000", "6029621011001", 100);
    }
}