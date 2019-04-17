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

package com.voxlearning.utopia.service.newexam.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/1/25
 */
@Getter
@Setter
@ToString
@DocumentConnection(configName = "mongo-siberia")
@DocumentDatabase(database = "vox-newexam")
@DocumentCollection(collection = "group_exam_registration")
@DocumentIndexes({
        @DocumentIndex(def = "{'clazzGroupId':1, 'beenCanceled':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190125")
public class GroupExamRegistration implements Serializable {
    private static final long serialVersionUID = -4579435611418301753L;

    @DocumentId
    private String id;                       // 主键ID(年月-考试ID-班组ID, 例:201809-E_10300304132247-545561)
    private String newExamId;                // 考试ID
    private Boolean beenCanceled;            // 是否取消报名
    private Date registerAt;                 // 报名时间
    private Long clazzGroupId;               // 班组级ID
    @DocumentUpdateTimestamp
    private Date updateAt;                   // 修改时间

    public static String generateId(Date newExamCreateAt, String newExamId, Long clazzGroupId) {
        MonthRange monthRange = MonthRange.newInstance(newExamCreateAt.getTime());
        return StringUtils.join(monthRange.toString(), "-", newExamId, "-", clazzGroupId);
    }


    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(GroupExamRegistration.class, id);
    }

    public static String ck_clazzGroupId(Long clazzGroupId) {
        return CacheKeyGenerator.generateCacheKey(GroupExamRegistration.class,
                new String[]{"CG"},
                new Object[]{clazzGroupId});
    }

}
