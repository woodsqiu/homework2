package com.lagou.edu.config;

/**
 *
 * @author wangzhiqiu
 * @since 20/7/8 下午7:35
 */
public interface ApplicationContext {
    // 获取bean
    Object getBean(String beanId);
}
