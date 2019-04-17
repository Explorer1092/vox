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

/**
 * @author changyuan.liu
 * @since 2015/3/26
 */
@Data
abstract public class AbstractRSAreaHomeworkBehaviorStat {
    @DocumentId
    private String id;

    private String acode;
    private String areaName;
    private Long ccode;
    private String cityName;
    private Long pcode;
    private String provinceName;
    private Integer stuNum;
    private Long stuTimes;
    private Integer teacherNum;
    private Long teacherTimes;
    private Integer schoolNum;

    private Date createAt;
    private Date updateAt;

    public static String generateId(Integer year, Term term, String acode) {
        return year + "_" + term.getKey() + "_" + acode;
    }
}
