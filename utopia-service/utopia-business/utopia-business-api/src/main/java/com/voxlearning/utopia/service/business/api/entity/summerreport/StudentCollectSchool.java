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

package com.voxlearning.utopia.service.business.api.entity.summerreport;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by jiangpeng on 16/6/12.
 */

@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-bigdata")
@DocumentCollection(collection = "student_collect_school")
@DocumentIndexes({
        @DocumentIndex(def = "{'county_id':1}", background = true),
        @DocumentIndex(def = "{'student_id':1}", background = true),
        @DocumentIndex(def = "{'ct':-1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160612")
public class StudentCollectSchool implements Serializable {

    @DocumentId private String id;
    @DocumentField("ct") @DocumentCreateTimestamp private Date createDatetime;     //创建时间
    @DocumentField("student_id") private Long studentId;                           //学生id
    @DocumentField("school_id") private String schoolId;                           //收集的学校id,对应CollectSchoolInfo的id
    @DocumentField("picture_url") private String pictureUrl;                       //上传的图片Url
    @DocumentField("student_name") private String studentName;                       //冗余的学生姓名
    @DocumentField("longitude") private Double longitude;                          //获取的经纬度
    @DocumentField("latitude") private Double latitude;                            //获取的到经纬度
    @DocumentField("description") private String description;                       //添加的学校描述
    @DocumentField("province_id") private Integer provinceId;                       //定位拿到的省id
    @DocumentField("city_id") private Integer cityId;                               //定位拿到的城市id
    @DocumentField("county_id") private Integer countyId;                           //定位拿到的区id

    @DocumentField("school_name") private String schoolName;                        //冗余的采集学校名称 对应CollectSchoolInfo里的


    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(StudentCollectSchool.class, id);
    }

    public static String ck_studentId(Long studentId) {
        return CacheKeyGenerator.generateCacheKey(StudentCollectSchool.class, "STUDENTID", studentId);
    }

    public static String ck_countyId(Integer countyId) {
        return CacheKeyGenerator.generateCacheKey(StudentCollectSchool.class, "COUNTYID", countyId);
    }

    public static StudentCollectSchool newInstance() {
        StudentCollectSchool instance = new StudentCollectSchool();
        return instance;
    }
}
