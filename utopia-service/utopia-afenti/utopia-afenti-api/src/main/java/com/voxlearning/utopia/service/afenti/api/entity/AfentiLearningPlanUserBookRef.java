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

package com.voxlearning.utopia.service.afenti.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Comparator;

/**
 * 阿分题学习计划学生课本关联
 * <pre>
 *     +------------+------------+------+-----+---------+----------------+
 *     | Field      | Type       | Null | Key | Default | Extra          |
 *     +------------+------------+------+-----+---------+----------------+
 *     | ID         | bigint(20) | NO   | PRI | NULL    | auto_increment |
 *     | USER_ID    | bigint(20) | NO   | MUL | NULL    |                |
 *     | BOOK_ID    | bigint(20) | NO   |     | NULL    |                |
 *     | ACTIVE     | bit(1)     | NO   |     | b'1'    |                |
 *     | CREATETIME | datetime   | NO   |     | NULL    |                |
 *     | UPDATETIME | datetime   | NO   |     | NULL    |                |
 *     +------------+------------+------+-----+---------+----------------+
 * </pre>
 *
 * @author Maofeng Lu
 * @author Guohong Tan
 * @author Xiaohai Zhang
 * @serial
 * @since 2013-09-09 11:36AM
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_afenti")
@DocumentTable(table = "VOX_AFENTI_LEARNING_PLAN_USER_BOOK_REF")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@AllArgsConstructor(staticName = "newInstance")
@NoArgsConstructor
@UtopiaCacheRevision("20170707")
public class AfentiLearningPlanUserBookRef extends LongIdEntity {

    private static final long serialVersionUID = 4346761357543419080L;

    @UtopiaSqlColumn(name = "USER_ID") private Long userId;
    @UtopiaSqlColumn(name = "ACTIVE") private Boolean active;
    @UtopiaSqlColumn(name = "NEW_BOOK_ID") private String newBookId;
    @UtopiaSqlColumn(name = "SUBJECT") private Subject subject;
    @UtopiaSqlColumn(name = "TYPE") private AfentiLearningType type;

    public static String ck_uid_s(Long userId, Subject subject) {
        return CacheKeyGenerator.generateCacheKey(AfentiLearningPlanUserBookRef.class,
                new String[]{"UID", "S"}, new Object[]{userId, subject});
    }

    public static class UPDATETIME_DESC_ACTIVE_PRIOR implements Comparator<AfentiLearningPlanUserBookRef> {
        @Override
        public int compare(AfentiLearningPlanUserBookRef o1, AfentiLearningPlanUserBookRef o2) {
            int ret = Long.compare(o2.fetchUpdateTimestamp(), o1.fetchUpdateTimestamp());
            if (ret != 0) return ret;
            return Boolean.compare(o2.getActive(), o1.getActive());
        }
    }
}
