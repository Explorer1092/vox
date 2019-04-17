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

package com.voxlearning.washington.controller.mobile.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilder;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzIntegralPool;
import com.voxlearning.utopia.service.flower.api.entities.FlowerRankMember;
import com.voxlearning.utopia.service.user.api.constants.RefStatus;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Flower;
import com.voxlearning.utopia.service.user.api.entities.FlowerExchangeHistory;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by jiangpeng on 16/9/9.
 */
@Controller
@RequestMapping(value = "/teacherMobile/flower")
@Slf4j
public class MobileTeacherFlowerController extends AbstractMobileTeacherController {


    @RequestMapping(value = "/teacher/subject_clazz.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage clazzList() {
        Teacher teacher = currentTeacher();
        if (teacher == null)
            return noLoginResult;
        MapMessage result = MapMessage.successMessage();
        Boolean multipleSubject = teacher.getSubjects().size() > 1;
        result.add("multiple_subject", multipleSubject);
        result.add("teacher_main_subject", teacher.getSubject().name());
        try {
            List<Clazz> clazzList = getTeacherClazzListForFlower(teacher, null);
            Map<Long, Clazz> clazzMap = clazzList.stream().distinct().collect(Collectors.toMap(Clazz::getId, Function.identity()));
            Map<Long, List<GroupMapper>> teacherAllGroupInClazz = teacherLoaderClient.findTeacherAllGroupInClazz(clazzList.stream().map(Clazz::getId).collect(Collectors.toSet()), teacher.getId());
            Map<Subject, Set<Long>> subject2ClazzIdListMap = new LinkedHashMap<>();
            teacherAllGroupInClazz.entrySet().forEach(entry -> {
                Long clazzId = entry.getKey();
                List<GroupMapper> groupMapperList = entry.getValue();
                groupMapperList.forEach(groupMapper -> {
                    Subject subject = groupMapper.getSubject();
                    if (subject2ClazzIdListMap.containsKey(subject))
                        subject2ClazzIdListMap.get(subject).add(clazzId);
                    else {
                        Set<Long> clazzIdSet = new LinkedHashSet<>();
                        clazzIdSet.add(clazzId);
                        subject2ClazzIdListMap.put(subject, clazzIdSet);
                    }
                });
            });

            List<Map<String, Object>> subjectClazzList = new ArrayList<>();
            subject2ClazzIdListMap.entrySet().stream().sorted(Comparator.comparingInt(o -> o.getKey().getKey())).forEach(entry -> {
                Map<String, Object> subjectClazzMap = new LinkedHashMap<>();
                Subject subject = entry.getKey();
                Set<Long> clazzIdSet = entry.getValue();
                subjectClazzMap.put("subject", subject.name());
                subjectClazzMap.put("subject_name", subject.getValue());
                List<Map<String, Object>> clazzMapList = new ArrayList<>();
                List<Clazz> clazzs = clazzIdSet.stream().map(clazzMap::get).collect(Collectors.toList());
                clazzs.forEach(clazz -> {
                    Map<String, Object> clazzInfoMap = new LinkedHashMap<>();
                    clazzInfoMap.put("clazz_id", clazz.getId());
                    clazzInfoMap.put("clazz_name", clazz.formalizeClazzName());
                    List<GroupMapper> groupMapperList = teacherAllGroupInClazz.get(clazz.getId());
                    if (groupMapperList != null) {
                        GroupMapper groupMapper = groupMapperList.stream().filter(g -> subject.equals(g.getSubject())).findFirst().orElse(null);
                        if (groupMapper != null) {
                            clazzInfoMap.put("group_id", groupMapper.getId());
                        }
                    }
                    clazzMapList.add(clazzInfoMap);
                });
                subjectClazzMap.put("clazz_list", clazzMapList);
                subjectClazzList.add(subjectClazzMap);
            });

            return result.add("teacher_subject_list", subjectClazzList).add("is_auth", true);
        } catch (Exception ex) {
            logger.error(getClass().getName() + ex.getMessage(), ex);
            return MapMessage.errorMessage("获取家长送花列表失败，请稍后再试");
        }
    }


    @RequestMapping(value = "/parent/rank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage flowerRankByParent() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null)
            return noLoginResult;

        Long clazzId = getRequestLong("clazz_id");
        if (clazzId == 0)
            return MapMessage.errorMessage("班级id不能为空");
        try {
            List<Map<String, Object>> rankList = washingtonCacheSystem.CBS.flushable
                    .wrapCache(businessStudentServiceClient)
                    .expiration(1800)
                    .keyPrefix("PARENT_FLOWER_RANK")
                    .keys(getSubjectSpecifiedTeacherId(), clazzId)
                    .proxy()
                    .findCurrentMonthFlowerRankByTeacherIdAndClazzId(teacher.getId(), clazzId);
            return MapMessage.successMessage().add("rank_list", rankList);
        } catch (Exception ex) {
            logger.error(getClass().getName() + ex.getMessage(), ex);
            return MapMessage.errorMessage("获取家长送花列表失败，请稍后再试");
        }
    }


