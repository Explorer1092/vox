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

package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiInvitationRecord;

import javax.inject.Named;
import java.util.*;

/**
 * @author peng.zhang.a
 * @since 16-7-19
 */
@Named
@UtopiaCacheSupport(value = AfentiInvitationRecord.class)
@CacheDimension(CacheDimensionDistribution.OTHER_FIELDS)
public class AfentiInvitationRecordPersistence extends StaticPersistence<Long, AfentiInvitationRecord> {

    @Override
    protected void calculateCacheDimensions(AfentiInvitationRecord source, Collection<String> dimensions) {
        dimensions.add(AfentiInvitationRecord.generateKeyByUserIdAndSubject(source.getUserId(), source.getSubject()));
        dimensions.add(AfentiInvitationRecord.generateKeyByInvitedUserIdAndSubject(source.getInvitedUserId(), source.getSubject()));
    }

    @UtopiaCacheable
    public List<AfentiInvitationRecord> findByUserIdAndSubject(@UtopiaCacheKey(name = "UID") Long userId,
                                                               @UtopiaCacheKey(name = "SJ") Subject subject) {
        return withSelectFromTable("WHERE USER_ID=? AND SUBJECT=? ORDER BY CREATE_DATETIME DESC")
                .useParamsArgs(userId, subject).queryAll();
    }

    @UtopiaCacheable
    public List<AfentiInvitationRecord> findByInvitedUserIdAndSubject(@UtopiaCacheKey(name = "IUID") Long inviteUserId,
                                                                      @UtopiaCacheKey(name = "SJ") Subject subject) {
        return withSelectFromTable("WHERE INVITED_USER_ID=? AND SUBJECT=? ORDER BY CREATE_DATETIME DESC")
                .useParamsArgs(inviteUserId, subject).queryAll();
    }

    public int updateAccepted(List<AfentiInvitationRecord> records) {
        if (CollectionUtils.isEmpty(records)) return 0;

        Set<String> cacheKeys = new LinkedHashSet<>();
        List<Long> ids = new LinkedList<>();
        records.forEach(e -> {
            ids.add(e.getId());
            calculateCacheDimensions(e, cacheKeys);
        });
        Date date = new Date();
        String sql = "SET ACCEPTED=TRUE,ACCEPT_TIME=:date,UPDATE_DATETIME=:date WHERE ID IN (:ids) ";
        int rows = withUpdateTable(sql).useParams(MiscUtils.m("ids", ids, "date", date)).executeUpdate();
        if (rows > 0) {
            getCache().delete(cacheKeys);
        }
        return rows;
    }
}
