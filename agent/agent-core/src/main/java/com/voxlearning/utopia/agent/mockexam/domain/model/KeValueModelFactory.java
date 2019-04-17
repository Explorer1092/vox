package com.voxlearning.utopia.agent.mockexam.domain.model;

import com.google.common.collect.Lists;
import com.voxlearning.alps.core.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 可命名的模型工厂<br>
 * 用以id、name用逗号分隔这种讨厌的、冗长的切分、组装过程
 *
 * @author xiaolei.li
 * @version 2018/8/18
 */
public class KeValueModelFactory {

    /**
     * 分隔符
     */
    public static final String DEFAULT_SEPARATOR = ",";

    /**
     * 个体工厂
     *
     * @param <T> 类型
     */
    public interface ModelFactory<T> {

        /**
         * 创建
         *
         * @param key   键
         * @param value 值
         * @return 个体
         */
        T create(String key, String value);
    }


    /**
     * 构建一个列表
     *
     * @param keys         键
     * @param values       值
     * @param modelFactory 个体工厂
     * @param <T>          个体类型
     * @return
     */
    public static <T> List<T> build(String keys, String values, ModelFactory<T> modelFactory) {
        return build(keys, values, DEFAULT_SEPARATOR, modelFactory);
    }

    /**
     * 构建一个列表
     *
     * @param keys         ids字符串
     * @param values       names字符串
     * @param separator    分隔符
     * @param modelFactory 个体工厂
     * @param <T>          个体类型
     * @return 列表
     */
    public static <T> List<T> build(String keys, String values,
                                    String separator, ModelFactory<T> modelFactory) {
        ArrayList<T> list = Lists.newArrayList();
        if (StringUtils.isNotBlank(keys)
                && StringUtils.isNotBlank(values)) {
            String[] keyArray = Arrays.stream(keys.split(separator))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .toArray(String[]::new);
            String[] nameArray = Arrays.stream(values.split(separator))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .toArray(String[]::new);
            for (int i = 0; i < keyArray.length && i < nameArray.length; i++)
                list.add(modelFactory.create(keyArray[i], nameArray[i]));
        }
        return list;
    }
}