    @RequestMapping(value = "/teacher/rank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage flowerRankByTeacher() {
        try {
            TeacherDetail teacherDetail = currentTeacherDetail();
            if (teacherDetail == null)
                return noLoginResult;

            MapMessage result = MapMessage.successMessage();
            Boolean multipleSubject = teacherDetail.getSubjects().size() > 1;
            result.add("multiple_subject", multipleSubject);
            if (multipleSubject) {
                result.add("teacher_subject_list", toSubjectList(teacherDetail.getSubjects(), true));
                result.add("teacher_main_subject", teacherDetail.getSubject().name());
            }
            List<Subject> teacherSubjectList = teacherDetail.getSubjects();
            List<Map<String, Object>> subjectRankList = new ArrayList<>();
            teacherSubjectList.forEach(subject -> {
                Map<String, Object> subjectRankMap = new LinkedHashMap<>();
                subjectRankMap.put("subject", subject.name());
                subjectRankMap.put("subject_name", subject.getValue());
                List<Map<String, Object>> rankList = washingtonCacheSystem.CBS.flushable
                        .wrapCache(businessStudentServiceClient)
                        .expiration(1800)
                        .keyPrefix("TEACHER_FLOWER_RANK")
                        .keys(teacherDetail.getTeacherSchoolId(), subject)
                        .proxy()
                        .findCurrentMonthFlowerRankBySchoolId(teacherDetail.getTeacherSchoolId(), subject);
                subjectRankMap.put("rank_list", rankList);
                subjectRankList.add(subjectRankMap);

            });

            return result.add("subject_rank_list", subjectRankList);
        } catch (Exception ex) {
            logger.error(getClass().getName() + ex.getMessage(), ex);
            return MapMessage.errorMessage("获取老师收花列表失败，请稍后再试");
        }
    }

