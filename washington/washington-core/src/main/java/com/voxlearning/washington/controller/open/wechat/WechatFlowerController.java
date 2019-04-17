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

package com.voxlearning.washington.controller.open.wechat;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilder;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.FlowerSourceType;
import com.voxlearning.utopia.service.business.client.AsyncBusinessCacheServiceClient;
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 那些关于花的故事
 * Created by Shuai Huan on 2015/5/29.
 */
@Controller
@RequestMapping(value = "/open/wechat/flower")
@Slf4j
public class WechatFlowerController extends AbstractOpenController {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private AsyncBusinessCacheServiceClient asyncBusinessCacheServiceClient;
    @Inject private FlowerServiceClient flowerServiceClient;

    @RequestMapping(value = "getclazzlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getClazzList(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long teacherId = ConversionUtils.toLong(openAuthContext.getParams().get("tid"));
        if (teacherId == 0) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }

        List<Long> classIdList = flowerServiceClient.getFlowerService()
                .loadReceiverFlowers(teacherId)
                .getUninterruptibly()
                .stream()
                .filter(t -> MonthRange.current().contains(t.fetchCreateTimestamp()))
                .filter(t -> t.getClazzId() != null)
                .map(Flower::getClazzId)
                .distinct()
                .collect(Collectors.toList());
        if (classIdList.isEmpty()) {
            openAuthContext.setCode("400");
            openAuthContext.setError("送花班级列表为空");
            return openAuthContext;
        }

