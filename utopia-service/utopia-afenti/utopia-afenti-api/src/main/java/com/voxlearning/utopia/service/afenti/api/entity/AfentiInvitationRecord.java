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
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 邀请记录表
 *
 * @author peng.zhang.a
 * @since 16-7-19
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_afenti")
@DocumentTable(table = "VOX_AFENTI_INVITATION_RECORD")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160722")
public class AfentiInvitationRecord extends AbstractDatabaseEntity {

    private static final long serialVersionUID = -4038501939341953576L;
    @UtopiaSqlColumn private Long userId;
    @UtopiaSqlColumn private Long invitedUserId;
    @UtopiaSqlColumn private Boolean accepted;
    @UtopiaSqlColumn private Date acceptTime;
    @UtopiaSqlColumn private Subject subject;

    public static AfentiInvitationRecord newInstence(Long userId, Long classmateUserId, Subject subject) {
        AfentiInvitationRecord afentiInvitationRecord = new AfentiInvitationRecord();
        afentiInvitationRecord.setUserId(userId);
        afentiInvitationRecord.setSubject(subject);
        afentiInvitationRecord.setInvitedUserId(classmateUserId);
        afentiInvitationRecord.setAccepted(false);
        return afentiInvitationRecord;
    }

    public static String generateKeyByUserIdAndSubject(Long userId, Subject subject) {
        return CacheKeyGenerator.generateCacheKey(AfentiInvitationRecord.class,
                new String[]{"UID", "SJ"},
                new Object[]{userId, subject});
    }

    public static String generateKeyByInvitedUserIdAndSubject(Long invitedUserId, Subject subject) {
        return CacheKeyGenerator.generateCacheKey(AfentiInvitationRecord.class,
                new String[]{"IUID", "SJ"},
                new Object[]{invitedUserId, subject});
    }
}