    // 执行兑换
    @RequestMapping(value = "flowerexchange.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage flowerExchange() {
        Long clazzId = getRequestLong("clazz_id");
        Long userId = getSubjectSpecifiedTeacherId();
        if (clazzId == 0 || userId == null) {
            return MapMessage.errorMessage();
        }
        try {
            AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
            return builder.keyPrefix("TEACHER_EXCHANGE_FLOWER")
                    .keys(userId)
                    .expirationInSeconds(30)
                    .callback(() -> flowerServiceClient.getFlowerService()
                            .flowerExchange(userId, clazzId, FlowerExchangeHistory.Source.MOBILE.name())
                            .getUninterruptibly())
                    .build()
                    .execute();
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    @RequestMapping(value = "exchangeinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage exchangeInfo() {
        try {
            TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
            if (teacherDetail == null)
                return noLoginResult;
            Subject currentSubject = currentSubject();
            List<Map<String, Object>> clazzInfoMapList = new ArrayList<>();
            List<Clazz> clazzList = getTeacherClazzListForFlower(currentTeacher(), currentSubject);
            Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherDetail.getId());
            Set<Long> allTeacherIds = new HashSet<>(relTeacherIds);
            allTeacherIds.add(teacherDetail.getId());
            Map<Long, List<GroupTeacherMapper>> teacherGroups = deprecatedGroupLoaderClient.loadTeacherGroups(allTeacherIds, true);

            Map<String, AlpsFuture<List<FlowerRankMember>>> rankListFutureMap = new HashMap<>();
            teacherGroups.values().stream().flatMap(Collection::stream).forEach(t -> {
                Map.Entry<Long, RefStatus> statusEntry = t.getTeacherGroupRefStatusMap().entrySet().stream().filter(entry -> entry.getValue() == RefStatus.VALID).findAny().orElse(null);
                if (statusEntry == null)
                    return;
                Long teacherId = statusEntry.getKey();
                Long groupId = t.getId();
                String key = groupId + "_" + teacherId;
                rankListFutureMap.put(key, flowerServiceClient.getFlowerConditionService().loadFlowerTermRank(SchoolYear.newInstance().currentTermDateRange(), groupId, teacherId));
            });

            Map<Long, List<GroupTeacherMapper>> clazzId2GroupMapperListMap = teacherGroups.values().stream().flatMap(Collection::stream).collect(Collectors.groupingBy(GroupTeacherMapper::getClazzId));
            MonthRange range = MonthRange.current();
            clazzList.forEach((Clazz clazz) -> {
                Map<String, Object> clazzInfoMap = new LinkedHashMap<>();
                clazzInfoMap.put("clazz_id", clazz.getId());
                clazzInfoMap.put("clazz_name", clazz.formalizeClazzName());
                Long groupId;
                List<GroupTeacherMapper> groupTeacherMappers = clazzId2GroupMapperListMap.get(clazz.getId());
                GroupTeacherMapper groupTeacherMapper;
                if (currentSubject != null) {
                    groupTeacherMapper = groupTeacherMappers.stream().filter(t -> t.getSubject() == currentSubject).findAny().orElse(null);
                    if (groupTeacherMapper == null)
                        return;
                    groupId = groupTeacherMapper.getId();
                }else{
                    groupTeacherMapper = groupTeacherMappers.stream().findAny().orElse(null);
                    if (groupTeacherMapper == null)
                        return;
                    groupId = groupTeacherMapper.getId();
                }
                AlpsFuture<SmartClazzIntegralPool> integralPoolAlpsFuture = clazzIntegralServiceClient.getClazzIntegralService()
                        .loadClazzIntegralPool(groupId);
                SmartClazzIntegralPool integralPool = integralPoolAlpsFuture.getUninterruptibly();
                if (integralPool == null)
                    clazzInfoMap.put("clazz_integral", 0);
                else
                    clazzInfoMap.put("clazz_integral", integralPool.fetchTotalIntegral());
                // 获取老师班级学豆数
                AlpsFuture<List<FlowerRankMember>> rankListFuture = rankListFutureMap.get(groupId + "_" + teacherDetail.getId());
                if (rankListFuture == null)
                    return;
                clazzInfoMap.put("group_id", groupId);
                List<FlowerRankMember> flowerRankMembers = rankListFuture.getUninterruptibly();
                List<Long> studentIdList = flowerRankMembers.stream().map(FlowerRankMember::getStudentId).collect(Collectors.toList());
                Map<Long, User> studentMap = userLoaderClient.loadUsers(studentIdList);
                if (CollectionUtils.isNotEmpty(flowerRankMembers)){
                    List<FlowerRankMember> threeMembers;
                    if (flowerRankMembers.size() >3) {
                        threeMembers = new ArrayList<>(flowerRankMembers.subList(0, 3));
                    }else
                        threeMembers = flowerRankMembers;
                    threeMembers.forEach(t -> {
                        User user = studentMap.get(t.getStudentId());
                        if (user == null)
                            return;
                        t.setAvatarUrl(getUserAvatarImgUrl(user));
                    });
                    clazzInfoMap.put("flower_rank_list", threeMembers);
                }

                // 获取当前老师 当前班级 本月的鲜花总数
                List<Flower> flowers = flowerServiceClient.getFlowerService()
                        .loadClazzFlowers(clazz.getId())
                        .getUninterruptibly();
                flowers = flowers.stream().filter(f -> f.getReceiverId() != null && Objects.equals(f.getReceiverId(), teacherDetail.getId()))
                        .filter(f -> range.contains(f.getCreateDatetime())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(flowers)) {
                    clazzInfoMap.put("flower_count", 0);
                    clazzInfoMap.put("exchange_count", 0);
                    clazzInfoMapList.add(clazzInfoMap);
                    return;
                }
                // 获取本月当前老师 当前班级 已经兑换的次数
                List<FlowerExchangeHistory> histories = flowerServiceClient.getFlowerService()
                        .loadTeacherFlowerExchangeHistories(teacherDetail.getId())
                        .getUninterruptibly();
                histories = histories.stream().filter(h -> range.contains(h.getCreateDatetime()))
                        .filter(h -> Objects.equals(clazz.getId(), h.getClazzId())).collect(Collectors.toList());
                int flowerCount = flowers.size() - (histories.size() * 10);
                int exchangeCount = flowerCount == 0 ? 0 : flowerCount / 10;
                clazzInfoMap.put("flower_count", flowerCount);
                clazzInfoMap.put("exchange_count", exchangeCount);
                clazzInfoMapList.add(clazzInfoMap);

            });
            // 查出本月有多少家长赠送鲜花
            long senderCnt = flowerServiceClient.getFlowerService()
                    .loadReceiverFlowers(teacherDetail.getId())
                    .getUninterruptibly()
                    .stream()
                    .filter(f -> f.getSenderId() != null)
                    .filter(t -> range.contains(t.fetchCreateTimestamp()))
                    .map(Flower::getSenderId)
                    .distinct().count();
            // 灰度关闭鲜花 http://project.17zuoye.net/redmine/issues/34943
            boolean closeFlower = getGrayFunctionManagerClient().getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacherDetail, "Flower", "Close");
            return MapMessage.successMessage().add("exchange_info", clazzInfoMapList)
                    .add("senderCnt", senderCnt)
                    .add("close_flower", closeFlower)
                    .add("cityName", currentTeacherDetail().getCityName());
        } catch (Exception ex) {
            logger.error(getClass().getName() + ex.getMessage(), ex);
            return MapMessage.errorMessage("获取信息失败，请稍后再试");
        }
    }

}
