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


import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.*;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "daily_increasement_data{}", dynamic = true)
@DocumentIndexes({
        @DocumentIndex(def = "{'school_id':1}", background = true),
        @DocumentIndex(def = "{'date':1}", background = true),
        @DocumentIndex(def = "{'areacode':1,'date':-1}", background = true),
})
@UtopiaCacheRevision("20160524")
public class DailyIncreasementData implements Serializable {
    private static final long serialVersionUID = -5235208234189862975L;

    @DocumentId private String id;
    private Integer status;
    private Integer teacher_active;     //老师日活跃数量
    private Integer student_active;     //学生日活跃数量
    private Integer teacher_auth;       //老师日认证数量
    private Integer student_auth;       //学生日认证数量
    private Integer student_auth_lv;    //一年级学生日认证数量
    private Integer teacher_register;   //老师日注册数量
    private Integer student_register;   //学生日注册数量
    private Integer teacher_use;        //老师日使用数量
    private Integer student_use;        //学生日使用数量
    private Integer online_pay;         //学生日在线付费金额
    private Integer procode;            //省code
    private String proname;             //省名称
    private Integer citycode;           //市code
    private String cityname;            //市名称
    private Integer areacode;           //区code
    private String areaname;            //区名称
    private Long school_id;             //学校ID
    private String school_name;         //学校名称
    private Integer date;               //统计的天
    private SchoolLevel schoolLevel;        //学校等级
}
