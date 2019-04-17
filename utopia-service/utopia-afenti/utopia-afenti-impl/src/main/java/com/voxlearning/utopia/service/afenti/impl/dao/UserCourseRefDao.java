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

package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.afenti.api.entity.UserCourseRef;

import javax.inject.Named;
import java.util.Collection;

/**
 * 用户当前学期默认视频课程
 *
 * @author liu jingchao
 * @since 2017/03/27
 */
@Named
@UtopiaCacheSupport(UserCourseRef.class)
public class UserCourseRefDao extends AlpsStaticJdbcDao<UserCourseRef, Long> {

    @Override
    protected void calculateCacheDimensions(UserCourseRef source, Collection<String> dimensions) {
        dimensions.add(UserCourseRef.cacheKeyFromUserIdAndSubject(source.getUserId(), source.getSubject()));
    }

    /**
     * 新增更新用户当前学期默认视频课程
     *
     * @param studentId 学生ID
     * @param subject   学科
     * @param courseId  课程ID
     * @return List
     */
    public void upsertUserCourse(Long studentId, Subject subject, String courseId) {
        if (studentId == null || subject == null || StringUtils.isBlank(courseId)) {
            return;
        }
        try {
            UserCourseRef userCourseRef = findCourseByUserIdAndSubject(studentId, subject);
            if (userCourseRef == null) {
                insert(UserCourseRef.newInstance(studentId, subject, courseId));
            } else {
                userCourseRef.setCourseId(courseId);
                replace(userCourseRef);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 查询学生相应学科使用教材
     *
     * @param studentId 学生ID
     * @param subject   学科
     * @return UserCourseRef
     */
    @CacheMethod
    public UserCourseRef findCourseByUserIdAndSubject(@CacheParameter(value = "userId") Long studentId,
                                                      @CacheParameter(value = "subject") Subject subject) {
        if (studentId == null || subject == null) {
            return null;
        }
        Criteria criteria = Criteria.where("USER_ID").is(studentId)
                .and("SUBJECT").is(subject)
                .and("DISABLED").is(false);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }
}
