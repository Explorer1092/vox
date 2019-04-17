package com.voxlearning.utopia.agent.mockexam.controller.view;

/**
 * 枚举列表构建器
 *
 * @author xiaolei.li
 * @version 2018/8/16
 */

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 枚举列表
 *
 * @author xiaolei.li
 * @version 2018/8/16
 */
public class EnumListBuilder {
    public static <E extends Enum> List<KeyValue<String, String>> build(E[] enums, Describable describable) {
        List<KeyValue<String, String>> list = Arrays.stream(enums)
                .map(i -> {
                    KeyValue<String, String> kv = new KeyValue<>();
                    kv.setKey(i.name());
                    kv.setValue(describable.getDesc(i));
                    return kv;
                }).collect(Collectors.toList());

        return list;
    }
}