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

package com.voxlearning.utopia.service.newhomework.api.entity;

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
import com.voxlearning.utopia.service.homework.api.constant.CheatingTeacherStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Summer Yang on 2015/7/9.
 */
@Getter
@Setter
@EqualsAndHashCode
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-statistics")
@DocumentCollection(collection = "vox_possible_cheating_teacher")
@DocumentIndexes({
        @DocumentIndex(def = "{'teacherId':1}", background = true),
        @DocumentIndex(def = "{'status':1,'createDatetime':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20150810")
public class PossibleCheatingTeacher implements Serializable {
    private static final long serialVersionUID = -4164458125210785076L;

    @DocumentId private String id;
    @DocumentCreateTimestamp private Date createDatetime;
    @DocumentUpdateTimestamp private Date updateDatetime;
    private Long teacherId;
    private CheatingTeacherStatus status;
    private Boolean disabled;
    private String desc;
    private Date lastCheatDate;//最近一次作弊时间

    public static PossibleCheatingTeacher newInstance(Long teacherId, CheatingTeacherStatus status, String desc, Date lastCheatDate) {
        PossibleCheatingTeacher teacher = new PossibleCheatingTeacher();
        teacher.setStatus(status);
        teacher.setTeacherId(teacherId);
        teacher.setDesc(desc);
        teacher.setLastCheatDate(lastCheatDate);
        return teacher;
    }

    public static String cacheKeyFromTeacherId(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(PossibleCheatingTeacher.class, "teacherId", teacherId);
    }
}