        //load ClazzList
        Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(classIdList)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        if (clazzMap.isEmpty()) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }

        List<Clazz> clazzList = new LinkedList<>();
        for (Clazz clazz : clazzMap.values()) {
            clazzList.add(clazz);
        }

        openAuthContext.setCode("200");
        openAuthContext.add("clazzList", clazzList);
        return openAuthContext;
    }

    @RequestMapping(value = "sendflower.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext sendFlower(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long studentId = ConversionUtils.toLong(openAuthContext.getParams().get("sid"));
        String homeworkId = ConversionUtils.toString(openAuthContext.getParams().get("hid"));
        String homeworkType = ConversionUtils.toString(openAuthContext.getParams().get("htype"));
        Long teacherId = ConversionUtils.toLong(openAuthContext.getParams().get("tid"));
        if (studentId == 0 || StringUtils.isBlank(homeworkId) || StringUtils.isBlank(homeworkType) ||
                teacherId == 0 || HomeworkType.of(homeworkType) == HomeworkType.UNKNOWN) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }

        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        if (newHomework == null) {
            openAuthContext.setCode("400");
            openAuthContext.setError("要送花的作业不存在");
            return openAuthContext;
        }
        if (newHomework.isHomeworkChecked()) {
            openAuthContext.setCode("400");
            openAuthContext.setError("已检查的作业不能送花");
            return openAuthContext;
        }

        Long parentId = 0L;
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        FlowerSourceType type = FlowerSourceType.of(homeworkType);
        String flowerKey = homeworkType + "-" + homeworkId;

        //之前作业送花的限制。现在从底层移到业务层了。
        long count = flowerServiceClient.getFlowerService()
                .loadHomeworkFlowers(flowerKey)
                .getUninterruptibly()
                .stream()
                .filter(t -> Objects.equals(studentId, t.getSenderId()))
                .count();
        if (count > 0) {
            openAuthContext.setCode("400");
            openAuthContext.setError("不能重复送花！");
            return openAuthContext;
        }

        MapMessage response = flowerServiceClient.getFlowerService()
                .sendFlower(studentId, parentId, teacherId, studentDetail.getClazzId(), type, flowerKey)
                .getUninterruptibly();
        if (!response.isSuccess()) {
            openAuthContext.setCode("400");
            openAuthContext.setError("送花失败");
            if (response.hasDuplicatedException()) {
                openAuthContext.setError("请不要重复送花");
            }
            return openAuthContext;
        } else {
            openAuthContext.setCode("200");
            return openAuthContext;
        }
    }

    @RequestMapping(value = "sendflowergratitude.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext sendFlowerGratitude(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        String activityDate = ConversionUtils.toString(openAuthContext.getParams().get("activityDate"));
        Long teacherId = ConversionUtils.toLong(openAuthContext.getParams().get("tid"));
        String content = ConversionUtils.toString(openAuthContext.getParams().get("content"));
        if (StringUtils.isBlank(activityDate) || StringUtils.isBlank(content) || teacherId == 0) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }

        try {
            if (badWordCheckerClient.containsConversationBadWord(content)) {
                openAuthContext.setCode("400");
                openAuthContext.setError("发送感谢失败！");
                return openAuthContext;
            }
            // 一天只能发送一次控制
            if (asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                    .TeacherFlowerGratitudeCacheManager_hasGratitude(teacherId)
                    .getUninterruptibly()) {
                openAuthContext.setCode("400");
                openAuthContext.setError("今天已经感谢过了");
                return openAuthContext;
            }
            MapMessage mapMessage = flowerServiceClient.getFlowerService()
                    .sendFlowerGratitude(teacherId, activityDate, content)
                    .getUninterruptibly();
            if (!mapMessage.isSuccess() && mapMessage.hasDuplicatedException()) {
                // 兼容原来的代码逻辑
                throw new DuplicatedOperationException();
            }

            asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                    .TeacherFlowerGratitudeCacheManager_gratitude(teacherId)
                    .awaitUninterruptibly();
            if (mapMessage.isSuccess()) {
                AlpsThreadPool.getInstance().submit(() -> {
                    // 包班制支持，实际上是感谢所有主副账号班级
                    Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
                    List<GroupTeacherMapper> groups = deprecatedGroupLoaderClient.loadTeacherGroups(relTeacherIds, false).values().stream().flatMap(Collection::stream).collect(Collectors.toList());
                    Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);

                    for (GroupTeacherMapper group : groups) {
                        Map<String, Object> extensionMap = new HashMap<>();
                        extensionMap.put("content", content);
                        extensionMap.put("activityDate", activityDate);
                        wechatServiceClient.processWechatNotice(
                                WechatNoticeProcessorType.FlowerGratitudeReceivedRemindNotice,
                                group.getClazzId(),
                                group.getId(),
                                teacher,
                                extensionMap,
                                WechatType.PARENT
                        );
                    }

                    // 发送家长消息
//                    parentMessageServiceClient.teacherFlowerGratitude(teacher.fetchRealname(), content, groups);
                });

                openAuthContext.setCode("200");
                return openAuthContext;
            } else {
                openAuthContext.setCode("400");
                openAuthContext.setError(mapMessage.getInfo());
                return openAuthContext;
            }
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                openAuthContext.setCode("400");
                openAuthContext.setError("请不要重复感谢！");
                return openAuthContext;
            }
            log.error("send flower gratitude failed.teacherId:{},activityDate:{},content:{}",
                    teacherId, activityDate, content, ex);
        }
        openAuthContext.setCode("400");
        openAuthContext.setError("发送感谢失败");
        return openAuthContext;
    }

    @RequestMapping(value = "getflowergratitude.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getFlowerGratitude(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long teacherId = ConversionUtils.toLong(openAuthContext.getParams().get("tid"));
        String activityDate = ConversionUtils.toString(openAuthContext.getParams().get("activityDate"));
        if (StringUtils.isBlank(activityDate) || teacherId == 0) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }

        FlowerGratitude flowerGratitude = flowerServiceClient.getFlowerService()
                .loadSenderFlowerGratitudes(teacherId)
                .getUninterruptibly()
                .stream()
                .filter(t -> StringUtils.equals(activityDate, t.getActivityDate()))
                .sorted((o1, o2) -> Long.compare(o2.fetchCreateTimestamp(), o1.fetchCreateTimestamp()))
                .findFirst()
                .orElse(null);
        boolean hasGratituded = asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                .TeacherFlowerGratitudeCacheManager_hasGratitude(teacherId)
                .getUninterruptibly();
        openAuthContext.setCode("200");
        openAuthContext.add("flowerGratitude", flowerGratitude);
        openAuthContext.add("canGratitude", (flowerGratitude == null && !hasGratituded));
        return openAuthContext;
    }

    @RequestMapping(value = "flowerrankbyparent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext flowerRankByParent(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long clazzId = SafeConverter.toLong(openAuthContext.getParams().get("cid"));
        long teacherId = getTeacherIdBySubject(openAuthContext);
        if (clazzId == 0 || teacherId == 0) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        List<Map<String, Object>> rankList = washingtonCacheSystem.CBS.flushable
                .wrapCache(businessStudentServiceClient)
                .expiration(1800)
                .keyPrefix("PARENT_FLOWER_RANK")
                .keys(teacherId, clazzId)
                .proxy()
                .findCurrentMonthFlowerRankByTeacherIdAndClazzId(teacherId, clazzId);
        openAuthContext.setCode("200");
        openAuthContext.add("rankList", rankList);
        return openAuthContext;
    }

    @RequestMapping(value = "flowerrankbyteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext flowerRankByTeacher(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long teacherId = getTeacherIdBySubject(openAuthContext);
        if (teacherId == 0) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacherDetail == null) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        List<Map<String, Object>> rankList = washingtonCacheSystem.CBS.flushable
                .wrapCache(businessStudentServiceClient)
                .expiration(1800)
                .keyPrefix("TEACHER_FLOWER_RANK")
                .keys(teacherDetail.getTeacherSchoolId(), teacherDetail.getSubject())
                .proxy()
                .findCurrentMonthFlowerRankBySchoolId(teacherDetail.getTeacherSchoolId(), teacherDetail.getSubject());
        // 上月排名
        boolean receivedFlag = asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                .TeacherFlowerRewardManager_hasReceivedIntegral(teacherId)
                .getUninterruptibly();
        int lastMonthRank = businessStudentServiceClient.findLastMonthFlowerRankInSchoolByTeacherId(teacherId);

        openAuthContext.setCode("200");
        openAuthContext.add("rankList", rankList);
        openAuthContext.add("receivedFlag", receivedFlag);
        openAuthContext.add("lastMonthRank", lastMonthRank);
        return openAuthContext;
    }

    @RequestMapping(value = "receiveflowerrankreward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext receiveFlowerRankReward(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long teacherId = ConversionUtils.toLong(openAuthContext.getParams().get("tid"));
        if (teacherId == 0) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }

        boolean receivedFlag = asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                .TeacherFlowerRewardManager_hasReceivedIntegral(teacherId)
                .getUninterruptibly();
        if (receivedFlag) {
            openAuthContext.setCode("400");
            openAuthContext.setError("已经领取过奖励，请勿重复领取。");
            return openAuthContext;
        }
        IntegralHistory integralHistory = new IntegralHistory(teacherId, IntegralType.老师鲜花排名奖励_产品平台, 1000);
        integralHistory.setComment("老师鲜花排名奖励");
        userIntegralService.changeIntegral(integralHistory);

        asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                .TeacherFlowerRewardManager_receiveReward(teacherId)
                .awaitUninterruptibly();

        openAuthContext.setCode("200");
        return openAuthContext;
    }

    // 获取班级鲜花兑换信息
    @RequestMapping(value = "exchangeinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext exchangeInfo(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long userId = SafeConverter.toLong(openAuthContext.getParams().get("uid"), Long.MIN_VALUE);
        String subject = SafeConverter.toString(openAuthContext.getParams().get("subject"));
        long teacherId = teacherLoaderClient.loadRelTeacherIdBySubject(userId, Subject.of(subject));
        long clazzId = SafeConverter.toLong(openAuthContext.getParams().get("cid"), Long.MIN_VALUE);
        if (teacherId == Long.MIN_VALUE || clazzId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }
        User teacher = raikouSystem.loadUser(teacherId);
        if (teacher == null) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }
        // 获取当前老师 当前班级 本月的鲜花总数
        List<Flower> flowers = flowerServiceClient.getFlowerService()
                .loadClazzFlowers(clazzId)
                .getUninterruptibly();
        flowers = flowers.stream().filter(f -> f.getReceiverId() != null && Objects.equals(f.getReceiverId(), teacherId))
                .filter(f -> MonthRange.current().contains(f.getCreateDatetime())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(flowers)) {
            openAuthContext.add("flowerCount", 0);
            openAuthContext.add("exchangeCount", 0);
            openAuthContext.setCode("200");
            return openAuthContext;
        }
        // 获取本月当前老师 当前班级 已经兑换的次数
        List<FlowerExchangeHistory> histories = flowerServiceClient.getFlowerService()
                .loadTeacherFlowerExchangeHistories(teacherId)
                .getUninterruptibly();
        histories = histories.stream().filter(h -> MonthRange.current().contains(h.getCreateDatetime()))
                .filter(h -> Objects.equals(clazzId, h.getClazzId())).collect(Collectors.toList());
        int flowerCount = flowers.size() - (histories.size() * 10);
        int exchangeCount = flowerCount == 0 ? 0 : flowerCount / 10;
        openAuthContext.add("flowerCount", flowerCount);
        openAuthContext.add("exchangeCount", exchangeCount);
        openAuthContext.setCode("200");
        return openAuthContext;
    }

    // 执行兑换
    @RequestMapping(value = "flowerexchange.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext flowerExchange(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long userId = SafeConverter.toLong(openAuthContext.getParams().get("uid"), Long.MIN_VALUE);
        String subject = SafeConverter.toString(openAuthContext.getParams().get("subject"));
        long teacherId = teacherLoaderClient.loadRelTeacherIdBySubject(userId, Subject.of(subject));
        long clazzId = SafeConverter.toLong(openAuthContext.getParams().get("cid"), Long.MIN_VALUE);
        if (teacherId == Long.MIN_VALUE || clazzId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }
        User teacher = raikouSystem.loadUser(teacherId);
        if (teacher == null) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }
        try {
            AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
            MapMessage message = builder.keyPrefix("TEACHER_EXCHANGE_FLOWER")
                    .keys(teacherId)
                    .expirationInSeconds(30)
                    .callback(() -> flowerServiceClient.getFlowerService()
                            .flowerExchange(teacherId, clazzId, FlowerExchangeHistory.Source.WECHAT.name())
                            .getUninterruptibly())
                    .build()
                    .execute();
            if (message.isSuccess()) {
                openAuthContext.setCode("200");
                openAuthContext.add("integral", SafeConverter.toInt(message.get("integral")));
                return openAuthContext;
            } else {
                openAuthContext.setCode("400");
                openAuthContext.setError(message.getInfo());
                return openAuthContext;
            }
        } catch (DuplicatedOperationException ex) {
            openAuthContext.setCode("400");
            openAuthContext.setError("您点击太快了，请重试");
            return openAuthContext;
        }
    }
}
