package com.lagou.edu.utils;

/**
 * 大小写转换工具类
 *
 * @author wangzhiqiu
 * @since 20/7/7 下午9:13
 */
public class CaseWriteUtil {
    /**
     * 首字母转小写
     *
     * @param simpleName
     * @return
     */
    public static String toLowerCaseFirstOne(String simpleName) {
        if (Character.isLowerCase(simpleName.charAt(0))) {
            return simpleName;
        } else {
            return (new StringBuilder()).append(Character.toLowerCase(simpleName.charAt(0))).append(simpleName.substring(1)).toString();
        }
    }
}
