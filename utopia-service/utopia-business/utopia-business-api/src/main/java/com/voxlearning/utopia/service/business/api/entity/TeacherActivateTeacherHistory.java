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


import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.api.constant.ActivationType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 教师激活教师表
 *
 * @author RuiBao
 * @author Xiaohai Zhang
 * @version 0.1
 * @serial
 * @since 13-11-27
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "WashingtonDatabase")
@DocumentCollection(collection = "teacherActivateTeacherHistory")
@DocumentIndexes({
        @DocumentIndex(def = "{'inviterId':1}", background = true),
        @DocumentIndex(def = "{'inviteeId':1}", background = true),
        @DocumentIndex(def = "{'createTime':-1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160310")
public class TeacherActivateTeacherHistory implements Serializable {
    private static final long serialVersionUID = -9188478966940542981L;

    @DocumentId private String id;
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;
    private Long inviterId;//发起的唤醒老师
    private Long inviteeId;//被唤醒的老师
    private Boolean success;
    private Boolean over;
    private ActivationType activationType;
    private Map<String, Object> extensionAttributes;

    // ========================================================================
    // Cache support
    // ========================================================================

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(TeacherActivateTeacherHistory.class, id);
    }

    public static String ck_inviterId(Long inviterId) {
        return CacheKeyGenerator.generateCacheKey(TeacherActivateTeacherHistory.class, "INVITER", inviterId);
    }

    public static String ck_inviteeId(Long inviteeId) {
        return CacheKeyGenerator.generateCacheKey(TeacherActivateTeacherHistory.class, "INVITEE", inviteeId);
    }
}
