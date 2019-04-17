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

package com.voxlearning.utopia.entity.mission;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.MissionState;
import com.voxlearning.utopia.api.constant.MissionType;
import com.voxlearning.utopia.api.constant.WishType;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @author RuiBao
 * @version 0.1
 * @since 1/8/2015
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
@DocumentTable(table = "VOX_MISSION")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20151015")
public class Mission extends AbstractDatabaseEntity {
    private static final long serialVersionUID = 2958522320303368046L;

    @DocumentField @UtopiaSqlColumn private Long studentId;
    @DocumentField @UtopiaSqlColumn private Integer integral;
    @DocumentField @UtopiaSqlColumn private String wish;
    @DocumentField @UtopiaSqlColumn private WishType wishType;
    @DocumentField @UtopiaSqlColumn private String mission;
    @DocumentField @UtopiaSqlColumn private MissionType missionType;
    @DocumentField @UtopiaSqlColumn private Integer totalCount;
    @DocumentField @UtopiaSqlColumn private Integer finishCount;
    @DocumentField @UtopiaSqlColumn private MissionState missionState;
    @DocumentField @UtopiaSqlColumn private String img;
    @DocumentField @UtopiaSqlColumn private Date missionDatetime;
    @DocumentField @UtopiaSqlColumn private Date completeDatetime;

    public String formalizeMissionContent() {
        return mission;
    }

    public String formalizeWishContent() {
        return wishType == WishType.INTEGRAL ? integral + "学豆" : wish;
    }

    // ========================================================================
    // Cache support
    // ========================================================================

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(Mission.class, id);
    }

    public static String ck_studentId(Long studentId) {
        return CacheKeyGenerator.generateCacheKey(Mission.class, "S", studentId);
    }

    // ========================================================================
    // Mission location
    // ========================================================================

    public Location toLocation() {
        Location location = new Location();
        location.id = id;
        location.createTime = fetchCreateTimestamp();
        location.studentId = SafeConverter.toLong(studentId);
        location.state = missionState;
        location.missionTime = SafeConverter.toLong(missionDatetime);
        return location;
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")
    @ToString
    public static class Location implements Serializable {
        private static final long serialVersionUID = -3968935077127588343L;

        private Long id;
        private long createTime;
        private long studentId;
        private MissionState state;
        private long missionTime;
    }
}
