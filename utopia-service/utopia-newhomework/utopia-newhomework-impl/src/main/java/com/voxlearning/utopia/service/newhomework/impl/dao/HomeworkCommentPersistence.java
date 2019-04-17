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

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkComment;

import javax.inject.Named;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Named
@CacheBean(type = HomeworkComment.class)
public class HomeworkCommentPersistence extends StaticCacheDimensionDocumentJdbcDao<HomeworkComment, Long> {

    @CacheMethod
    public Map<String, Set<HomeworkComment.Location>>
    queryByHomeworkIds(@CacheParameter(value = "H", multiple = true) Collection<String> homeworkIds) {
        Criteria criteria = Criteria.where("HOMEWORK_ID").in(homeworkIds);
        Query query = Query.query(criteria);
        query.field().includes("ID", "DISABLED", "CREATE_DATETIME", "STUDENT_ID", "HOMEWORK_TYPE", "HOMEWORK_ID");
        return query(query).stream()
                .map(HomeworkComment::toLocation)
                .collect(Collectors.groupingBy(HomeworkComment.Location::getHomeworkId, Collectors.toSet()));
    }
}
