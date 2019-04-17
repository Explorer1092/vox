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

package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilder;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.business.client.AsyncBusinessCacheServiceClient;
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Flower;
import com.voxlearning.utopia.service.user.api.entities.FlowerExchangeHistory;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Shuai Huan
 * @since 2015/6/4.
 */
@Controller
@RequestMapping("/teacher/flower")
public class TeacherFlowerController extends AbstractTeacherController {

    @Inject private RaikouSDK raikouSDK;

    @Inject private AsyncBusinessCacheServiceClient asyncBusinessCacheServiceClient;
    @Inject private FlowerServiceClient flowerServiceClient;

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String flowerList(Model model) {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return "redirect:/teacher/index.vpage";
        }

        //load ClazzIdList
        List<Long> classIdList = flowerServiceClient.getFlowerService()
                .loadReceiverFlowers(teacher.getId())
                .getUninterruptibly()
                .stream()
                .filter(t -> MonthRange.current().contains(t.fetchCreateTimestamp()))
                .filter(t -> t.getClazzId() != null)
                .map(Flower::getClazzId)
                .distinct()
                .collect(Collectors.toList());
        //load ClazzList
        Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(classIdList)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        List<Clazz> clazzList = new LinkedList<>();
        for (Clazz clazz : clazzMap.values()) {
            clazzList.add(clazz);
        }
        model.addAttribute("clazzList", clazzList.stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .collect(Collectors.toList()));

        //多学科支持
        model.addAttribute("curSubject", teacher.getSubject());
        model.addAttribute("curSubjectText", teacher.getSubject().getValue());

