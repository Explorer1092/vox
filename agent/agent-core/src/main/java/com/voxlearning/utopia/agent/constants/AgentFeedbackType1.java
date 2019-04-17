/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.constants;

import com.voxlearning.alps.lang.convert.ConversionUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shiwei.liao
 * @since 2015/9/1.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentFeedbackType1 {

    DATAINFO(1, "数据信息问题"),
    TOOLQUESTION(2, "工具操作问题"),
    OTHER(3, "其它问题");

    public final int id;
    public final String desc;

    public static final Map<Integer, AgentFeedbackType1> value_map;

    static {
        value_map = new HashMap<>();
        for (AgentFeedbackType1 type : AgentFeedbackType1.values()) {
            value_map.put(type.getId(), type);
        }
    }

    public static Map<String, String> toDescMap() {
        Map<String, String> map = new HashMap<>();
        for (AgentFeedbackType1 type : AgentFeedbackType1.values()) {
            map.put(ConversionUtils.toString(type.getId()), type.getDesc());
        }
        return map;
    }

    public static AgentFeedbackType1 of(Integer id) {
        if (id == null) {
            return null;
        }
        return value_map.get(id);
    }

}
