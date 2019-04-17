/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.dao;

import com.mongodb.ReadPreference;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.business.api.entity.RSTeacherAuthStudentCountDaily;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author changyuan.liu
 * @since 2015/5/13
 */
@Named
@UtopiaCacheSupport(RSTeacherAuthStudentCountDaily.class)
public class RSTeacherAuthStudentCountDailyDao extends StaticMongoDao<RSTeacherAuthStudentCountDaily, String> {
    private static FastDateFormat dayFormat = FastDateFormat.getInstance("yyyyMMdd");

    @Override
    protected void calculateCacheDimensions(RSTeacherAuthStudentCountDaily source, Collection<String> dimensions) {
    }

    public List<RSTeacherAuthStudentCountDaily> findByTeacherIds(Collection<Long> teacherIds,
                                                                 Date startDate, Date endDate) {
        Find find = Find.find(filterBuilder.where("teaId").in(teacherIds));
        Filter dayFilter = null;
        if (startDate != null) {
            int start = SafeConverter.toInt(dayFormat.format(startDate), -1);
            if (start != -1) {
                dayFilter = filterBuilder.where("day").gte(start);
            }
        }
        if (endDate != null) {
            int end = SafeConverter.toInt(dayFormat.format(endDate), -1);
            if (end != -1) {
                if (dayFilter == null) {
                    dayFilter = filterBuilder.where("day").lte(end);
                } else {
                    dayFilter = dayFilter.lte(end);
                }
            }
        }
        if (dayFilter != null) {
            find.addFilter(dayFilter);
        }
        return __find_OTF(find, ReadPreference.primary());
    }
}
