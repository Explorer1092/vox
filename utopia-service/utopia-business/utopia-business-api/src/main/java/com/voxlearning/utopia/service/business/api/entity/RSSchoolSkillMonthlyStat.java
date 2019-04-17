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

package com.voxlearning.utopia.service.business.api.entity;


import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentRangeable;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import com.voxlearning.utopia.service.business.api.entity.embedded.RSSkillMonthlyData;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by Changyuan on 2015/1/17.
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-rstaff")
@DocumentCollection(collection = "rs_school_skill_month_stat_{}", dynamic = true)
@DocumentIndexes({
        @DocumentIndex(def = "{'acode':1}", background = true),
        @DocumentIndex(def = "{'ccode':1}", background = true),
        @DocumentIndex(def = "{'schoolId':1}", background = true)
})
@DocumentRangeable(range = DateRangeType.W, age = 0)
public class RSSchoolSkillMonthlyStat implements Serializable, TimestampTouchable {
    private static final long serialVersionUID = -462187663947931049L;

    @DocumentId private String id;
    private Long schoolId;
    private String schoolName;
    private String areaName;
    private Long acode;
    private Long ccode;
    private String cityName;
    private Date createAt;
    private List<RSSkillMonthlyData> skills;

    @Override
    public void touchCreateTime(long timestamp) {
        if (createAt == null) createAt = new Date(timestamp);
    }
}
