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

package com.voxlearning.utopia.service.zone.impl.service;


import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.athena.bean.MarkThumbsUp;
import com.voxlearning.athena.bean.Marks;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.zone.api.constant.ClazzRecordCardStatusEnum;
import com.voxlearning.utopia.service.zone.api.constant.ClazzRecordTypeEnum;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.ClazzRecordCardMapper;
import com.voxlearning.utopia.service.zone.impl.loader.PersonalZoneLoaderImpl;
import com.voxlearning.utopia.service.zone.impl.manager.ClazzRecordHwLikeCacheManager;
import com.voxlearning.utopia.service.zone.impl.manager.ClazzRecordNonHwLikeCacheManager;
import com.voxlearning.utopia.service.zone.impl.manager.ClazzRecordSoundShareManager;
import lombok.Getter;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/3/2
 * Time: 18:36
 */
@Named
public class ClazzRecordHelper extends SpringContainerSupport {

    @Inject private UserLoaderClient userLoaderClient;
    @Inject private PersonalZoneLoaderImpl personalZoneLoaderImpl;
    @Inject private PrivilegeBufferServiceClient privilegeBufferServiceClient;

    @Getter private ClazzRecordNonHwLikeCacheManager clazzRecordNonHwLikeCacheManager;
    @Getter private ClazzRecordHwLikeCacheManager clazzRecordHwLikeCacheManager;
    @Getter private ClazzRecordSoundShareManager clazzRecordSoundShareManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        clazzRecordNonHwLikeCacheManager = new ClazzRecordNonHwLikeCacheManager(CacheSystem.CBS.getCache("persistence"));
        clazzRecordHwLikeCacheManager = new ClazzRecordHwLikeCacheManager(CacheSystem.CBS.getCache("persistence"));
        clazzRecordSoundShareManager = new ClazzRecordSoundShareManager(CacheSystem.CBS.getCache("persistence"));
    }


    /**
     * 渲染 挑战结束卡片
     */
    public void renderDoneFocusCardMapper(List<NewHomeworkResult> first, ClazzRecordCardMapper cardMapper) {
        NewHomeworkResult newHomeworkResult = first.get(0);
        cardMapper.setHomeworkId(newHomeworkResult.getHomeworkId());
        cardMapper.setScore(newHomeworkResult.processScore());
        cardMapper.setTime(calBreakTime(newHomeworkResult));
        cardMapper.setHasGot(first.stream().map(NewHomeworkResult::getUserId).collect(Collectors.toList()));
        cardMapper.setGroupId(newHomeworkResult.getClazzGroupId());
        cardMapper.setSubject(newHomeworkResult.getSubject());
        //1个第一名 展示头像 和 头饰
        if (first.size() == 1) {
            fillAloneMapper(newHomeworkResult.getUserId(), cardMapper);
        }
    }

    /**
     * 获取头饰
     */
    public String getHeadWear(Long userId) {
        if (null == userId || 0 >= userId) {
            return null;
        }
        StudentInfo studentInfo = personalZoneLoaderImpl.loadStudentInfo(userId);
        if (studentInfo != null) {
            Privilege headWearPrivilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadById(studentInfo.getHeadWearId());
            return headWearPrivilege == null ? null : headWearPrivilege.getImg();
        }
        return null;
    }

    /**
     * 计算中断时间
     */
    public Long calBreakTime(NewHomeworkResult r) {
        Long allTime = (r.getFinishAt().getTime() - r.getUserStartAt().getTime()) / 1000;
        return allTime - r.processDuration() + 1;
    }

    /**
     * 渲染名次为一个人的数据
     */
    public void fillAloneMapper(Long userId, ClazzRecordCardMapper cardMapper) {
        User user = userLoaderClient.loadUser(userId);
        cardMapper.setImage(user.fetchImageUrl());
        cardMapper.setHeadWear(getHeadWear(userId));
        cardMapper.setStudentName(user.fetchRealname());
    }


    public void fillUserInfo(ClazzRecordCardMapper mapper, Long userId) {
        User user = userLoaderClient.loadUser(userId);
        if (user == null) return;

        mapper.setImage(user.fetchImageUrl());
        mapper.setStudentName(user.fetchRealname());
        StudentInfo studentInfo = personalZoneLoaderImpl.loadStudentInfo(userId);
        if (studentInfo == null) return;

        Privilege headWearPrivilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadById(studentInfo.getHeadWearId());
        if (headWearPrivilege != null) {
            mapper.setHeadWear(headWearPrivilege.getImg());
        }

    }

    /**
     * 获取满分数据 map
     */
    public TreeMap<Integer, List<Long>> getFullMarkSortMap(List<MarkThumbsUp> markThumbsUps) {
        if (CollectionUtils.isEmpty(markThumbsUps)) {
            return new TreeMap<>();
        }

        List<Marks> allMarks = new LinkedList<>();
        markThumbsUps.forEach(mu -> {
            if (mu != null && CollectionUtils.isNotEmpty(mu.getMarksList())) {
                allMarks.addAll(mu.getMarksList());
            }
        });
        //计算 个人总共获取的满分次数
        Map<Long, Integer> countingMap = new HashMap<>();
        for (Marks marks : allMarks) {
            int count = SafeConverter.toInt(marks.getCount());
            countingMap.merge(marks.getUserId(), count, (a, b) -> b + a);
        }
        if (MapUtils.isEmpty(countingMap)) {
            return new TreeMap<>();
        }

        //计算分组 按照满分次数 降序排序
        TreeMap<Integer, List<Long>> sortedMap = new TreeMap<>(Comparator.reverseOrder());
        for (Map.Entry<Long, Integer> entry : countingMap.entrySet()) {
            List<Long> vs = sortedMap.get(entry.getValue());
            if (CollectionUtils.isEmpty(vs)) {
                List<Long> tempVs = new LinkedList<>();
                tempVs.add(entry.getKey());
                sortedMap.put(entry.getValue(), tempVs);
            } else {
                vs.add(entry.getKey());
            }
        }

        return sortedMap;
    }

    /**
     * 渲染满分 card
     */
    public ClazzRecordCardMapper renderFullMarksCard(List<Long> studentIds, Integer count) {
        ClazzRecordCardMapper cardMapper = new ClazzRecordCardMapper(
                ClazzRecordTypeEnum.FULLMARKS_STAR, ClazzRecordCardStatusEnum.DONE
        );
        cardMapper.setNotExist(false);
        cardMapper.setCount(count);
        if (studentIds.size() > 1) {
            cardMapper.setHasGot(studentIds);
        } else {
            User user = userLoaderClient.loadUser(studentIds.get(0));
            cardMapper.setImage(user.fetchImageUrl());
            cardMapper.setHeadWear(getHeadWear(user.getId()));
            cardMapper.setHasGot(Collections.singletonList(user.getId()));
            cardMapper.setStudentName(user.fetchRealname());
        }
        return cardMapper;
    }


    /**
     * 渲染 友谊卡
     */
    public ClazzRecordCardMapper renderFriendshipCard(Map<Integer, List<Marks>> marks, Integer key) {
        ClazzRecordCardMapper cardMapper = new ClazzRecordCardMapper(ClazzRecordTypeEnum.FRIENDSHIP_STAR);
        if (MapUtils.isEmpty(marks) || key == null || marks.get(key) == null) {
            cardMapper.setNotExist(true);
            return cardMapper;
        }
        List<Marks> firsts = marks.get(key);
        if (CollectionUtils.isNotEmpty(firsts)) {
            cardMapper.setNotExist(false);
            cardMapper.setStatusEnum(ClazzRecordCardStatusEnum.DONE);
            cardMapper.setCount(firsts.get(0).getCount());
            cardMapper.setHasGot(firsts.stream().map(Marks::getUserId).collect(Collectors.toList()));
            if (firsts.size() == 1) {
                fillUserInfo(cardMapper, firsts.get(0).getUserId());
            }
        }
        return cardMapper;
    }

    public List<NewHomeworkResult> filterFocusRecords(Map<Long, NewHomeworkResult> homeworkResultMap) {
        if (MapUtils.isEmpty(homeworkResultMap)) {
            return Collections.emptyList();
        }

        // 平均分数
        double average = homeworkResultMap.values().stream()
                .filter(NewHomeworkResult::isFinished)
                .filter(r -> r.processScore() != null)
                .mapToInt(NewHomeworkResult::processScore)
                .average()
                .orElse(0D);

        // 平均做题时间
        double averageTime = homeworkResultMap.values().stream()
                .filter(BaseHomeworkResult::isFinished)
                .filter(r -> r.processScore() != null)
                .mapToLong(NewHomeworkResult::processDuration)
                .average()
                .orElse(0D);

        //单次作业中断时间=作业时间-做题时间，且作业分数>=班级平均分 且 做题时间<班级平均做题时间
        return homeworkResultMap.values().stream()
                .filter(NewHomeworkResult::isFinished)
                .filter(r -> r.processScore() != null && r.processScore() > 0)
                .filter(r -> new BigDecimal(r.processScore()).doubleValue() > (average - 0.1)) // 分数大于平均分数
                .filter(r -> new BigDecimal(r.processDuration()).doubleValue() < averageTime)  // 做题时间小于平均做题时间
                .collect(Collectors.toList());
    }

}
