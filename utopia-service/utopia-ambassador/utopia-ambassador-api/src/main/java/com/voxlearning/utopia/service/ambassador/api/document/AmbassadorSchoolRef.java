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

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

/**
 * Reference between ambassador and school.
 *
 * @author Summer Yang
 * @author Xiaohai Zhang
 * @serial
 * @since Jun 15, 2015
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
@DocumentTable(table = "VOX_AMBASSADOR_SCHOOL_REF")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160804")
public class AmbassadorSchoolRef extends AbstractDatabaseEntityWithDisabledField implements CacheDimensionDocument {
    private static final long serialVersionUID = -3789068058256364724L;

    private Long ambassadorId;
    private Long schoolId;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("ambassadorId", ambassadorId),
                newCacheKey("schoolId", schoolId),
                // this is a hack cache key for boosting teacher loader
                // If you want to change this, please refer to TeacherLoader
                CacheKeyGenerator.generateCacheKey("TeacherLoader.AmbassadorSchoolRef",
                        new String[]{"ambassadorId"}, new Object[]{ambassadorId})
        };
    }
}
