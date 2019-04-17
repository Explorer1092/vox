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

package com.voxlearning.utopia.service.business.api.entity.extended;


import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.meta.Term;
import lombok.Data;

import java.util.Date;
import java.util.Set;

/**
 * @author changyuan.liu
 * @since 2015/3/26
 */
@Data
abstract public class AbstractRSSchoolHomeworkBehaviorStat {
    @DocumentId
    private String id;

    private String schoolId;
    private String schoolName;
    private Long acode;
    private String areaName;
    private Long ccode;
    private String cityName;
    private Long pcode;
    private String provinceName;
    private Integer stuNum;
    private Long stuTimes;
    private Integer teacherNum;
    private Long teacherTimes;

    private Set<Long> stuIds;
    private Set<Long> teacherIds;

    private Date createAt;
    private Date updateAt;

    public static String generateId(Integer year, Term term, String schoolId) {
        return year + "_" + term.getKey() + "_" + schoolId;
    }
}
