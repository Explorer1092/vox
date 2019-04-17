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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shiwei.liao
 * @since 2015/9/1.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentFeedbackType2 {

    PERFORMANCE(1, 1, "业绩数据"),
    TEACHER(2, 1, "教师数据"),
    SCHOOL(3, 1, "学校数据"),
    TASK(4, 2, "任务流转问题"),
    RECORD(5, 2, "工作记录问题"),
    VISIT(6, 2, "拜访计划问题"),
    USABILITY(7, 3, "操作体验问题"),
    UI(8, 3, "视觉体验问题"),
    BUG(9, 3, "BUG问题"),
    OTHER(10, 3, "其他问题");

    public final int id;
    public final int firstTypeId;
    public final String desc;


    public static final Map<String, AgentFeedbackType2> value_map;

    static {
        value_map = new HashMap<>();
        for (AgentFeedbackType2 type : AgentFeedbackType2.values()) {
            value_map.put(ConversionUtils.toString(type.getId()), type);
        }
    }

    public static List<Map<String, Object>> toMapList() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (AgentFeedbackType2 type : AgentFeedbackType2.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id",type.getId());
            map.put("desc",type.getDesc());
            map.put("first",type.getFirstTypeId());
            list.add(map);
        }
        return list;
    }

    public static AgentFeedbackType2 of(Integer id) {
        if (id == null) {
            return null;
        }
        return value_map.get(ConversionUtils.toString(id));
    }

}
