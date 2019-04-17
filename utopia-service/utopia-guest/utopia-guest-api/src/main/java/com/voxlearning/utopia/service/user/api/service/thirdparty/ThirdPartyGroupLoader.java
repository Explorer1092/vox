/*
 *
 *  * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *  *
 *  * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *  *
 *  * NOTICE: All information contained herein is, and remains the property of
 *  * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 *  * and technical concepts contained herein are proprietary to Shanghai Sunny
 *  * Education, Inc. and its suppliers and may be covered by patents, patents
 *  * in process, and are protected by trade secret or copyright law. Dissemination
 *  * of this information or reproduction of this material is strictly forbidden
 *  * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 *
 */

package com.voxlearning.utopia.service.user.api.service.thirdparty;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.user.api.constants.ThirdPartyGroupType;
import com.voxlearning.utopia.service.user.api.entities.third.ThirdPartyGroup;
import com.voxlearning.utopia.service.user.api.mappers.third.ThirdPartyGroupMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author xuesong.zhang
 * @author changyuan.liu
 * @since 2016/9/27
 */
@ServiceVersion(version = "20160927")
@ServiceTimeout(timeout = 3, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ThirdPartyGroupLoader extends IPingable {

    /**
     * 获取组信息
     *
     * @param groupIds 组ids
     * @return Map
     */
    Map<Long, ThirdPartyGroup> loadThirdPartyGroupsIncludeDisabled(Collection<Long> groupIds);

    /**
     * 获取老师分组
     *
     * @param teacherId 老师id
     * @param groupType 组类型
     * @return List
     */
    default List<ThirdPartyGroupMapper> loadTeacherGroups(Long teacherId, ThirdPartyGroupType groupType) {
        return loadTeacherGroups(Collections.singleton(teacherId), groupType).getOrDefault(teacherId, Collections.emptyList());
    }

    /**
     * 获取老师分组
     *
     * @param teacherIds 老师ids
     * @return Map
     */
    Map<Long, List<ThirdPartyGroupMapper>> loadTeacherGroups(Collection<Long> teacherIds, ThirdPartyGroupType groupType);

    /**
     * 获取学生分组
     *
     * @param studentId 学生id
     * @param groupType 组类型
     * @return List
     */
    default List<ThirdPartyGroupMapper> loadStudentGroups(Long studentId, ThirdPartyGroupType groupType) {
        return loadStudentGroups(Collections.singleton(studentId), groupType).getOrDefault(studentId, Collections.emptyList());
    }

    /**
     * 获取学生分组
     *
     * @param studentIds 学生ids
     * @return Map
     */
    Map<Long, List<ThirdPartyGroupMapper>> loadStudentGroups(Collection<Long> studentIds, ThirdPartyGroupType groupType);

    /**
     * 获取分组学生id
     *
     * @param groupId
     * @return
     */
    default List<Long> loadGroupStudentIds(Long groupId) {
        if (groupId == null) {
            return Collections.emptyList();
        }
        return loadGroupStudentIds(Collections.singleton(groupId)).getOrDefault(groupId, Collections.emptyList());
    }

    Map<Long, List<Long>> loadGroupStudentIds(Collection<Long> groupIds);
}
