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
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.convert.SafeConverter;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by jiangpeng on 16/6/13.
 */

@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-bigdata")
@DocumentCollection(collection = "student_city_interview_count")
@DocumentIndexes({
        @DocumentIndex(def = "{'city_id':1,'count':-1}", background = true),
        @DocumentIndex(def = "{'ct':-1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160620")
public class StudentCityInterviewCount implements Serializable {

    @DocumentId private String id;  //主键就是学生id
    @DocumentField("ut") @DocumentUpdateTimestamp private Date updateDatetime;      //修改时间
    @DocumentField("city_id") private Integer cityId;
    @DocumentField("count") private Integer count;
    @DocumentField("school_name") private String schoolName; //所在学校名称 冗余;注意不是采访的学校
    @DocumentField("student_name") private String studentName; // 学生姓名 冗余


    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(StudentCityInterviewCount.class, id);
    }


    public static String ck_cityId(Integer cityId) {
        return CacheKeyGenerator.generateCacheKey(StudentCityInterviewCount.class, "CITYID", cityId);
    }

    public Long fetchStudentId() {
        return SafeConverter.toLong(id);
    }

}
