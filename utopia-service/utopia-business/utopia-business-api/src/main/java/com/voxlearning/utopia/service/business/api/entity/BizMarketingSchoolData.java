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
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import lombok.Data;

import java.io.Serializable;

@Data
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-statistics")
@DocumentCollection(collection = "vox_marketing_school_data")
@DocumentIndexes({
        @DocumentIndex(def = "{'ts':-1}", background = true),
        @DocumentIndex(def = "{'province_code':1}", background = true),
        @DocumentIndex(def = "{'city_code':1}", background = true),
        @DocumentIndex(def = "{'area_code':1}", background = true),
        @DocumentIndex(def = "{'subject':1}", background = true),
        @DocumentIndex(def = "{'authentication_state':1}", background = true),
        @DocumentIndex(def = "{'restaff_auth_total':1}", background = true),
        @DocumentIndex(def = "{'school_id':-1}", background = true)
})
public class BizMarketingSchoolData implements Serializable {
    @DocumentId private String id;
    private Integer ts;

    @DocumentField("teacher_id") private Long teacherId;
    @DocumentField("teacher_name") private String teacherName;
    @DocumentField("teacher_mobile") private String teacherSensitiveMobile;
    @DocumentField("teacher_email") private String teacherSensitiveEmail;
    @DocumentField("authentication_state") private Integer authenticationState;
    @DocumentField("subject") private String subject;
    @DocumentField("register_time") private String registerTime;

    @DocumentField("group_type") private String groupType;
    @DocumentField("group_id") private Integer groupId;
    @DocumentField("group_name") private String groupName;

    @DocumentField("parent_count") private Integer parentCount;
    @DocumentField("bind_parent_count") private Integer bindParentCount;

    @DocumentField("dohw_auth_stu_count") private Integer dohwAuthStuCount;
    @DocumentField("dohw_stu_count") private Integer dohwStuCount;

    @DocumentField("dohw_stu_count_2w") private Integer dohwStuCount2W;
    @DocumentField("auth_stu_total") private Integer authStuTotal;
    @DocumentField("pay_stu_total") private Integer payStuTotal;
    @DocumentField("subjectaccom_threetime_stu_total") private Integer subjectaccomThreetimeStuTotal;

    @DocumentField("homework_count") private Integer homeworkCount;
    @DocumentField("homework_count_2w") private Integer homeworkCount2W;

    @DocumentField("restaff_auth_count") private Integer restaffAuthCount;
    @DocumentField("restaff_auth_total") private Integer restaffAuthTotal;

    @DocumentField("class_id") private Integer classId;
    @DocumentField("class_number") private Integer classNumber;
    @DocumentField("class_name") private String className;
    @DocumentField("class_size") private Integer classSize;
    @DocumentField("class_level") private String classLevel;

    @DocumentField("school_id") private Long schoolId;
    @DocumentField("school_name") private String schoolName;
    @DocumentField("school_payopen") private String schoolPayopen;

    @DocumentField("province_code") private Integer provinceCode;
    @DocumentField("province") private String province;

    @DocumentField("city_code") private Integer cityCode;
    @DocumentField("city") private String city;

    @DocumentField("area_code") private Integer areaCode;
    @DocumentField("area") private String area;

    private Integer ifVoice;
}
