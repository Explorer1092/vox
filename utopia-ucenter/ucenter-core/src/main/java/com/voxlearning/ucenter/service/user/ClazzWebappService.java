/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.ucenter.service.user;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xinxin
 * @since 11/12/2015.
 */
@Named
public class ClazzWebappService {

    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private DeprecatedGroupLoaderClient deprecatedGroupLoaderClient;
    @Inject private RaikouSDK raikouSDK;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserLoginServiceClient userLoginServiceClient;

    /**
     * 根据老师查询其班级列表
     *
     * @param teacher
     * @return
     */
    public MapMessage getClazzListByTeacher(User teacher) {
        if (null == teacher || teacher.isDisabledTrue()) {
            return MapMessage.errorMessage("老师帐号错误");
        }

        List<Map<String, Object>> list = getClazzList(teacher);
        if (CollectionUtils.isEmpty(list)) {
            return MapMessage.errorMessage("此老师还没有创建班级");
        }

        return MapMessage.successMessage().add("clazzList", list).add("teacher", teacher);
    }

    private List<Map<String, Object>> getClazzList(User teacher) {
        assert teacher != null;

        Long teacherId = teacher.getId();

        // 包班制支持，读取老师的所有班级
        Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        // 读取老师班级
        Map<Long, List<Clazz>> teacherClazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(relTeacherIds);
        // 返回结果
        List<Map<String, Object>> list = new ArrayList<>();
        // 记录clazz是否处理过了
        Set<Long> handledClazzIds = new HashSet<>();

        for (Map.Entry<Long, List<Clazz>> e : teacherClazzs.entrySet()) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
            List<Clazz> clazzs = e.getValue();
            for (Clazz c : clazzs) {
                //中学没有这个限制
                if (!(teacherDetail.isJuniorTeacher() || teacherDetail.isSeniorTeacher())) {
                    if (!c.isPublicClazz()) {
                        continue;
                    }
                }
                if (c.isTerminalClazz()) {
                    continue;
                }
                Long cid = c.getId();
                if (handledClazzIds.contains(cid)) {// 已处理过，跳过
                    continue;
                }
                handledClazzIds.add(cid);

                Map<String, Object> clazzMap = new HashMap<>();
                clazzMap.put("clazzName", c.formalizeClazzName());
                clazzMap.put("clazzId", c.getId());
                if (c.getCreateBy() != null) {
                    clazzMap.put("creatorType", c.getCreateBy().name());
                }
                if (MapUtils.isNotEmpty(clazzMap)) {
                    list.add(clazzMap);
                }
            }
        }
        return list;
    }

    /**
     * 检查是否允许学生自由加入班级
     *
     * @param clazzId
     * @param teacherId
     * @return
     */
    public MapMessage checkFreeJoinClazz(Long clazzId, Long teacherId) {
        if (Objects.isNull(clazzId) || Objects.isNull(teacherId)) {
            return MapMessage.errorMessage("参数错误");
        }

        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        boolean flag = false;// 标志是否允许学生加入
        if (clazz != null) {
            GroupMapper group = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacherId, clazzId, false);
            flag = null == group ? false : group.getFreeJoin();
        }

        if (!flag) {
            return MapMessage.errorMessage().add("clazzId", "老师不允许新学生加入此班级，有问题请与老师联系。"); //FIXME 这里怎么用了clazzId?
        }
        return MapMessage.successMessage();
    }

    /**
     * 查找班级里的重名帐号,优先返回未登录过的帐号
     *
     * @param clazzId
     * @param realName
     * @return
     */
    public Optional<User> findDuplicatedName(Long clazzId, String realName) {
        if (Objects.isNull(clazzId) || StringUtils.isBlank(realName)) {
            throw new UtopiaRuntimeException("Invalid parameters[clazzId:" + clazzId + ",realName:" + realName + "]");
        }

        List<Long> userIds = asyncGroupServiceClient.getAsyncGroupService()
                .findStudentIdsByClazzId(clazzId);
        if (CollectionUtils.isNotEmpty(userIds)) {
            List<User> sameNameList = userLoaderClient.loadUsers(userIds).values()
                    .stream()
                    .filter(s -> StringUtils.equals(s.fetchRealname(), realName.trim()))
                    .sorted((o1, o2) -> Long.compare(o2.getCreateTime().getTime(), o1.getCreateTime().getTime()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sameNameList)) {
                // 找到第一个未登录的重名学生
                User user = userLoginServiceClient.getUserLoginService().findSameNameNeverLoginUser(sameNameList).getUninterruptibly();
                if (user != null) {
                    return Optional.of(user);
                } else {
                    return Optional.of(MiscUtils.firstElement(sameNameList));
                }
            }
        }
        return Optional.empty();
    }
}
