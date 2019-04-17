/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.afenti.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.*;

/**
 * 用户当前学期默认视频课程存储(2017-8-10开始不再使用)
 *
 * @author liu jingchao
 * @since 2017/03/27
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "newInstance")
@DocumentConnection(configName = "hs_afenti")
@DocumentTable(table = "VOX_AFENTI_USER_COURSE_REF")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20170327")
public class UserCourseRef extends AbstractDatabaseEntityWithDisabledField {

    private static final long serialVersionUID = -2025695717319813667L;
    @UtopiaSqlColumn(name = "USER_ID")  // 用户ID
    @NonNull
    private Long userId;

    @UtopiaSqlColumn(name = "SUBJECT")      // 学科
    @NonNull
    private Subject subject;

    @UtopiaSqlColumn(name = "COURSE_ID")      // 默认课程ID
    @NonNull
    private String courseId;

    public static String cacheKeyFromUserIdAndSubject(Long userId, Subject subject) {
        return CacheKeyGenerator.generateCacheKey(UserCourseRef.class, new String[]{"userId", "subject"}, new Object[]{userId, subject});
    }
}
