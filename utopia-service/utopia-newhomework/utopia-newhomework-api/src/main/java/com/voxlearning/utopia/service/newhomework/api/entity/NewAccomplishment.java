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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.calendar.MonthRange;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * 以下注释是老的逻辑，主键生成规则发生变化
 * <p>
 * 学生作业、假期作业、假期作业包、测验等完成结果的数据结构
 * 将取代原有的所有作业结果相关的数据结构。
 * 这个数据结构将以作业、测验类型+ID组成唯一的ID。
 * 每个学生是其下的一个分项。
 * 按照作业、测验所在的月份为单位进行分库吧。
 * 说明：直接上分库，这样对于运维有好处。但是目前没有框架代码，先这里手工实现。以后再移植出去。
 * 主键格式说明：
 * a-b-c
 * a = yyyyMM (homework create time)
 * b =
 * c = homework id
 * </p>
 * <p>
 * 主键格式：month-subject-homeworkId
 * 其中month是对应作业的创建月份
 * subject是作业的学科（这是一份冗余信息）
 * homeworkId是作业ID
 * </p>
 */
@Getter
@Setter
@EqualsAndHashCode
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newhomework")
@DocumentCollection(collection = "accomplishment_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161215")
public class NewAccomplishment implements Serializable {
    private static final long serialVersionUID = -2603022963802946270L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE) private String id;
    private Map<String, Detail> details;    // 键值是学生ID的字符串形式

    /**
     * 每个学生完成此作业的详情数据结构
     */
    @Getter
    @Setter
    @EqualsAndHashCode
    public static class Detail implements Serializable {
        private static final long serialVersionUID = 1880015576986738469L;

        private Date accomplishTime;            // 完成作业、测验的时间
        private String ip;                      // 完成作业时传上来的IP地址
        private Boolean repair;                 // TRUE表示是补做作业，目前此属性预留
        private String clientType;              // 先定义成string了，这个type预计要有改动。
        private String clientName;

        @JsonIgnore
        public boolean isRepairTrue() {
            return Boolean.TRUE.equals(repair);
        }
    }

    public ID parseID() {
        if (id == null) return null;
        String[] segments = id.split("-");
        if (segments.length != 3) return null;
        String month = segments[0];
        Subject subject = Subject.valueOf(segments[1]);
        String hid = segments[2];
        return new ID(month, subject, hid);
    }

    public int size() {
        return getDetails() == null ? 0 : getDetails().size();
    }

    public boolean contains(Long studentId) {
        return studentId != null && getDetails() != null && getDetails().containsKey(studentId.toString());
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {
        private static final long serialVersionUID = -8243953908940862577L;

        private String month;
        private Subject subject;
        private String hid;

        /**
         * Generate primary key string, don't change this method
         *
         * @return primary key string
         */
        @Override
        public String toString() {
            return month + "-" + subject + "-" + hid;
        }

        public static ID build(Date homeworkCreateTime, Subject subject, String homeworkId) {
            Objects.requireNonNull(homeworkCreateTime);
            return build(homeworkCreateTime.getTime(), subject, homeworkId);
        }

        public static ID build(long homeworkCreateTime, Subject subject, String homeworkId) {
            Objects.requireNonNull(subject);
            Objects.requireNonNull(homeworkId);
            MonthRange month = MonthRange.newInstance(homeworkCreateTime);
            return new ID(month.toString(), subject, homeworkId);
        }
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(NewAccomplishment.class, id);
    }
}
