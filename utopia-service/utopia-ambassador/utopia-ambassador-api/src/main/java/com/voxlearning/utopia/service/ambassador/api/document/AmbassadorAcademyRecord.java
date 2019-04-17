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
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 大使学院答题记录
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 * @serial
 * @see com.voxlearning.utopia.api.constant.AmbassadorRecordType
 * @since May 6, 2015
 */
@Getter
@Setter
@DocumentTable(table = "VOX_AMBASSADOR_ACADEMY_RECORD")
@UtopiaCacheExpiration(3600)
@UtopiaCacheRevision("20160804")
public class AmbassadorAcademyRecord extends AbstractDatabaseEntity implements CacheDimensionDocument {
    private static final long serialVersionUID = 1390096983193947374L;

    private Long ambassadorId;
    private Integer recordType;
    private Boolean disabled;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("ambassadorId", ambassadorId)
        };
    }
}