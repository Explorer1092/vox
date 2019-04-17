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

package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.mongodb.ReadPreference;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.PossibleCheatingHomework;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author RuiBao
 * @version 0.1
 * @since 14-3-21
 */
@Named
public class PossibleCheatingHomeworkDao extends StaticMongoDao<PossibleCheatingHomework, String> {
    @Override
    protected void calculateCacheDimensions(PossibleCheatingHomework source, Collection<String> dimensions) {
    }

    public Page<PossibleCheatingHomework> pageGetByDateRange(DateRange range, Pageable pageable) {
        Filter filter = filterBuilder.where("createDatetime").gte(range.getStartDate()).lte(range.getEndDate());
        Pageable request = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(),
                new Sort(Sort.Direction.DESC, "createDatetime"));
        return __pageFind_OTF(filter.toBsonDocument(), request, ReadPreference.primary());
    }

    public List<PossibleCheatingHomework> getByDateRange(DateRange range) {
        Filter filter = filterBuilder.where("createDatetime").gte(range.getStartDate()).lte(range.getEndDate());
        Find find = Find.find(filter).with(new Sort(Sort.Direction.DESC, "createDatetime"));
        return __find_OTF(find, ReadPreference.primary());
    }

    public PossibleCheatingHomework getByTeacherIdAndHomeworkId(Long teacherId, String homeworkId, HomeworkType type) {
        if (null == teacherId || null == homeworkId) {
            return null;
        }
        Filter f1 = filterBuilder.where("teacherId").is(teacherId)
                .and("homeworkId").is(homeworkId)
                .and("homeworkType").is(type);
        Filter f2 = filterBuilder.build().orOperator(filterBuilder.where("recordOnly").is(false),
                filterBuilder.where("recordOnly").exists(false));
        Filter filter = filterBuilder.build().andOperator(f1, f2);
        Find find = Find.find(filter).limit(1);
        return __find_OTF(find, ReadPreference.primary())
                .stream()
                .findFirst()
                .orElse(null);
    }

    public List<PossibleCheatingHomework> getByTeacherIdAndHomeworkIds(Long teacherId, List<String> homeworkIds, HomeworkType type) {
        if (null == teacherId || CollectionUtils.isEmpty(homeworkIds)) {
            return Collections.emptyList();
        }
        Filter filter = filterBuilder.where("teacherId").is(teacherId)
                .and("homeworkId").in(homeworkIds)
                .and("homeworkType").is(type);
        Find find = Find.find(filter);
        return __find_OTF(find, ReadPreference.primary());
    }

    public void updatePossibleCheatingHomeworkIntegral(String id) {
        Update update = updateBuilder.update("isAddIntegral", true);
        __update_OTF(id, update);
    }
}
