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
import com.voxlearning.alps.annotation.dao.*;
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
@DocumentCollection(collection = "collect_school_info")
@DocumentIndexes({
        @DocumentIndex(def = "{'school_name':1}", background = true, unique = true),
        @DocumentIndex(def = "{'ct':-1}", background = true),
        @DocumentIndex(def = "{'county_id':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160612")
public class CollectSchoolInfo implements Serializable {

    @DocumentId private String id;
    @DocumentField("ct") @DocumentCreateTimestamp private Date createDatetime;     //创建时间
    @DocumentField("ut") @DocumentUpdateTimestamp private Date updateDatetime;      //修改时间
    @DocumentField("school_name") private String schoolName;                        //学校名称
    @DocumentField("province_id") private Integer provinceId;                       //省份id
    @DocumentField("city_id") private Integer cityId;                               //
    @DocumentField("county_id") private Integer countyId;
    @DocumentField("is_standard") private Boolean isStandard;

    public static String ck_schoolName(String schoolName) {
        return CacheKeyGenerator.generateCacheKey(CollectSchoolInfo.class, "SCHOOLNAME", schoolName);
    }

    public static String ck_countyId(Integer countyId) {
        return CacheKeyGenerator.generateCacheKey(CollectSchoolInfo.class, "COUNTYID", countyId);
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(CollectSchoolInfo.class, id);
    }

    public static CollectSchoolInfo instance(String schoolName) {
        CollectSchoolInfo ins = new CollectSchoolInfo();
        ins.setSchoolName(schoolName);
        ins.setIsStandard(false);
        return ins;
    }

}
