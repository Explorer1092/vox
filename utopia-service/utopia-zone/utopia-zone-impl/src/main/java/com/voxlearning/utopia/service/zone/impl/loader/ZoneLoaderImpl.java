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

package com.voxlearning.utopia.service.zone.impl.loader;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.integral.api.entities.Integral;
import com.voxlearning.utopia.service.integral.client.IntegralLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserAggregationLoaderClient;
import com.voxlearning.utopia.service.zone.api.ZoneLoader;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;
import com.voxlearning.utopia.service.zone.impl.persistence.StudentInfoPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default {@link ZoneLoader} implementation.
 *
 * @author Xiaohai Zhang
 * @since Feb 25, 2015
 */
@Named
@ExposeService(interfaceClass = ZoneLoader.class)
public class ZoneLoaderImpl extends SpringContainerSupport implements ZoneLoader {

    @Inject private IntegralLoaderClient integralLoaderClient;

    @Inject private StudentInfoPersistence studentInfoPersistence;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private UserAggregationLoaderClient userAggregationLoaderClient;
    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private PersonalZoneLoaderImpl personalZoneLoader;

    private final String rankSliverCacheKey = "CLAZZ_ZONE:RANK:SLIVER:SNAPSHOT:%s";
    private final String rankSMCacheKey = "CLAZZ_ZONE:RANK:SM:SNAPSHOT:%s";
    private final String rankLockKey = "CLAZZ_ZONE:RANK:LOCK:SNAPSHOT:%s";
    private final String rankSnapshotFlagKey = "CLAZZ_ZONE:RANK:FLAG:SNAPSHOT:%s";

    private final AtomicLockManager atomicLockManager = AtomicLockManager.instance();

