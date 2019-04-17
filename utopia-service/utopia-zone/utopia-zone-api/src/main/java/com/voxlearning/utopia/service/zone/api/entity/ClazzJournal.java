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

package com.voxlearning.utopia.service.zone.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 班级动态--按班级存储
 * 为了减少数据库的查询，为ClazzJournal增加缓存的支持。
 * 3个缓存维度
 * A. 主键对应存储每个完整的ClazzJournal对象
 * B. 以CLAZZ_ID为键值，存储 ComplexID set
 * C. 以RELEVENT_USER_ID为键值，存储 ComplexID set
 * D. 以JOURNAL_TYPE为键值，存储 ComplexID set
 * 这张表的数据是每个星期清除一次，数据的最大有效时间范围是2个星期（当前周向前推2周）
 * <p>
 * com.voxlearning.utopia.queue.zone.ClazzJournalCreator 正在使用这个数据结构，修改字段时请注意
 *
 * @author RuiBao
 * @version 0.1
 * @since 13-8-7
 */
@Getter
@Setter
@DocumentTable(table = "VOX_CLAZZ_JOURNAL")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class ClazzJournal extends AbstractDatabaseEntity {
    private static final long serialVersionUID = 7046652044246110519L;

    @UtopiaSqlColumn private Long clazzId;                          // 动态发生在哪个班级
    @UtopiaSqlColumn private Long relevantUserId;                   // 动态发生在哪个人身上
    @UtopiaSqlColumn private UserType relevantUserType;             // 动态相关人的类型
    @UtopiaSqlColumn private ClazzJournalType journalType;          // 动态类型
    @UtopiaSqlColumn private ClazzJournalCategory journalCategory;  // 动态分类
    @UtopiaSqlColumn private String journalJson;                    // 存储新鲜事内容中的变量
    @UtopiaSqlColumn private Integer likeCount;                     // 赞的数量
    @UtopiaSqlColumn(name = "CLAZZ_GROUP_ID") private Long groupId; // 分组id

    public static String cacheKeyFromId(Long id) {
        return CacheKeyGenerator.generateCacheKey(ClazzJournal.class, id);
    }

    public static String cacheKeyFromClazzId(Long clazzId) {
        return CacheKeyGenerator.generateCacheKey(ClazzJournal.class, "CID", clazzId);
    }

    public static String cacheKeyFromUserId(Long userId) {
        return CacheKeyGenerator.generateCacheKey(ClazzJournal.class, "UID", userId);
    }

    public static String cacheKeyFromJournalType(ClazzJournalType journalType) {
        return CacheKeyGenerator.generateCacheKey(ClazzJournal.class, "JT",
                (journalType == null ? ClazzJournalType.UNKOWN : journalType).getId());
    }

    /**
     * Create mock instance for supporting unit tests.
     *
     * @return mock clazz journal instance
     */
    public static ClazzJournal mockInstance() {
        ClazzJournal inst = new ClazzJournal();
        inst.clazzId = 0L;
        inst.relevantUserId = 0L;
        inst.relevantUserType = UserType.ANONYMOUS;
        inst.journalType = ClazzJournalType.UNKOWN;
        return inst;
    }

    // ========================================================================
    // yes, this is also called Location
    // ========================================================================

    public ComplexID toComplexID() {
        ComplexID ID = new ComplexID();
        ID.id = SafeConverter.toLong(id);
        ID.clazzId = SafeConverter.toLong(clazzId);
        ID.groupId = SafeConverter.toLong(groupId);
        ID.type = journalType.getId();
        ID.category = (journalCategory == null ? ClazzJournalCategory.MISC : journalCategory).getId();
        return ID;
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")
    public static class ComplexID implements Serializable {
        private static final long serialVersionUID = -2505629714105077252L;

        private long id;
        private long clazzId;
        private long groupId;
        private int type;
        private int category;

        @Override
        public String toString() {
            // don't change the sequence
            return id + "." + clazzId + "." + groupId + "." + type + "." + category;
        }

        public static ComplexID parse(String s) {
            String[] segments = StringUtils.split(s, ".");
            ComplexID ID = new ComplexID();
            ID.id = SafeConverter.toLong(segments[0]);
            ID.clazzId = SafeConverter.toLong(segments[2]);
            ID.groupId = SafeConverter.toLong(segments[3]);
            ID.type = SafeConverter.toInt(segments[4]);
            ID.category = SafeConverter.toInt(segments[5]);
            return ID;
        }
    }
}
