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

package com.voxlearning.utopia.service.feedback.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.feedback.api.DislocationGroupService;
import com.voxlearning.utopia.service.feedback.impl.persistence.DislocationGroupPersistence;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.DislocationGroup;
import com.voxlearning.utopia.service.user.api.entities.DislocationGroupDetail;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named("com.voxlearning.utopia.service.feedback.impl.service.DislocationGroupServiceImpl")
@ExposeService(interfaceClass = DislocationGroupService.class)
public class DislocationGroupServiceImpl extends SpringContainerSupport implements DislocationGroupService {

    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private RaikouSDK raikouSDK;
    @Inject private DislocationGroupPersistence dislocationGroupPersistence;

    @Override
    public AlpsFuture<DislocationGroupDetail> loadDislocationGroupDetailByGroupId(Long groupId) {
        if (groupId == null) {
            return new ValueWrapperFuture<>(null);
        }
        DislocationGroup dislocationGroup = dislocationGroupPersistence.loadByGroupId(groupId);
        DislocationGroupDetail dislocationGroupDetail = null;
        if (dislocationGroup != null) {
            dislocationGroupDetail = new DislocationGroupDetail(dislocationGroup);
            GroupMapper groupMapper = groupLoaderClient.loadGroup(dislocationGroupDetail.getGroupId(), false);
            if (groupMapper != null) {
                Clazz clazz = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazz(groupMapper.getClazzId());
                if (clazz != null) {
                    School school = schoolLoaderClient.getSchoolLoader()
                            .loadSchool(clazz.getSchoolId())
                            .getUninterruptibly();
                    if (school != null) {
                        dislocationGroupDetail.setCurrentSchoolId(school.getId().toString());
                        dislocationGroupDetail.setCurrentSchoolName(school.getCname());
                    }
                }
            }
            School school = schoolLoaderClient.getSchoolLoader()
                    .loadSchool(dislocationGroupDetail.getRealSchoolId())
                    .getUninterruptibly();
            if (school != null) {
                dislocationGroupDetail.setRealSchoolName(school.getCname());
            }
        }
        return new ValueWrapperFuture<>(dislocationGroupDetail);
    }

    @Override
    public AlpsFuture<List<DislocationGroupDetail>> loadDislocationGroupDetailsByRealSchoolId(Long realSchoolId) {
        if (realSchoolId == null) {
            return ValueWrapperFuture.emptyList();
        }
        List<DislocationGroupDetail> DislocationGroupDetailList = getDislocationGroups(dislocationGroupPersistence.findByRealSchoolId(realSchoolId));
        return new ValueWrapperFuture<>(DislocationGroupDetailList == null ? Collections.emptyList() : DislocationGroupDetailList);
    }

    @Override
    public AlpsFuture<List<DislocationGroupDetail>> loadDislocationGroupDetailsByTime(Date beginTime, Date endTime) {
        if (beginTime == null || endTime == null) {
            return ValueWrapperFuture.emptyList();
        }
        List<DislocationGroupDetail> DislocationGroupDetailList = getDislocationGroups(dislocationGroupPersistence.findByTime(beginTime, endTime));
        return new ValueWrapperFuture<>(DislocationGroupDetailList == null ? Collections.emptyList() : DislocationGroupDetailList);
    }

    @Override
    public AlpsFuture<MapMessage> createDislocationGroup(DislocationGroup dislocationGroup) {
        if (dislocationGroup == null || dislocationGroup.getGroupId() == null || dislocationGroup.getRealSchoolId() == null || StringUtils.isBlank(dislocationGroup.getLatestOperator()) || StringUtils.isBlank(dislocationGroup.getNotes())) {
            return new ValueWrapperFuture<>(MapMessage.errorMessage("参数有误"));
        }
        if (groupLoaderClient.loadGroup(dislocationGroup.getGroupId(), false) == null) {
            return new ValueWrapperFuture<>(MapMessage.errorMessage(dislocationGroup.getGroupId() + "组不存在"));
        }
        School realSchool = schoolLoaderClient.getSchoolLoader()
                .loadSchool(dislocationGroup.getRealSchoolId())
                .getUninterruptibly();
        if (realSchool == null) {
            return new ValueWrapperFuture<>(MapMessage.errorMessage("学校" + dislocationGroup.getRealSchoolId() + "不存在"));
        }
        GroupMapper groupMapper = groupLoaderClient.loadGroup(dislocationGroup.getGroupId(), false);
        if (groupMapper != null) {
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(groupMapper.getClazzId());
            if (clazz != null) {
                School school = schoolLoaderClient.getSchoolLoader()
                        .loadSchool(clazz.getSchoolId())
                        .getUninterruptibly();
                if (school == null) {
                    return new ValueWrapperFuture<>(MapMessage.errorMessage(dislocationGroup.getGroupId() + "组当前学校不存在"));
                } else if (school.getId().longValue() == realSchool.getId().longValue()) {
                    return new ValueWrapperFuture<>(MapMessage.errorMessage("当前学校和新增的实际学校相同"));
                }
            }
        }
        if (dislocationGroupPersistence.loadByGroupId(dislocationGroup.getGroupId()) != null) {//groupId已经存在时,不允许在添加
            return new ValueWrapperFuture<>(MapMessage.errorMessage("已存在相同GroupID" + dislocationGroup.getGroupId() + "的记录"));
        } else {
            dislocationGroupPersistence.insert(dislocationGroup);
        }
        return new ValueWrapperFuture<>(MapMessage.successMessage());
    }

