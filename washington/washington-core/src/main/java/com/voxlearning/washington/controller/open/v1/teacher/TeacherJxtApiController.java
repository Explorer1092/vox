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

package com.voxlearning.washington.controller.open.v1.teacher;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.parent.api.DPScoreCircleLoader;
import com.voxlearning.utopia.service.parent.api.mapper.scorecircle.ScoreCircleGroupContext;
import com.voxlearning.utopia.service.parent.api.mapper.scorecircle.ScoreCircleGroupContextStoreInfo;
import com.voxlearning.utopia.service.parent.api.mapper.scorecircle.ScoreCircleResponse;
import com.voxlearning.utopia.service.parent.constant.GroupCircleType;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNotice;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author shiwe.liao
 * @since 2016/4/26
 */
@Controller
@Slf4j
@RequestMapping(value = "/v1/teacher/jxt/")
public class TeacherJxtApiController extends AbstractTeacherApiController {

    @Inject private RaikouSDK raikouSDK;

    @Inject
    private ClazzIntegralServiceClient clazzIntegralServiceClient;

    @ImportService(interfaceClass = DPScoreCircleLoader.class)
    private DPScoreCircleLoader dpScoreCircleLoader;

    @RequestMapping(value = "/index.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage teacherJxtIndex() {
        return failMessage(RES_RESULT_ERROR_NEED_UPGRADE);
    }


