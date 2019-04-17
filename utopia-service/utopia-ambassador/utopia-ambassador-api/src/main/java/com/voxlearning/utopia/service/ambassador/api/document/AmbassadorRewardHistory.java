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

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by XiaoPeng.Yang on 15-2-12.
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
@DocumentTable(table = "VOX_AMBASSADOR_REWARD_HISTORY")
public class AmbassadorRewardHistory extends AbstractDatabaseEntity implements CacheDimensionDocument {
    private static final long serialVersionUID = -8596101746818837348L;

    private Long teacherId;
    private Long schoolId;
    private String month;
    private Integer amount;
    private Integer status;

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }
}