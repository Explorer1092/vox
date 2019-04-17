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
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.utopia.service.business.api.entity.StudentAppInteractiveInfo;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by tanguohong on 14-4-10.
 */
@Named
public class StudentAppInteractiveInfoDao extends StaticMongoDao<StudentAppInteractiveInfo, String> {
    @Override
    protected void calculateCacheDimensions(StudentAppInteractiveInfo source, Collection<String> dimensions) {
    }

    public StudentAppInteractiveInfo findByUserIdAndUnitId(Long userId, Long bookId, Long unitId, Long lessonId,
                                                           Long practiceId) {
        Filter filter = filterBuilder.where("userId").is(userId)
                .and("bookId").is(bookId)
                .and("unitId").is(unitId)
                .and("lessonId").is(lessonId)
                .and("practiceId").is(practiceId);
        Find find = Find.find(filter).limit(1);
        return __find_OTF(find, ReadPreference.primary())
                .stream()
                .findFirst()
                .orElse(null);
    }

    public List<StudentAppInteractiveInfo> findByUserIdsAndUnitId(List<Long> userIds, Long bookId, Long unitId,
                                                                  Long lessonId, Long practiceId) {
        Filter filter = filterBuilder.where("userId").in(userIds)
                .and("bookId").is(bookId)
                .and("unitId").is(unitId)
                .and("lessonId").is(lessonId)
                .and("practiceId").is(practiceId);
        Find find = Find.find(filter);
        return __find_OTF(find, ReadPreference.primary());
    }

}
