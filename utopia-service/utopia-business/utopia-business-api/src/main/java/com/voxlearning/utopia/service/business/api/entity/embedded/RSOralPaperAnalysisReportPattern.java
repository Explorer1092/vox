/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.api.entity.embedded;

import com.voxlearning.alps.annotation.dao.DocumentField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * 记录口语报告中题型相关数据
 *
 * @author changyuan.liu
 * @since 2015/5/18
 */
@Getter
@Setter
public class RSOralPaperAnalysisReportPattern implements Serializable {
    private static final long serialVersionUID = -7072874732544475951L;

    @DocumentField("ms") private Double machineScore;           // 机器打分总分
    @DocumentField("ts") private Double teacherScore;           // 老师打分总分
    @DocumentField("qs") private Map<String, Double> questions; // 每道题的得分：id=>score
}
