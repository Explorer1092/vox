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
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkContentType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author guoqiang.li
 * @since 2016/2/29
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newhomework")
@DocumentCollection(collection = "total_assignment_record")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160831")
public class TotalAssignmentRecord implements Serializable {
    private static final long serialVersionUID = 1000598177031309901L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    private HomeworkContentType contentType;                // 作业内容形式
    private Subject subject;                                // 学科
    private Integer assignTimes;                            // 总使用次数
    @DocumentCreateTimestamp
    private Long createTimestamp;                           // 创建时间
    @DocumentUpdateTimestamp
    private Long updateTimestamp;                           // 修改时间

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(TotalAssignmentRecord.class, id);
    }
}
