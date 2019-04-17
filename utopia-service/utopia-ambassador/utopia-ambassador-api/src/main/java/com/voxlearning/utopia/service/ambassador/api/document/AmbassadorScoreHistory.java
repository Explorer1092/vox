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

import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.api.constant.AmbassadorCompetitionScoreType;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * Ambassador score history data structure.
 *
 * @author Summer Yang
 * @serial
 * @since Nov 9, 2015
 */
@Getter
@Setter
@DocumentTable(table = "VOX_AMBASSADOR_SCORE_HISTORY")
public class AmbassadorScoreHistory extends AbstractDatabaseEntity implements CacheDimensionDocument {
    private static final long serialVersionUID = 6081263936692015324L;

    private Long ambassadorId;
    private Long targetUserId;
    private Integer score;
    private AmbassadorCompetitionScoreType scoreType;
    private Boolean disabled;

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }
}

