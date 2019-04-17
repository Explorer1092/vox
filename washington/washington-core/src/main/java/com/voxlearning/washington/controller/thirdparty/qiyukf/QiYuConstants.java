package com.voxlearning.washington.controller.thirdparty.qiyukf;

import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 七鱼相关常量类
 *
 * @author Wenlong Meng
 * @version 1.0.0
 * @date 2018-08-28
 */
public class QiYuConstants {

    //启用机器人
    public static final Set<String> ENABLE_ROBOT = Sets.newHashSet("student", "parent", "teacher", "marketer");

    /**
     * 问题类型映射
     */
    public static final Map<String, String> QUESTION_TYPE_MAP = new HashMap<>();
    static {
        QUESTION_TYPE_MAP.put("question_integral", "question_award");
    }
}
