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

package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.dao.AlpsDynamicMongoDao;
import com.voxlearning.alps.lang.support.RangeableId;
import com.voxlearning.alps.lang.support.RangeableIdVersion;
import com.voxlearning.utopia.service.newhomework.api.entity.MiddleSchoolHomeworkDoQuestion;

import javax.inject.Named;
import java.util.Collection;
import java.util.Objects;

@Named
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class MiddleSchoolHomeworkDoQuestionDao extends AlpsDynamicMongoDao<MiddleSchoolHomeworkDoQuestion, String> {

    @Override
    protected void calculateCacheDimensions(MiddleSchoolHomeworkDoQuestion source, Collection<String> dimensions) {
        dimensions.add(MiddleSchoolHomeworkDoQuestion.ck_id(source.getId()));
    }

    @Override
    protected String calculateDatabase(String template, MiddleSchoolHomeworkDoQuestion entity) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, MiddleSchoolHomeworkDoQuestion entity) {
        RangeableId rangeableId = RangeableIdVersion.V1.parse(entity.getId());
        Objects.requireNonNull(rangeableId);
        return StringUtils.formatMessage(template, rangeableId.range(DateRangeType.D).toString());
    }
}
