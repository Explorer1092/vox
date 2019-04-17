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

package com.voxlearning.utopia.service.guest.impl.service.thirdparty;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.guest.impl.dao.ThirdPartyGroupStudentRefPersistence;
import com.voxlearning.utopia.service.guest.impl.dao.ThirdPartyGroupTeacherRefPersistence;
import com.voxlearning.utopia.service.user.api.constants.ThirdPartyGroupType;
import com.voxlearning.utopia.service.user.api.entities.third.ThirdPartyGroup;
import com.voxlearning.utopia.service.user.api.entities.third.ThirdPartyGroupStudentRef;
import com.voxlearning.utopia.service.user.api.entities.third.ThirdPartyGroupTeacherRef;
import com.voxlearning.utopia.service.user.api.mappers.third.ThirdPartyGroupMapper;
import com.voxlearning.utopia.service.user.api.service.thirdparty.ThirdPartyGroupLoader;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2016/9/27
 */
@Named
@Service(interfaceClass = ThirdPartyGroupLoader.class)
@ExposeService(interfaceClass = ThirdPartyGroupLoader.class)
public class ThirdPartyGroupLoaderImpl extends SpringContainerSupport implements ThirdPartyGroupLoader {

    @Inject private RaikouSDK raikouSDK;
    @Inject private ThirdPartyGroupTeacherRefPersistence thirdPartyGroupTeacherRefPersistence;
    @Inject private ThirdPartyGroupStudentRefPersistence thirdPartyGroupStudentRefPersistence;

    @Override
    public Map<Long, ThirdPartyGroup> loadThirdPartyGroupsIncludeDisabled(Collection<Long> groupIds) {
        Map<Long, ThirdPartyGroup> map = new LinkedHashMap<>();
        raikouSDK.getClazzClient()
                .getThirdPartyGroupServiceClient()
                .loadGroupsIncludeDisabled(groupIds)
                .forEach(e -> map.put(e.getId(), e));
        return map;
    }

    @Override
    public Map<Long, List<ThirdPartyGroupMapper>> loadTeacherGroups(Collection<Long> teacherIds, ThirdPartyGroupType groupType) {
        if (CollectionUtils.isEmpty(teacherIds)) {
            return Collections.emptyMap();
        }
        Map<Long, List<ThirdPartyGroupTeacherRef>> refMap = thirdPartyGroupTeacherRefPersistence.findByTeacherIds(teacherIds);
        if (MapUtils.isEmpty(refMap)) {
            return Collections.emptyMap();
        }
        Set<Long> groupIds = refMap.values()
                .stream()
                .flatMap(Collection::stream)
                .map(ThirdPartyGroupTeacherRef::getGroupId)
                .collect(Collectors.toSet());

        Map<Long, ThirdPartyGroup> groupMap = loadThirdPartyGroupsIncludeDisabled(groupIds);
        if (MapUtils.isEmpty(groupMap)) {
            return Collections.emptyMap();
        }
        Map<Long, List<ThirdPartyGroupMapper>> result = new LinkedHashMap<>();
        for (Map.Entry<Long, List<ThirdPartyGroupTeacherRef>> entry : refMap.entrySet()) {
            List<ThirdPartyGroupMapper> list = entry.getValue().stream()
                    .map(ThirdPartyGroupTeacherRef::getGroupId)
                    .map(groupMap::get)
                    .filter(g -> groupType == null || g.getGroupType() == groupType)
                    .map(ThirdPartyGroupMapper::of)
                    .collect(Collectors.toList());
            result.put(entry.getKey(), list);
        }
        return result;
    }

    @Override
    public Map<Long, List<ThirdPartyGroupMapper>> loadStudentGroups(Collection<Long> studentIds, ThirdPartyGroupType groupType) {
        if (CollectionUtils.isEmpty(studentIds)) {
            return Collections.emptyMap();
        }
        Map<Long, List<ThirdPartyGroupStudentRef>> refMap = thirdPartyGroupStudentRefPersistence.findByUserIds(studentIds);
        if (MapUtils.isEmpty(refMap)) {
            return Collections.emptyMap();
        }
        Set<Long> groupIds = refMap.values()
                .stream()
                .flatMap(Collection::stream)
                .map(ThirdPartyGroupStudentRef::getGroupId)
                .collect(Collectors.toSet());

        Map<Long, ThirdPartyGroup> groupMap = loadThirdPartyGroupsIncludeDisabled(groupIds);
        if (MapUtils.isEmpty(groupMap)) {
            return Collections.emptyMap();
        }
        Map<Long, List<ThirdPartyGroupMapper>> result = new LinkedHashMap<>();
        for (Map.Entry<Long, List<ThirdPartyGroupStudentRef>> entry : refMap.entrySet()) {
            List<ThirdPartyGroupMapper> list = entry.getValue().stream()
                    .map(ThirdPartyGroupStudentRef::getGroupId)
                    .map(groupMap::get)
                    .filter(g -> groupType == null || g.getGroupType() == groupType)
                    .map(ThirdPartyGroupMapper::of)
                    .collect(Collectors.toList());
            result.put(entry.getKey(), list);
        }
        return result;
    }

    @Override
    public Map<Long, List<Long>> loadGroupStudentIds(Collection<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyMap();
        }

        Map<Long, List<ThirdPartyGroupStudentRef>> refs = thirdPartyGroupStudentRefPersistence.findByGroupIds(groupIds);

        Map<Long, List<Long>> map = new HashMap<>();
        refs.entrySet().forEach(e -> map.put(e.getKey(), e.getValue().stream().map(ThirdPartyGroupStudentRef::getStudentId).collect(Collectors.toList())));
        return map;
    }
}