    private UtopiaCache flushable;
    private UtopiaCache unflushable;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        flushable = CacheSystem.CBS.getCache("flushable");
        unflushable = CacheSystem.CBS.getCache("unflushable");
    }

    @Override
    public List<Map<String, Object>> likeCountRank(Clazz clazz, Long userId) {
        if (clazz == null) {
            return Collections.emptyList();
        }

        List<User> clazzStudents = userAggregationLoaderClient.loadLinkedStudentsByClazzId(clazz.getId(), userId);
        if (CollectionUtils.isEmpty(clazzStudents)) {
            return Collections.emptyList();
        }

        Set<Long> studentIds = new TreeSet<>();
        // 班级中所有人
        Map<Long, User> studentMap = clazzStudents.stream()
                .collect(Collectors.toMap(User::getId, Function.identity(),
                        (u, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", u));
                        },
                        LinkedHashMap::new));
        studentIds.addAll(studentMap.keySet());

        List<StudentInfo> studentInfos = studentInfoPersistence.findByUserIdsOrderByLikeCountDesc(studentIds);

        List<Map<String, Object>> result = new LinkedList<>();
        for (StudentInfo studentInfo : studentInfos) {
            Map<String, Object> each = new HashMap<>();
            User user = studentMap.get(studentInfo.getStudentId());
            if (null != user) {
                each.put("studentId", user.getId());
                each.put("studentName", getStudentName(user));
                each.put("studentImg", user.fetchImageUrl());
                each.put("likeCount", studentInfo.getLikeCount());
                result.add(each);
                studentIds.remove(user.getId());
            }
        }
        for (Long studentId : studentIds) {
            Map<String, Object> each = new HashMap<>();
            User user = studentMap.get(studentId);
            if (null != user) {
                each.put("studentId", user.getId());
                each.put("studentName", getStudentName(user));
                each.put("studentImg", user.fetchImageUrl());
                each.put("likeCount", 0);
                result.add(each);
            }
        }
        return result;
    }


    @Override
    public List<Map<String, Object>> studyMasterCountRank(Clazz clazz, Long userId) {
        return studyMasterCountRank(clazz, userId, false);
    }


    private List<Map<String, Object>> studyMasterCountRank(Clazz clazz, Long userId, boolean snapshot) {
        if (clazz == null) return Collections.emptyList();

        List<User> clazzStudents = userAggregationLoaderClient.loadLinkedStudentsByClazzId(clazz.getId(), userId);
        if (CollectionUtils.isEmpty(clazzStudents)) return Collections.emptyList();

        // 班级中所有人
        Set<Long> studentIds = new TreeSet<>();
        Map<Long, User> studentMap = clazzStudents.stream()
                .collect(Collectors.toMap(User::getId, Function.identity(),
                        (u, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", u));
                        },
                        LinkedHashMap::new));
        studentIds.addAll(studentMap.keySet());
        //学霸
        List<StudentInfo> studentInfos = new LinkedList<>();

        if (snapshot && snapshotted(clazz.getId())) {
            CacheObject<Map<Long, StudentInfo>> cacheObject = unflushable.get(String.format(rankSMCacheKey, clazz.getId()));
            if (cacheObject != null && MapUtils.isNotEmpty(cacheObject.getValue())) {
                studentInfos = cacheObject.getValue().values().stream().filter(info -> studentIds.contains(info.getStudentId())).collect(Collectors.toList());
            }
        } else {
            studentInfos = studentInfoPersistence.findByUserIdsOrderByStudyMasterCountDesc(studentIds);
        }
        //排序
        studentInfos = studentInfos.stream()
                .sorted((s1, s2) -> {
                    Integer c1 = s1.getStudyMasterCountValue();
                    Integer c2 = s2.getStudyMasterCountValue();
                    return c2.compareTo(c1);
                }).collect(Collectors.toList());

        List<Map<String, Object>> result = new LinkedList<>();
        for (StudentInfo studentInfo : studentInfos) {
            Map<String, Object> each = new HashMap<>();
            User user = studentMap.get(studentInfo.getStudentId());
            if (null != user) {
                each.put("studentId", user.getId());
                each.put("studentName", getStudentName(user));
                each.put("studentImg", user.fetchImageUrl());
                each.put("smCount", studentInfo.getStudyMasterCount());
                each.put("studentHeadWear", studentInfo.getHeadWearId());
                result.add(each);
                studentIds.remove(user.getId());
            }
        }
        for (Long studentId : studentIds) {
            Map<String, Object> each = new HashMap<>();
            User user = studentMap.get(studentId);
            if (null != user) {
                each.put("studentId", user.getId());
                each.put("studentName", getStudentName(user));
                each.put("studentImg", user.fetchImageUrl());
                each.put("smCount", 0);
                result.add(each);
            }
        }
        return result;
    }


    @Override
    public List<Map<String, Object>> silverRank(Clazz clazz, Long userId) {
        return silverRank(clazz, userId, false);
    }

    private List<Map<String, Object>> silverRank(Clazz clazz, Long userId, boolean snapshot) {
        if (clazz == null) return Collections.emptyList();
        StudentDetail student = studentLoaderClient.loadStudentDetail(userId);
        if (student == null || student.getClazz() == null) return Collections.emptyList();

        List<User> clazzStudents = userAggregationLoaderClient.loadLinkedStudentsByClazzId(clazz.getId(), userId);
        if (CollectionUtils.isEmpty(clazzStudents)) return Collections.emptyList();

        // 班级中所有人
        Set<Long> studentIds = new TreeSet<>();
        Map<Long, User> studentMap = clazzStudents.stream()
                .collect(Collectors.toMap(User::getId, Function.identity(),
                        (u, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", u));
                        },
                        LinkedHashMap::new));
        studentIds.addAll(studentMap.keySet());
        Map<Long, StudentInfo> studentInfoMap = personalZoneLoader.loadStudentInfos(studentIds);

        List<Map<String, Object>> result = new LinkedList<>();

        // 有积分记录的学生
        List<Integral> integrals = new LinkedList<>();

        //如果走快照且快照生成
        if (snapshot && snapshotted(clazz.getId())) {
            CacheObject<Map<Long, Integral>> cacheObject = unflushable.get(String.format(rankSliverCacheKey, clazz.getId()));
            if (cacheObject != null && MapUtils.isNotEmpty(cacheObject.getValue())) {
                integrals = cacheObject.getValue().values().stream().filter(integral -> studentIds.contains(integral.getId())).collect(Collectors.toList());
            }
        } else {
            integrals = new ArrayList<>(integralLoaderClient.getIntegralLoader().loadIntegralsOrdered(studentIds).values());
        }
        //排序
        integrals = integrals.stream()
                .sorted((o1, o2) -> o2.getUsableIntegral().compareTo(o1.getUsableIntegral()))
                .collect(Collectors.toList());

        StudentInfo studentInfo;
        for (Integral integral : integrals) {
            Map<String, Object> each = new HashMap<>();
            User user = studentMap.get(integral.getId());
            if (null != user) {
                each.put("studentId", user.getId());
                each.put("studentName", getStudentName(user));
                each.put("studentImg", user.fetchImageUrl());
                each.put("studentUsableIntegral", integral.getUsableIntegral());

                studentInfo = studentInfoMap.get(user.getId());
                if (studentInfo != null) {
                    each.put("studentHeadWear", studentInfo.getHeadWearId());
                }

                result.add(each);
                studentIds.remove(user.getId());
            }
        }

        for (Long studentId : studentIds) {
            Map<String, Object> each = new HashMap<>();
            User user = studentMap.get(studentId);
            if (null != user) {
                each.put("studentId", user.getId());
                each.put("studentName", getStudentName(user));
                each.put("studentImg", user.fetchImageUrl());
                each.put("studentUsableIntegral", 0);
                result.add(each);
            }
        }

        return result;
    }

    @Override
    public void rankSnapshot(Long clazzId) {

        if (clazzId == null) {
            return;
        }
        try {
            atomicLockManager.acquireLock(String.format(rankLockKey, clazzId), 60);

            if (snapshotted(clazzId))
                return;
            //加载所有班级学生
            List<Long> studentIds = asyncGroupServiceClient.getAsyncGroupService()
                    .findStudentIdsByClazzId(clazzId);
            if (CollectionUtils.isEmpty(studentIds)) {
                return;
            }
            Set<Long> studentIdSet = new TreeSet<>(studentIds);

            /*成长值
            Map<Long, UserGrowth> userGrowthMap = actionLoader.loadUserGrowths(studentIdSet);
            if (MapUtils.isNotEmpty(userGrowthMap))
                zoneCacheSystem.CBS.unflushable.set(String.format(rankGrowthCacheKey, clazzId), 60 * 60 * 24 * 60, userGrowthMap);
            */
            //学生学豆信息
            List<Integral> integrals = new ArrayList<>(integralLoaderClient.getIntegralLoader().loadIntegrals(studentIds).values());
            if (CollectionUtils.isNotEmpty(integrals)) {
                Map<Long, Integral> userIntegralMap = new HashMap<>();
                for (Integral integral : integrals) {
                    if (integral == null) continue;
                    userIntegralMap.put(integral.getId(), integral);
                }
                unflushable.set(String.format(rankSliverCacheKey, clazzId), 60 * 60 * 24 * 60, userIntegralMap);
            }
            //学分信息
            List<StudentInfo> studentInfos = studentInfoPersistence.findByUserIdsOrderByStudyMasterCountDesc(studentIdSet);
            if (CollectionUtils.isNotEmpty(studentInfos)) {
                Map<Long, StudentInfo> studentInfoMap = new HashMap<>();
                for (StudentInfo studentInfo : studentInfos) {
                    if (studentInfo == null) continue;
                    studentInfoMap.put(studentInfo.getStudentId(), studentInfo);
                }
                unflushable.set(String.format(rankSMCacheKey, clazzId), 60 * 60 * 24 * 60, studentInfoMap);
            }
            //生成快照成功
            flushable.set(String.format(rankSnapshotFlagKey, clazzId), 60 * 60 * 24 * 60, true);
        } catch (Exception e) {

        } finally {
            atomicLockManager.releaseLock(String.format(rankLockKey, clazzId));
        }
    }

    @Override
    public List<Map<String, Object>> studyMasterCountRankSnapshot(Clazz clazz, Long userId) {
        return studyMasterCountRank(clazz, userId, true);
    }

    @Override
    public List<Map<String, Object>> silverRankSnapshot(Clazz clazz, Long userId) {
        return silverRank(clazz, userId, true);
    }


    /**
     * 是否已生成过快照
     *
     * @param clazzId
     * @return
     */
    private boolean snapshotted(Long clazzId) {
        CacheObject<Boolean> flag = flushable.get(String.format(rankSnapshotFlagKey, clazzId));
        if (flag != null && flag.getValue() != null)
            return flag.getValue();
        else
            return false;
    }

    private String getStudentName(User user) {
        return StringUtils.isNotBlank(user.getProfile().getRealname()) ? user.getProfile().getRealname() : SafeConverter.toString(user.getId(), "");
    }
}
