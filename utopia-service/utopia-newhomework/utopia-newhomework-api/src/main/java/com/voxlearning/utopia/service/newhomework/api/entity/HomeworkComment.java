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
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Homework comment data structure.
 *
 * @author Xiaohai Zhang
 * @author Xiaopeng Yang
 * @serial
 * @since 2013-07-11 10:53
 */
@Getter
@Setter
@DocumentConnection(configName = "homework")
@DocumentTable(table = "VOX_HOMEWORK_COMMENT")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160804")
public class HomeworkComment extends AbstractDatabaseEntityWithDisabledField implements CacheDimensionDocument {
    private static final long serialVersionUID = 6534662510446109469L;

    @DocumentField("TEACHER_ID")
    private Long teacherId;
    @DocumentField("STUDENT_ID")
    private Long studentId;
    @DocumentField("COMMENT")
    private String comment;
    @DocumentField("HOMEWORK_ID")
    private String homeworkId;
    @DocumentField("HOMEWORK_TYPE")
    private String homeworkType;
    @DocumentField("READED")
    private Boolean readed;
    @DocumentField("REWARD_INTEGRAL")
    private Integer rewardIntegral;                 //奖励学豆数量

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id),
                newCacheKey("H", homeworkId)
        };
    }

    public Location toLocation() {
        Location location = new Location();
        location.id = id;
        location.disabled = Boolean.TRUE.equals(getDisabled());
        location.createTime = fetchCreateTimestamp();
        location.studentId = (studentId == null ? 0 : studentId);
        location.homeworkType = homeworkType;
        location.homeworkId = homeworkId;
        return location;
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")
    @ToString
    public static class Location implements Serializable {
        private static final long serialVersionUID = -2665615073665999612L;

        private Long id;
        private boolean disabled;
        private long createTime;
        private long studentId;
        private String homeworkType;
        private String homeworkId;
    }
}
