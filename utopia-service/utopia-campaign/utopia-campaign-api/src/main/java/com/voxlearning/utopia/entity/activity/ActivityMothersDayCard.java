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

package com.voxlearning.utopia.entity.activity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.common.PrimaryKeyAccessor;
import com.voxlearning.alps.spi.common.TimestampAccessor;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author RuiBao
 * @version 0.1
 * @since 4/24/2015
 */
@Getter
@Setter
@DocumentConnection(configName = "main")
@DocumentTable(table = "VOX_ACTIVITY_MOTHERS_DAY_CARD")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class ActivityMothersDayCard implements Serializable, TimestampTouchable, PrimaryKeyAccessor<Long>, TimestampAccessor {
    private static final long serialVersionUID = 6430854824259784285L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    @DocumentField("STUDENT_ID")
    protected Long studentId;

    @DocumentCreateTimestamp
    @DocumentField("CREATE_DATETIME")
    protected Date createDatetime;

    @DocumentUpdateTimestamp
    @DocumentField("UPDATE_DATETIME")
    protected Date updateDatetime;

    @DocumentField("IMAGE")
    private String image;

    @DocumentField("VOICE")
    private String voice;

    @DocumentField("SENDED")
    private Boolean sended;

    @DocumentField("SHARED")
    private Boolean shared;

    @Override
    public Long getId() {
        return getStudentId();
    }

    @Override
    public void setId(Long id) {
        setStudentId(id);
    }

    @Override
    public long fetchCreateTimestamp() {
        return createDatetime == null ? 0L : createDatetime.getTime();
    }

    @Override
    public long fetchUpdateTimestamp() {
        return updateDatetime == null ? 0L : updateDatetime.getTime();
    }

    @Override
    public void touchCreateTime(long timestamp) {
        if (createDatetime == null) {
            createDatetime = new Date(timestamp);
        }
    }

    @Override
    public void touchUpdateTime(long timestamp) {
        updateDatetime = new Date(timestamp);
    }

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(ActivityMothersDayCard.class, id);
    }
}
