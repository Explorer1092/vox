/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.nekketsu.impl.dao;

import com.mongodb.WriteConcern;
import com.voxlearning.alps.dao.mongo.dao.RangeableMongoDao;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.ParkourPlayLog;

import javax.inject.Named;
import java.util.Collection;

/**
 * Dao implementation of entity {@link ParkourPlayLog}.
 *
 * @author Xiaohai Zhang
 * @since Jan 6, 2015
 */
@Named
public class ParkourPlayLogDao extends RangeableMongoDao<ParkourPlayLog> {
    @Override
    protected void calculateCacheDimensions(ParkourPlayLog source, Collection<String> dimensions) {
    }

    @Override
    protected WriteConcern writeConcernForInsert() {
        return WriteConcern.UNACKNOWLEDGED;
    }
}
