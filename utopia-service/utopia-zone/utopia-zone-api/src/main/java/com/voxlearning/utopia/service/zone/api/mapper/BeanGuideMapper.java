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

package com.voxlearning.utopia.service.zone.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * 学豆攻略卡片 Mapper
 */
@Getter
@Setter
public class BeanGuideMapper implements Serializable {
    private static final long serialVersionUID = 5119258001755952152L;

    private String title;              // 标题
    private String description;        // 描述文案
    private boolean enable;            // 是否可跳转
    private String linkUrl;            // 跳转链接
    private String type;               // 类型
    private Map<String, Object> data;  // 数据

    public BeanGuideMapper(String title, String description, String type) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.enable = false;
    }

    public static BeanGuideMapper homework() {
        return new BeanGuideMapper(
                "完成作业", "按时完成老师布置的线上作业", "homework"
        );
    }

    public static BeanGuideMapper exam() {
        return new BeanGuideMapper(
                "完成测试", "按时完成老师布置的测验", "exam"
        );
    }

}