    @Override
    public AlpsFuture<MapMessage> disableDislocationGroupByGroupId(Long groupId, String operationNotes, String operator) {
        if (groupId == null || StringUtils.isBlank(operationNotes)) {
            return new ValueWrapperFuture<>(MapMessage.errorMessage("参数错误"));
        }
        dislocationGroupPersistence.disableByGroupId(groupId, operationNotes, operator);
        return new ValueWrapperFuture<>(MapMessage.successMessage());
    }

    @Override
    public AlpsFuture<MapMessage> updateDislocationGroup(DislocationGroup dislocationGroup) {
        if (dislocationGroup == null) {
            return new ValueWrapperFuture<>(MapMessage.errorMessage("参数错误"));
        }
        GroupMapper groupMapper = groupLoaderClient.loadGroup(dislocationGroup.getGroupId(), false);
        if (groupMapper != null) {
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(groupMapper.getClazzId());
            if (clazz != null) {
                School school = schoolLoaderClient.getSchoolLoader()
                        .loadSchool(clazz.getSchoolId())
                        .getUninterruptibly();
                School realSchool = schoolLoaderClient.getSchoolLoader()
                        .loadSchool(dislocationGroup.getRealSchoolId())
                        .getUninterruptibly();
                if (school == null) {
                    return new ValueWrapperFuture<>(MapMessage.errorMessage(dislocationGroup.getGroupId() + "组当前学校不存在"));
                } else if (school.getId().longValue() == realSchool.getId().longValue()) {
                    return new ValueWrapperFuture<>(MapMessage.errorMessage("当前学校和要更新的实际学校相同"));
                }
            }
        }
        dislocationGroupPersistence.replace(dislocationGroup);
        return new ValueWrapperFuture<>(MapMessage.successMessage());
    }

    private List<DislocationGroupDetail> getDislocationGroups(List<DislocationGroup> dislocationGroupList) {
        if (CollectionUtils.isEmpty(dislocationGroupList)) {
            return Collections.emptyList();
        }
        List<DislocationGroupDetail> dislocationGroupDetailList = dislocationGroupList.stream()
                .map(DislocationGroupDetail::new)
                .collect(Collectors.toList());
        Set<Long> groupIds = new LinkedHashSet<>();
        Set<Long> schooIds = new LinkedHashSet<>();
        dislocationGroupDetailList.forEach(dislocationGroupDetail -> {
            groupIds.add(dislocationGroupDetail.getGroupId());
            schooIds.add(dislocationGroupDetail.getRealSchoolId());
        });
        Map<Long, GroupMapper> longGroupMapperMap = groupLoaderClient.loadGroups(groupIds, false);
        Map<Long, Long> groupIdClazzIdMap = new LinkedHashMap<>();
        Set<Long> clazzIds = new LinkedHashSet<>();
        longGroupMapperMap.values().forEach(groupMapper -> {
            clazzIds.add(groupMapper.getClazzId());
            if (!groupIdClazzIdMap.containsKey(groupMapper.getId())) {
                groupIdClazzIdMap.put(groupMapper.getId(), groupMapper.getClazzId());
            }
        });
        Map<Long, Long> clazzIdSchoolIdMap = new LinkedHashMap<>();
        raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .forEach(clazz -> {
                    if (!clazzIdSchoolIdMap.containsKey(clazz.getId())) {
                        clazzIdSchoolIdMap.put(clazz.getId(), clazz.getSchoolId());
                        schooIds.add(clazz.getSchoolId());
                    }
                });
        Map<Long, School> schoolIdSchoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schooIds)
                .getUninterruptibly();

        dislocationGroupDetailList.forEach(dislocationGroupDetail -> {
            Long tempClazzId = groupIdClazzIdMap.get(dislocationGroupDetail.getGroupId());
            if (tempClazzId != null) {
                Long tempSchoolId = clazzIdSchoolIdMap.get(tempClazzId);
                if (tempSchoolId != null) {
                    School tempCurrentSchool = schoolIdSchoolMap.get(tempSchoolId);
                    if (tempCurrentSchool != null) {
                        dislocationGroupDetail.setCurrentSchoolId(tempCurrentSchool.getId().toString());
                        dislocationGroupDetail.setCurrentSchoolName(tempCurrentSchool.getCname());
                    }
                }
            }
            School tempRealSchool = schoolIdSchoolMap.get(dislocationGroupDetail.getRealSchoolId());
            if (tempRealSchool != null) {
                dislocationGroupDetail.setRealSchoolName(tempRealSchool.getCname());
            }
        });
        return dislocationGroupDetailList;
    }
}
