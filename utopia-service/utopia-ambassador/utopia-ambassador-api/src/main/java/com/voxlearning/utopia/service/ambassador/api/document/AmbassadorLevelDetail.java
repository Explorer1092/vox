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

package com.voxlearning.utopia.service.ambassador.api.document;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.api.constant.AmbassadorLevel;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Created by XiaoPeng.Yang on 15-2-12.
 */
@Getter
@Setter
@DocumentTable(table = "VOX_AMBASSADOR_LEVEL_DETAIL")
@NoArgsConstructor
@UtopiaCacheExpiration
@UtopiaCacheRevision("20160806")
public class AmbassadorLevelDetail extends AbstractDatabaseEntity implements CacheDimensionDocument {
    private static final long serialVersionUID = 8240862744298681343L;

    private Long ambassadorId;
    private Long schoolId;
    private Integer inviteCount;    // 字段已经无用
    private Integer activateCount;  // 字段已经无用
    private AmbassadorLevel level;
    private Date bornDate;          // 成为铜牌大使的时间
    private Boolean isObservation;  // 是否观察期
    private Boolean disabled;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"ambassadorId"}, new Object[]{ambassadorId}, new Object[]{0L})
        };
    }
}
