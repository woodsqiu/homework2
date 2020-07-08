package com.lagou.edu.config;

import com.lagou.edu.annotation.Autowired;
import com.lagou.edu.annotation.Service;
import com.lagou.edu.annotation.Transactional;
import com.lagou.edu.factory.ProxyFactory;
import com.lagou.edu.utils.CaseWriteUtil;
import com.lagou.edu.utils.PackageScanner;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 手写Spring专题 注解版本注入bean
 *
 * @author wangzhiqiu
 * @since 20/7/7 下午9:19
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ClassPathXmlApplicationContext implements ApplicationContext{

    /**
     * 扫包范围
     */
    private static String packageName = "com.lagou.edu";
    /**
     * 所有 bean 集合
     */
    private static Map<String, Object> beanMap = new ConcurrentHashMap<>();


    public ClassPathXmlApplicationContext() {
        //类初始化时初始化所有bean
        initBean();
    }


    /**
     * 初始化bean
     */
    public void initBean() {
        try {
            // 1.使用反射技术获取指定包下所有的类
            List<Class<?>> classByPackageName = PackageScanner.getClasses(packageName);
            // 2.获取所有类上有@Service 注解的类,全部注入到beanMap
            // 遍历有@Service 注解的类
            for (Class classInfo : classByPackageName) {
                // 判断该类上属否存在 @ExtService注解
                Service serviceAnnotation = (Service) classInfo.getDeclaredAnnotation(Service.class);
                if (serviceAnnotation == null) {
                    continue;
                }
                String beanId = serviceAnnotation.value();
                // 如果没有配置bean名称，则取类名首字母转小写
                if ("".equals(beanId)) {
                    beanId = CaseWriteUtil.toLowerCaseFirstOne(classInfo.getSimpleName());
                }
                // 通过反射获取到该类对象
                Object newInstance = classInfo.newInstance();
                beanMap.put(beanId, newInstance);
                //打印注入信息
                System.out.println("springIoc initBean -->  " + beanId + " = " + newInstance.getClass().getPackage().getName() + "." + classInfo.getSimpleName());
            }

            // 注入依赖
            injectDependency();
            // 判断transactional注解是否存在，如果存在则生成代理对象，管理事务
            transactionManage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 遍历所有bean，判断transactional注解是否存在，如果存在则生成代理对象，管理事务
     */
    private void transactionManage() {
        for (Map.Entry<String, Object> stringObjectEntry : beanMap.entrySet()) {
            Object instance = stringObjectEntry.getValue();
            Class<?> clazz = instance.getClass();
            Transactional transactionalAnnotation = clazz.getDeclaredAnnotation(Transactional.class);
            if (transactionalAnnotation != null) {
                // 获取事务代理工厂
                ProxyFactory proxyFactory = (ProxyFactory) beanMap.get("proxyFactory");
                Class<?>[] interfaces = clazz.getInterfaces();
                Object proxy = null;
                // 如果没有实现接口，使用cglib动态代理
                if (interfaces.length == 0) {
                    proxy = proxyFactory.getCglibProxy(instance);
                } else {
                    proxy = proxyFactory.getJdkProxy(instance);
                }
                // 将代理对象替换原对象，实现事务管理
                beanMap.put(stringObjectEntry.getKey(), proxy);
            }
        }
    }


    /**
     * 依赖注入
     *
     * @return void
     * @date 2019/10/26 14:39
     */
    public void injectDependency() throws IllegalAccessException {
        //循环所有带有 @Service注解的类
        for (Object object : beanMap.values()) {
            // 1、通过反射获取类对象
            Class<? extends Object> classInfo = object.getClass();
            // 2、获得类的所有声明的字段，即包括public、private和protected
            Field[] declaredFields = classInfo.getDeclaredFields();
            // 循环类所有字段
            for (Field field : declaredFields) {
                // 3、判断字段上是否存在@ExtResource注解
                Autowired autowired = field.getDeclaredAnnotation(Autowired.class);
                if (autowired == null) {
                    continue;
                }
                // 4.根据字段名查询是否存在 bean 对象（类对象）
                Object bean = beanMap.get(field.getName());
                if (bean != null) {
                    // 私有访问允许访问
                    field.setAccessible(true);
                    // 给带有@ExtResource 注解的属性赋值对应的类对象
                    field.set(object, bean);
                    //打印参数
                    System.out.println("初始化  依赖注入 -->  " + field.getName() + "=" + field.getType());
                }
            }
        }
    }


    /**
     * 通过id获取bean对象
     *
     * @param beanId
     * @return bean对象
     */
    @Override
    public Object getBean(String beanId) {
        // 通过beanId查询beanMap集合对应bean(类对象)
        return beanMap.get(beanId);
    }
}
