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


import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.*;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 基于DailyIncreasementData基础上的汇总
 *
 * @author Maofeng Lu
 * @since 14-7-24 下午4:18
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "daily_increasement_region_data{}", dynamic = true)
@DocumentIndexes({
        @DocumentIndex(def = "{'region_code':1}", background = true),
        @DocumentIndex(def = "{'date':1,'region_code':1}", background = true)
})
public class DailyIncreasementRegionData implements Serializable {
    private static final long serialVersionUID = -251243482337877943L;

    @DocumentId private String id;
    private Integer status;
    private Integer region_code;        //区域编码
    private String region_name;         //区域名称
    private Integer date;               //统计的天
    private Integer teacher_active;     //老师日活跃数量
    private Integer teacher_register;   //老师日注册数量
    private Integer teacher_auth;       //老师日认证数量
    private Integer teacher_use;        //老师日使用数量
    private Integer student_auth;       //学生日认证数量
    private Integer student_auth_lv;    //一二年级学生日认证数量
    private Integer student_use;        //学生日使用数量
    private Integer student_active;     //学生日活跃数量
    private Integer student_register;   //学生日注册数量
    private Integer online_pay;         //学生日在线付费金额
    private SchoolLevel schoolLevel;        //学校等级
}
