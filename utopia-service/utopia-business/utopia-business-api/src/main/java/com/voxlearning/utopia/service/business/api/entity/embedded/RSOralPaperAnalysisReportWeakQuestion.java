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

/**
 * @author changyuan.liu
 * @since 2015/5/18
 */
@Getter
@Setter
public class RSOralPaperAnalysisReportWeakQuestion implements Serializable {
    private static final long serialVersionUID = 2969534394395570869L;

    @DocumentField("eid") private String eid;       // 题目id
    private String snapshot;                        // 题目快照
    @DocumentField("tScore") private Double score;  // 老师打分
}
