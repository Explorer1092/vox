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

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.user.api.constants.ThirdPartyGroupType;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * @author xuesong.zhang
 * @since 2016/9/26
 */
@ServiceVersion(version = "20160926")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ThirdPartyGroupService extends IPingable {

    /**
     * 创建老师关联分组
     *
     * @param userId    用户id
     * @param subject   学科
     * @param groupType 第三方组类型
     * @return Long 组id
     */
    MapMessage createTeacherGroup(Long userId, Subject subject, ThirdPartyGroupType groupType);

    /**
     * 创建分组
     *
     * @param subject
     * @param groupType
     * @return
     */
    MapMessage createGroup(Subject subject, ThirdPartyGroupType groupType);

    /**
     * 给组里添加学生
     *
     * @param studentId 学生id
     * @param groupId   组id
     * @return MapMessage
     */
    MapMessage addStudentToGroup(Long studentId, Long groupId);

    /**
     * 更换分组老师
     *
     * @param groupId
     * @param teacherId
     * @return
     */
    MapMessage changeGroupTeacher(Long groupId, Long teacherId);

    /**
     * 移除分组学生
     *
     * @param groupId
     * @param studentIds
     * @return
     */
    MapMessage removeGroupStudents(Long groupId, Collection<Long> studentIds);

    /**
     * 修改组名
     *
     * @param groupId
     * @param groupName
     * @return
     */
    MapMessage changeGroupName(Long groupId, String groupName);
}
