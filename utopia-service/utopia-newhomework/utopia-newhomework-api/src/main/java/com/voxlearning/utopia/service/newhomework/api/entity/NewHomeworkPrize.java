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
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 作业奖励表
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newhomework")
@DocumentCollection(collection = "homeworkPrize_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161104")
public class NewHomeworkPrize implements Serializable {

    private static final long serialVersionUID = -8067244618686655993L;

    @DocumentId private String id;
    @DocumentCreateTimestamp private Date createAt;
    @DocumentCreateTimestamp private Date updateAt;

    private Long teacherId;
    private Integer quantity;
    private Map<String, Detail> details;

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"when", "what", "where"})
    public static class Detail implements Serializable {

        private static final long serialVersionUID = 3800295520470242609L;
        private Date when; // 什么时候抽奖
        private Integer what; // 抽到了什么
        private String where; // 在哪里抽的
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"day", "subject", "hid"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {
        private static final long serialVersionUID = -5617498416224329778L;

        private String day;
        private Subject subject;
        private String hid;

        @Override
        public String toString() {
            return day + "." + subject + "." + hid;
        }
    }

    public ID parseID() {
        if (id == null || id.trim().length() == 0) return null;
        String[] segments = id.split("\\.");
        if (segments.length != 3) return null;
        String day = segments[0];
        Subject subject = Subject.safeParse(segments[1]);
        String hid = segments[2];
        return new ID(day, subject, hid);
    }

    // 计算还剩下多少学豆
    public int left() {
        if (details == null || details.isEmpty()) return quantity;
        return quantity - details.values().stream().mapToInt(Detail::getWhat).sum();
    }

    public static String generateCacheKey(String id) {
        return CacheKeyGenerator.generateCacheKey(NewHomeworkPrize.class, id);
    }
}