        return "teacherv3/flower/list";
    }

    @RequestMapping(value = "flowerrankbyparent.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage flowerRankByParent() {
        try {
            Long clazzId = ConversionUtils.toLong(getRequest().getParameter("clazzId"));
            List<Map<String, Object>> rankList = washingtonCacheSystem.CBS.flushable
                    .wrapCache(businessStudentServiceClient)
                    .expiration(1800)
                    .keyPrefix("PARENT_FLOWER_RANK")
                    .keys(getSubjectSpecifiedTeacherId(), clazzId)
                    .proxy()
                    .findCurrentMonthFlowerRankByTeacherIdAndClazzId(getSubjectSpecifiedTeacherId(), clazzId);
            return MapMessage.successMessage().add("rankList", rankList);
        } catch (Exception ex) {
            logger.error(getClass().getName() + ex.getMessage(), ex);
            return MapMessage.errorMessage("获取家长送花列表失败，请稍后再试");
        }
    }

    @RequestMapping(value = "flowerrankbyteacher.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage flowerRankByTeacher() {
        try {
            TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
            List<Map<String, Object>> rankList = washingtonCacheSystem.CBS.flushable
                    .wrapCache(businessStudentServiceClient)
                    .expiration(1800)
                    .keyPrefix("TEACHER_FLOWER_RANK")
                    .keys(teacherDetail.getTeacherSchoolId(), teacherDetail.getSubject())
                    .proxy()
                    .findCurrentMonthFlowerRankBySchoolId(teacherDetail.getTeacherSchoolId(), teacherDetail.getSubject());
            boolean receivedFlag = asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                    .TeacherFlowerRewardManager_hasReceivedIntegral(teacherDetail.getId())
                    .getUninterruptibly();
            int lastMonthRank = businessStudentServiceClient.findLastMonthFlowerRankInSchoolByTeacherId(teacherDetail.getId());
            return MapMessage.successMessage().add("rankList", rankList).add("lastMonthRank", lastMonthRank).add("receivedFlag", receivedFlag);
        } catch (Exception ex) {
            logger.error(getClass().getName() + ex.getMessage(), ex);
            return MapMessage.errorMessage("获取老师收花列表失败，请稍后再试");
        }
    }

    @RequestMapping(value = "sendflowergratitude.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendFlowerGratitude() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        String activityDate = getRequestString("activityDate");
        String content = getRequestString("content");

        if (StringUtils.isBlank(activityDate) || StringUtils.isBlank(content)) {
            return MapMessage.errorMessage("发送感谢失败！");
        }

        if (badWordCheckerClient.containsConversationBadWord(content)) {
            return MapMessage.errorMessage("发送感谢失败！");
        }
        // 一天只能发送一次控制
        if (asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                .TeacherFlowerGratitudeCacheManager_hasGratitude(teacher.getId())
                .getUninterruptibly()) {
            return MapMessage.errorMessage("今天已经感谢过了！");
        }

        try {
            MapMessage mapMessage = flowerServiceClient.getFlowerService()
                    .sendFlowerGratitude(teacher.getId(), activityDate, content)
                    .getUninterruptibly();
            if (!mapMessage.isSuccess()) {
                if (mapMessage.hasDuplicatedException()) {
                    throw new DuplicatedOperationException();
                }
                return MapMessage.errorMessage("发送感谢失败！");
            }
            asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                    .TeacherFlowerGratitudeCacheManager_gratitude(teacher.getId())
                    .awaitUninterruptibly();

            AlpsThreadPool.getInstance().submit(() -> {
                // 包班制支持，实际上是感谢所有主副账号班级
                Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
                List<GroupTeacherMapper> groups = deprecatedGroupLoaderClient.loadTeacherGroups(relTeacherIds, false).values().stream().flatMap(Collection::stream).collect(Collectors.toList());

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
                // 发送消息中心的消息
//                parentMessageServiceClient.teacherFlowerGratitude(teacher.fetchRealname(), content, groups);
            });
            return MapMessage.successMessage();
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.errorMessage("请不要重复感谢！");
            }
            logger.error("send flower gratitude failed.teacherId:{},activityDate:{},content:{}",
                    teacher.getId(), activityDate, content, ex);
        }
        return MapMessage.errorMessage("发送感谢失败");
    }


    /**
     * 鲜花兑换班级学豆页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "exchange.vpage", method = RequestMethod.GET)
    public String exchange(Model model) {
        Teacher teacher = getSubjectSpecifiedTeacher();
        // 没有班级的校验
        List<Clazz> clazzList = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId());
        if (CollectionUtils.isEmpty(clazzList)) {
            return "redirect:/teacher/index.vpage";
        }
        model.addAttribute("clazzList", clazzList.stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .collect(Collectors.toList()));
        //多学科支持
        model.addAttribute("curSubject", teacher.getSubject());
        model.addAttribute("curSubjectText", teacher.getSubject().getValue());
        // 查出本月有多少家长赠送鲜花
        MonthRange range = MonthRange.current();
        long senderCnt = flowerServiceClient.getFlowerService()
                .loadReceiverFlowers(teacher.getId())
                .getUninterruptibly()
                .stream()
                .filter(f -> f.getSenderId() != null)
                .filter(t -> range.contains(t.fetchCreateTimestamp()))
                .map(Flower::getSenderId)
                .distinct().count();
        model.addAttribute("senderCnt", senderCnt);
        return "teacherv3/flower/exchange";
    }

    @RequestMapping(value = "exchangeinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage exchangeInfo() {
        try {
            Long clazzId = getRequestLong("clazzId");
            Long userId = getSubjectSpecifiedTeacherId();
            if (clazzId == 0 || userId == null) {
                return MapMessage.errorMessage();
            }
            // 获取当前老师 当前班级 本月的鲜花总数
            List<Flower> flowers = flowerServiceClient.getFlowerService()
                    .loadClazzFlowers(clazzId)
                    .getUninterruptibly();
            flowers = flowers.stream().filter(f -> f.getReceiverId() != null && Objects.equals(f.getReceiverId(), userId))
                    .filter(f -> MonthRange.current().contains(f.getCreateDatetime())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(flowers)) {
                return MapMessage.successMessage().add("flowerCount", 0).add("exchangeCount", 0);
            }
            // 获取本月当前老师 当前班级 已经兑换的次数
            List<FlowerExchangeHistory> histories = flowerServiceClient.getFlowerService()
                    .loadTeacherFlowerExchangeHistories(userId)
                    .getUninterruptibly();
            histories = histories.stream().filter(h -> MonthRange.current().contains(h.getCreateDatetime()))
                    .filter(h -> Objects.equals(clazzId, h.getClazzId())).collect(Collectors.toList());
            int flowerCount = flowers.size() - (histories.size() * 10);
            int exchangeCount = flowerCount == 0 ? 0 : flowerCount / 10;
            return MapMessage.successMessage().add("flowerCount", flowerCount).add("exchangeCount", exchangeCount);
        } catch (Exception ex) {
            logger.error(getClass().getName() + ex.getMessage(), ex);
            return MapMessage.errorMessage("获取信息失败，请稍后再试");
        }
    }

    // 执行兑换
    @RequestMapping(value = "flowerexchange.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage flowerExchange() {
        Long clazzId = getRequestLong("clazzId");
        Long userId = getSubjectSpecifiedTeacherId();
        if (clazzId == 0 || userId == null) {
            return MapMessage.errorMessage();
        }
        try {
            AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
            return builder.keyPrefix("TEACHER_EXCHANGE_FLOWER")
                    .expirationInSeconds(30)
                    .keys(userId)
                    .callback(() -> flowerServiceClient.getFlowerService()
                            .flowerExchange(userId, clazzId, FlowerExchangeHistory.Source.PC.name())
                            .getUninterruptibly())
                    .build()
                    .execute();
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }
}