    @RequestMapping(value = "/clazz_list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getTeacherClazzList() {
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        Long teacherId = getApiRequestUser().getId();
        //都要支持包班制 老师子账号也要查出来。。。。
        Set<Long> relTeacherIdSet = teacherLoaderClient.loadRelTeacherIds(teacherId);
        //老师所有group
        Map<Long, GroupTeacherMapper> teacherGroupMapperMap = new HashMap<>();
        Map<Long, List<GroupTeacherMapper>> teacherGroups = deprecatedGroupLoaderClient.loadTeacherGroups(relTeacherIdSet, false);
        teacherGroups.forEach((tid, mapperList) -> {
            mapperList.stream().filter(t -> t.isTeacherGroupRefStatusValid(tid)).forEach(t -> teacherGroupMapperMap.put(t.getId(), t));
        });
        //按clazzId聚合
        Map<Long, List<GroupTeacherMapper>> clazzGroupTeacherMaps = teacherGroups.values().stream().flatMap(Collection::stream).collect(Collectors.groupingBy(GroupTeacherMapper::getClazzId));
        //groupId-clazzId
        Map<Long, Long> groupClazzIds = teacherGroupMapperMap.values().stream().collect(Collectors.toMap(GroupTeacherMapper::getId, GroupTeacherMapper::getClazzId));
        Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(groupClazzIds.values())
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));

        MapMessage message = successMessage();
        //置顶通知 start
        String content = "向班群发布班务、任务通知，跟踪家长反馈";
        boolean isDefault = true;
        List<JxtNotice> noticeList = jxtLoaderClient.getJxtNoticeListByTeacherIds(relTeacherIdSet).values().stream().flatMap(Collection::stream).filter(p -> p.getExpireTime() != null).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(noticeList)) {
            if (noticeList.stream().anyMatch(p -> p.getExpireTime().after(new Date()) && DateUtils.dayDiff(p.getExpireTime(), new Date()) == 0)) {
                //未过期且24小时内到期的
                content = noticeList.stream().filter(p -> p.getExpireTime().after(new Date()) && DateUtils.dayDiff(p.getExpireTime(), new Date()) == 0).count() + "个通知1天后到期，你可以去已发布通知中提醒家长";
                isDefault = false;
            } else if (noticeList.stream().anyMatch(p -> p.getExpireTime().after(new Date()))) {
                //未过期的
                content = noticeList.stream().filter(p -> p.getExpireTime().after(new Date())).count() + "个通知正在进行中，去已发布通知中查看进度";
                isDefault = false;
            }
        }
        message.add(RES_RESULT_TEACHER_TOP_NOTICE_IS_DEFAULT, isDefault);
        message.add(RES_RESULT_TOP_NOTICE_CONTENT, content);
        message.add(RES_FLOWER_URL, fetchMainsiteUrlByCurrentSchema() + flowerUrl);
        message.add(RES_INTEGRAL_URL, fetchMainsiteUrlByCurrentSchema() + integralUrl);
        //置顶通知 end

        //学豆数量
        Set<Long> groupIds = teacherGroups.values().stream().flatMap(Collection::stream).map(GroupTeacherMapper::getId).collect(Collectors.toSet());
        AtomicInteger integralCounter = new AtomicInteger(0);
        clazzIntegralServiceClient.getClazzIntegralService()
                .loadClazzIntegralPools(groupIds.stream().filter(t -> {
                    GroupTeacherMapper groupTeacherMapper = teacherGroupMapperMap.get(t);
                    if (groupTeacherMapper == null)
                        return false;
                    if (relTeacherIdSet.stream().noneMatch(groupTeacherMapper::isTeacherGroupRefStatusValid))
                        return false;
                    Clazz clazz = clazzMap.get(groupTeacherMapper.getClazzId());
                    if (clazz == null)
                        return false;
                    if (clazz.isTerminalClazz())
                        return false;
                    return true;
                }).collect(Collectors.toSet()))
                .getUninterruptibly().values().forEach(t -> integralCounter.addAndGet(t.fetchTotalIntegral()));
        message.add(RES_TOTAL_INTEGRAL, integralCounter.get());


        //按年级和名字排序
        List<Long> clazzIdList = clazzMap.values()
                .stream()
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .map(Clazz::getId)
                .collect(Collectors.toList());
        //根据group所在的年级对group做排序
        List<Long> sortedGroupIds = groupIds.stream().sorted((o1, o2) -> clazzIdList.indexOf(groupClazzIds.get(o1)) - clazzIdList.indexOf(groupClazzIds.get(o2))).collect(Collectors.toList());
        //每个组的共享组
        Map<Long, Set<Long>> sharedGroupIdMaps = deprecatedGroupLoaderClient.loadSharedGroupIds(groupIds);
        List<Map<String, Object>> mapList = new ArrayList<>();

        Set<Long> hadReturnGroupIds = new HashSet<>();
        for (Long groupId : sortedGroupIds) {
            //已处理过的共享组
            if (hadReturnGroupIds.contains(groupId)) {
                continue;
            }
            Long clazzId = groupClazzIds.get(groupId);
            Clazz clazz = clazzMap.get(clazzId);
            if (clazz == null) {
                continue;
            }
            if (clazz.isTerminalClazz()) {
                continue;
            }
            //共享组
            Set<Long> sharedGroupIds = sharedGroupIdMaps.get(groupId);
            if (CollectionUtils.isEmpty(sharedGroupIds)) {
                sharedGroupIds = new HashSet<>();
            }
            sharedGroupIds.add(groupId);
            //查出有关联的老师
            List<GroupTeacherMapper> teacherMapperList = clazzGroupTeacherMaps.get(clazzId);
            if (CollectionUtils.isEmpty(teacherMapperList)) {
                continue;
            }
            final Set<Long> shared = sharedGroupIds;
            teacherMapperList = teacherMapperList.stream().filter(p -> shared.contains(p.getId())).collect(Collectors.toList());

            Map<String, Object> map = new HashMap<>();
            map.put(RES_GROUP_ID, groupId);
            map.put(RES_CLAZZ_ID, clazz.getId());
            map.put(RES_CLAZZ_NAME, clazz.formalizeClazzName());
            //最新一条消息
            ScoreCircleGroupContext context = getLatestContext(shared, new Date());
            if (context != null) {
                String id = ScoreCircleGroupContextStoreInfo.generateId(context.getGroupCircleType(), context.getTypeId(), context.getGroupId());
                ScoreCircleGroupContextStoreInfo storeInfo = dpScoreCircleLoader.loads(Collections.singleton(id)).get(id);
                if (storeInfo == null) {
                    continue;
                }
                String extContent;
                String sys = getRequestString(REQ_SYS);
                if ("android".equals(sys)) {
                    extContent = "<font color='#4A5060' size='16px'>[" + storeInfo.getGroupCircleType().getLeftTopTag() + "]</font><font color='#878E9F' size='16px'>" + storeInfo.getContent() + "</font>";
                } else {
                    extContent = "<span style='color:#4A5060;font-size:16px;'>[" + storeInfo.getGroupCircleType().getLeftTopTag() + "]<span style='color:#878E9F;font-size:16px;'>" + storeInfo.getContent();
                }
                map.put(RES_CLAZZ_EXT_INFO, extContent);
                map.put(RES_CLAZZ_EXT_TITLE, storeInfo.getGroupCircleType().getLeftTopTag());
                String clazzExtContent = storeInfo.getContent().replace("<br>", "");
                if (storeInfo.getGroupCircleType() == GroupCircleType.HOMEWORK_NEW) {
                    NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(storeInfo.getTypeId());
                    if (newHomework != null && newHomework.getNewHomeworkType() == NewHomeworkType.OCR) {
                        Map<ObjectiveConfigType, NewHomeworkPracticeContent> newHomeworkPracticeContents = newHomework.findPracticeContents();
                        if (newHomeworkPracticeContents.containsKey(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC)) {
                            NewHomeworkPracticeContent newHomeworkPracticeContent = newHomeworkPracticeContents.get(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC);
                            String workBookName = newHomeworkPracticeContent.getWorkBookName();
                            String homeworkDetail = newHomeworkPracticeContent.getHomeworkDetail();
                            String[] bookNames = StringUtils.split(workBookName, NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_SEPARATOR);
                            String[] homeworkDetails = StringUtils.split(homeworkDetail, NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_SEPARATOR);
                            List<String> bookNameList = Arrays.asList(bookNames);
                            List<String> homeworkDetailList = Arrays.asList(homeworkDetails);
                            int length = Integer.max(bookNameList.size(), homeworkDetailList.size());
                            List<String> homeworkContent = new ArrayList<>();
                            for (int i = 0; i < length; i++) {
                                homeworkContent.add(bookNameList.get(i) + "：" + homeworkDetailList.get(i));
                            }
                            clazzExtContent = StringUtils.join(homeworkContent, ";");
                        }
                    }
                }
                map.put(RES_CLAZZ_EXT_CONTENT, clazzExtContent);
            }
            map.put(RES_CLAZZ_SUBJECT_LIST, teacherMapperList.stream().map(GroupTeacherMapper::getSubject).sorted(Comparator.comparingInt(Subject::getKey)).collect(Collectors.toList()));
            map.put(RES_CLAZZ_FUNCTION_LIST, clazzFunctionList());
            mapList.add(map);
            hadReturnGroupIds.addAll(sharedGroupIds);
        }
        return message.add(RES_CLAZZ_LIST, mapList);
    }

    private ScoreCircleGroupContext getLatestContext(Collection<Long> groupIds, Date createDate) {
        Set<ScoreCircleGroupContext> contextSet = new HashSet<>();
        //时间处理
        Date current = new Date();
        //最多取两个月内的数据
        Date oldestDate = DateUtils.addDays(current, -60);
        //本月
        MonthRange monthRange = MonthRange.newInstance(createDate.getTime());
        ScoreCircleGroupContext context = new ScoreCircleGroupContext();
        do {
            context.setCreateDate(createDate);
            for (Long id : groupIds) {
                context.setGroupId(id);
                ScoreCircleResponse circleResponse = dpScoreCircleLoader.loadGroupCircleByScoreDesc(context, 1);
                if (circleResponse.isSuccess()) {
                    circleResponse.getContextList()
                            .stream()
                            .filter(e -> e instanceof ScoreCircleGroupContext)
                            .map(e -> (ScoreCircleGroupContext) e)
                            .filter(p -> !p.getCreateDate().before(oldestDate))
                            .forEach(contextSet::add);
                }
                //忽略学科的消息中不要通用消息
                ScoreCircleResponse ignoreSubjectCircleResponse = dpScoreCircleLoader.loadGroupIgnoreSubjectCircleByScoreDesc(context, 10);
                if (ignoreSubjectCircleResponse.isSuccess()) {
                    ignoreSubjectCircleResponse.getContextList()
                            .stream()
                            .filter(e -> e instanceof ScoreCircleGroupContext)
                            .map(e -> (ScoreCircleGroupContext) e)
                            .filter(p -> p.getGroupCircleType() != GroupCircleType.COMMON)
                            .filter(p -> !p.getCreateDate().before(oldestDate))
                            .forEach(contextSet::add);
                }
            }
            //获取记录条数>=10跳出循环
            if (contextSet.size() >= 1) {
                break;
            }
            //重新计算时间
            monthRange = monthRange.previous();
            createDate = monthRange.getEndDate();
        } while (!oldestDate.after(createDate));
        return contextSet.stream().sorted((o1, o2) -> o2.getCreateDate().compareTo(o1.getCreateDate())).findFirst().orElse(null);
    }

    //目前所有班级都是 班级管理,课堂奖励,邀请家长3个,固定。
    private List<FunctionEntry> clazzFunctionList() {
        FunctionEntry clazzManagement = new FunctionEntry(0, "班级管理", FunctionType.NATIVE, NativeFunctionKey.CLAZZ_MANAGEMENT.name());
        FunctionEntry clazzReward = new FunctionEntry(1, "课堂奖励", FunctionType.NATIVE, NativeFunctionKey.CLAZZ_REWARD.name());
        FunctionEntry inviteParent = new FunctionEntry(2, "邀请家长", FunctionType.H5, fetchMainsiteUrlByCurrentSchema() + "/view/mobile/parent/static_page/open_app_by_teacher.vpage");
        return Arrays.asList(clazzManagement, clazzReward, inviteParent);
    }

    @Setter
    @Getter
    @AllArgsConstructor
    private static class FunctionEntry {
        private Integer index;
        private String text;
        @JsonProperty("function_type")
        private FunctionType functionType;
        @JsonProperty("function_key")
        private String functionKey;
    }

    private enum FunctionType {
        H5,
        NATIVE;
    }

    private enum NativeFunctionKey {
        CLAZZ_MANAGEMENT,
        CLAZZ_REWARD;
    }
}
