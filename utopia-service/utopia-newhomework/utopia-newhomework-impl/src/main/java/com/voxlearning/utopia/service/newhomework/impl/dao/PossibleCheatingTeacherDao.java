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
import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.homework.api.constant.CheatingTeacherStatus;
import com.voxlearning.utopia.service.newhomework.api.entity.PossibleCheatingTeacher;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Summer Yang on 2015/7/9.
 */
@Named
@UtopiaCacheSupport(PossibleCheatingTeacher.class)
public class PossibleCheatingTeacherDao extends StaticMongoDao<PossibleCheatingTeacher, String> {
    @Override
    protected void calculateCacheDimensions(PossibleCheatingTeacher source, Collection<String> collection) {
        collection.add(PossibleCheatingTeacher.cacheKeyFromTeacherId(source.getTeacherId()));
    }

    @Override
    protected void preprocessEntity(PossibleCheatingTeacher entity) {
        super.preprocessEntity(entity);
        if (entity.getDisabled() == null) {
            entity.setDisabled(Boolean.FALSE);
        }
    }

    @UtopiaCacheable
    public PossibleCheatingTeacher loadByTeacherId(@UtopiaCacheKey(name = "teacherId") Long teacherId) {
        Find find = Find.find(filterBuilder.where("teacherId").is(teacherId)
                .and("disabled").is(false));
        return __find_OTF(find, ReadPreference.primary()).stream().findFirst().orElse(null);
    }

    public void updateLastCheatDateAndStatus(String id, Date lastCheatDate, CheatingTeacherStatus status) {
        Update update = updateBuilder.build().set("lastCheatDate", lastCheatDate)
                .set("status", status);
        update(id, update);
    }

    public void disabledById(String id) {
        Update update = updateBuilder.build().set("disabled", Boolean.TRUE);
        update(id, update);
    }

    //任务使用 获取所有不是可洗白状态的作弊老师
    public List<PossibleCheatingTeacher> loadAllBlackTeachers() {
        Find find = Find.find(filterBuilder.where("status").ne(CheatingTeacherStatus.WHITE.name())
                .and("disabled").is(false));
        return __find_OTF(find, ReadPreference.primary());
    }

    public List<PossibleCheatingTeacher> loadBlackStatusTeachers() {
        Find find = Find.find(filterBuilder.where("status").is(CheatingTeacherStatus.BLACK.name())
                .and("disabled").is(false));
        return __find_OTF(find, ReadPreference.primary());
    }

    //任务使用
    public void washTeacher(String id) {
        Update update = updateBuilder.build().set("status", CheatingTeacherStatus.WHITE);
        update(id, update);
    }

    public void updateStatus(String id, CheatingTeacherStatus status) {
        Update update = updateBuilder.build().set("status", status);
        update(id, update);
    }


    public List<PossibleCheatingTeacher> loadByDateRangeAndStatus(DateRange range, CheatingTeacherStatus status) {
        if (range == null) {
            return Collections.emptyList();
        }
        Find find = Find.find(filterBuilder.where("status").is(status.name())
                .and("createDatetime").gte(range.getStartDate()).lte(range.getEndDate())
                .and("disabled").is(false))
                .with(new Sort(Sort.Direction.DESC, "createDatetime"));
        return __find_OTF(find, ReadPreference.primary());
    }

    public List<PossibleCheatingTeacher> loadTeacherIdFieldWhereUpdateGteAndLt(Date start, Date end) {
        Find find = Find.find(filterBuilder.where("disabled").is(false)
                .and("updateDatetime").gte(start).lt(end));
        find.field().includes("teacherId");
        return __find_OTF(find, ReadPreference.primary());
    }

    // 谨慎使用 查全表
    public List<PossibleCheatingTeacher> loadAllCheatingTeacher() {
        Find find = Find.find(filterBuilder.where("disabled").is(false));
        return __find_OTF(find, ReadPreference.primary());
    }
}
