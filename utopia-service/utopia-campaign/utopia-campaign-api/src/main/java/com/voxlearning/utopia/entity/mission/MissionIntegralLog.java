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
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author RuiBao
 * @version 0.1
 * @since 1/9/2015
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
@DocumentTable(table = "VOX_MISSION_INTEGRAL_LOG")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20151015")
public class MissionIntegralLog extends AbstractDatabaseEntity {
    private static final long serialVersionUID = -4193347284142396017L;

    @DocumentField private Long studentId;
    @DocumentField private Long missionId;
    @DocumentField private String month;

    public static MissionIntegralLog newInstance(Long studentId, Long missionId, String month) {
        Objects.requireNonNull(studentId);
        Objects.requireNonNull(missionId);
        Objects.requireNonNull(month);
        MissionIntegralLog inst = new MissionIntegralLog();
        inst.setStudentId(studentId);
        inst.setMissionId(missionId);
        inst.setMonth(month);
        return inst;
    }

    // ========================================================================
    // Cache support
    // ========================================================================

    public static String ck_studentId(Long studentId) {
        return CacheKeyGenerator.generateCacheKey(MissionIntegralLog.class, "S", studentId);
    }
}
