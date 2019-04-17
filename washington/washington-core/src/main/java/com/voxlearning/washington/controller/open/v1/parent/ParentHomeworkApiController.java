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

package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.api.concurrent.AlpsFutureMap;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.ScoreLevel;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.constant.StudentHomeworkStatus;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkFinishRewardInParentApp;
import com.voxlearning.utopia.service.newhomework.api.entity.OfflineHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.*;
import com.voxlearning.utopia.service.newhomework.api.util.OfflineHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkCacheServiceClient;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.Flower;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.vendor.api.constant.JxtNoticeType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentHomeWorkReportTab;
import com.voxlearning.utopia.service.vendor.api.entity.JxtFeedBack;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNotice;
import com.voxlearning.utopia.service.vendor.api.entity.OfflineHomeworkSignRecord;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author Hailong Yang
 * @version 0.1
 * @since 2015/10/22
 */
@Controller
@RequestMapping(value = "/v1/parent/homework")
@Slf4j
public class ParentHomeworkApiController extends AbstractParentApiController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private FlowerServiceClient flowerServiceClient;
    @Inject
    private NewHomeworkCacheServiceClient newHomeworkCacheServiceClient;

    /**
     * 跟读点赞
     */
    @RequestMapping(value = "/praise.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage praise() {
        return failMessage("本次点赞已过期，请关注下次推荐吧");
    }


    @RequestMapping(value = "/homeworksDynamicStateCount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getHomeworksDynamicStateCount() {
        long studentId = getRequestLong(REQ_STUDENT_ID);
        if (studentId <= 0) {
            return failMessage(MessageFormat.format(RES_RESULT_STUDENT_ID_ERROR_MSG, studentId));
        }
        User parent = getCurrentParent();
        if (parent == null) {
            return failMessage("请登录家长号");
        }
        User student = raikouSystem.loadUser(studentId);
        if (student == null || !student.isStudent()) {
            return failMessage("学生账号【" + studentId + "】不存在");
        }

        List<StudentParentRef> refList = parentLoaderClient.loadParentStudentRefs(parent.getId());
        if (CollectionUtils.isEmpty(refList)) {
            return failMessage("当前家长未关联任何学生");
        }
        boolean parentStudentMatch = refList.stream().anyMatch(e -> Objects.equals(e.getStudentId(), studentId));
        if (!parentStudentMatch) {
            return failMessage("当前家长未关联学生" + student.fetchRealname());
        }

        Long createTime = getRequestLong(REQ_PARENT_LATEST_TIME);

        Set<Long> groupIds = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false).stream()
                .filter(e -> e != null && e.getId() != null)
                .map(GroupMapper::getId)
                .collect(Collectors.toSet());

        //查询作业
        Date endDate = new Date();
        Date startDate = createTime != 0 ? new Date(createTime) : DateUtils.calculateDateDay(endDate, -14);
        //分页查。最大去50个就行了。再多也没意义了。
        List<NewHomework.Location> locations = newHomeworkPartLoaderClient.loadNewHomeworkByClazzGroupId(groupIds, startDate, endDate);
        return successMessage("count", locations.size());
    }


    /**
     * 作业动态详情
     */
    @RequestMapping(value = "/homeworksDynamicState.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage homeworksDynamicState() {
        Long userId = getRequestLong(REQ_STUDENT_ID);
        Long createTime = getRequestLong("time") <= 0 ? null : getRequestLong("time");
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        PageRequest pageable = new PageRequest(0, 6);
        //这里为了优化布置作业Push的消息打开作业动态时没有studentId。只有teacherId
        //所以sid参数传的实际上是teacherId。同时兼容延迟的消息。也可能是studentId
        //在try里面处理了。
        Long studentId = userId;
        try {
            validateRequired(REQ_STUDENT_ID, "学生编号");
            validateRequest(REQ_STUDENT_ID, "time");
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }

        try {

            if (userId <= 0) {
                return failMessage(MessageFormat.format(RES_RESULT_STUDENT_ID_ERROR_MSG, userId));
            }

            if (getCurrentParent() == null) {
                return failMessage("请登录家长号");
            }

            User parent = getCurrentParent();
            User user = raikouSystem.loadUser(userId);
            if (user == null) {
                return failMessage(MessageFormat.format(RES_RESULT_STUDENT_ID_ERROR_MSG, userId));
            }
            //下面这都是为了处理消息兼容性的问题。
            if (UserType.TEACHER.getType() == user.getUserType()) {
                List<User> students = studentLoaderClient.loadParentStudents(parent.getId());
                Set<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toSet());
                Map<Long, List<GroupMapper>> studentGroupMap = deprecatedGroupLoaderClient.loadStudentGroups(studentIds, false);
                List<GroupTeacherMapper> groupTeacherMapperList = deprecatedGroupLoaderClient.loadTeacherGroups(userId, false);
                if (MapUtils.isEmpty(studentGroupMap) || CollectionUtils.isEmpty(groupTeacherMapperList)) {
                    return failMessage(MessageFormat.format(RES_RESULT_STUDENT_ID_ERROR_MSG, userId));
                }
                Set<Long> teacherGroupIds = groupTeacherMapperList.stream().map(GroupTeacherMapper::getId).collect(Collectors.toSet());
                boolean isHit = false;
                for (Long sid : studentGroupMap.keySet()) {
                    if (!isHit) {
                        List<GroupMapper> studentGroups = studentGroupMap.get(sid);
                        if (CollectionUtils.isNotEmpty(studentGroups)) {
                            for (GroupMapper mapper : studentGroups) {
                                if (teacherGroupIds.contains(mapper.getId())) {
                                    studentId = sid;
                                    isHit = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);

            if (studentDetail == null) {
                return failMessage("学生账号【" + studentId + "】不存在");
            }

            //查询作业
            Date endDate = createTime != null ? new Date(createTime) : new Date();
            Date startDate = DateUtils.calculateDateDay(new Date(), -60);

            List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentDetail.getId(), false);
            Set<Long> groupIds = GroupMapper.filter(groupMappers).idSet();

            //日常作业
            List<NewHomework.Location> totalLocations = newHomeworkPartLoaderClient.loadNewHomeworkByClazzGroupId(groupIds, startDate, endDate);
            Page<NewHomework.Location> locationPage = PageableUtils.listToPage(totalLocations, pageable);

            List<NewHomework.Location> newHomeworkLocations = new ArrayList<>(locationPage.getContent());

            List<OfflineHomework> offlineHomeworkList = offlineHomeworkLoaderClient.loadGroupOfflineHomeworks(groupIds).values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(p -> p.getCreateAt() != null && p.getCreateAt().after(startDate) && p.getCreateAt().before(endDate))
                    .sorted((o1, o2) -> o2.getCreateAt().compareTo(o1.getCreateAt()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(offlineHomeworkList)) {
                offlineHomeworkList = offlineHomeworkList.subList(0, offlineHomeworkList.size() > 6 ? 6 : offlineHomeworkList.size());
            }

            //新版家校通的老师通知
            List<JxtNotice> noticeList;
            Map<Long, List<Teacher>> teacherListByGroupIds = teacherLoaderClient.loadGroupTeacher(groupIds);
            Set<Long> groupTeacherIds = teacherListByGroupIds.values().stream().flatMap(Collection::stream).map(Teacher::getId).collect(Collectors.toSet());
            Map<Long, Long> teacherIdMap = teacherLoaderClient.loadMainTeacherIds(groupTeacherIds);
            groupTeacherIds.addAll(teacherIdMap.values());
            List<JxtNotice> jxtNoticeList = jxtLoaderClient.getJxtNoticeListByTeacherIds(groupTeacherIds).values().stream().flatMap(Collection::stream).collect(Collectors.toList());
            List<JxtNotice> groupNoticeList = new ArrayList<>();
            for (Long groupId : groupIds) {
                groupNoticeList.addAll(
                        jxtNoticeList.stream()
                                .filter(p -> p.getNoticeType() == JxtNoticeType.ClAZZ_AFFAIR.getType())
                                .filter(p -> p.getGroupIds().contains(groupId))
                                .filter(p -> p.getExpireTime() != null)
                                .collect(Collectors.toList())
                );
            }
            noticeList = groupNoticeList
                    .stream()
                    .filter(p -> p.getCreateTime().compareTo(startDate) > 0 && p.getCreateTime().compareTo(endDate) < 0)
                    .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(noticeList)) {
                noticeList = noticeList.subList(0, noticeList.size() > 6 ? 6 : noticeList.size());
            }
            //把作业、作业单、通知排序取最新的6个
            List<Map<String, Object>> returnList = getReturnList(newHomeworkLocations, noticeList, offlineHomeworkList);

            List<Map<String, Object>> resultList = new ArrayList<>();
            //1、作业转化为卡片
            List<NewHomework.Location> newHomeworkList = new ArrayList<>();
            returnList.stream()
                    .filter(p -> HomeWorkDynamicType.NEW_HOMEWORK == p.get("type"))
                    .forEach(p -> newHomeworkList.add((NewHomework.Location) p.get("ob")));

            generateTotalHomeworkInfo(newHomeworkList, studentDetail, ver, resultList);
            //2、通知转化为卡片
            List<JxtNotice> returnNoticeList = new ArrayList<>();
            returnList.stream()
                    .filter(p -> HomeWorkDynamicType.JXT_NOTICE == p.get("type"))
                    .forEach(p -> returnNoticeList.add((JxtNotice) p.get("ob")));

            generateJxtNoticeInfo(returnNoticeList, studentDetail, groupIds, parent, ver, resultList);
            //3、作业单转化为卡片
            List<OfflineHomework> returnOfflineHomeworkList = new ArrayList<>();
            returnList.stream()
                    .filter(p -> HomeWorkDynamicType.OFFLINE_HOMEWORK == p.get("type"))
                    .forEach(p -> returnOfflineHomeworkList.add((OfflineHomework) p.get("ob")));
            generateOfflineHomeworkInfo(returnOfflineHomeworkList, parent, studentDetail, resultList, ver);

            //重新做一遍排序
            resultList = resultList.stream()
                    .filter(m -> createTime == null || (Long) ((Map) m.get("info")).get("createTime") < createTime)
                    .sorted((m1, m2) -> {
                        long o1CreateTime = (Long) ((Map) m1.get("info")).get("createTime");
                        long o2CreateTime = (Long) ((Map) m2.get("info")).get("createTime");
                        return o1CreateTime < o2CreateTime ? 1 : -1;
                    })
                    .collect(Collectors.toList());
            //生成时间轴
            String today = DateUtils.dateToString(DayRange.current().getStartDate(), "MM月dd日");
            String yesterday = DateUtils.dateToString(DayRange.current().previous().getStartDate(), "MM月dd日");
            //需要处理上一页最后一条记录的日期
            String lastPageDay = "";
            if (createTime != null) {
                lastPageDay = DateUtils.dateToString(new Date(createTime), "MM月dd日");
            }
            Map<String, List<Map<String, Object>>> dateList = resultList.stream().collect(Collectors.groupingBy(e -> DateUtils.dateToString(new Date((Long) ((Map) e.get("info")).get("createTime")), "MM月dd日")));
            for (String day : dateList.keySet()) {
                //如果和上一页最后一条的日期相同。则不需要处理日期了
                if (day.equals(lastPageDay)) {
                    continue;
                }
                if (day.equals(today)) {
                    List<Map<String, Object>> mapList = dateList.get(day);
                    if (CollectionUtils.isNotEmpty(mapList)) {
                        ((Map) (mapList.get(0).get("info"))).put("dateTitle", "今天");
                    }
                } else if (day.equals(yesterday)) {
                    List<Map<String, Object>> mapList = dateList.get(day);
                    if (CollectionUtils.isNotEmpty(mapList)) {
                        ((Map) (mapList.get(0).get("info"))).put("dateTitle", "昨天");
                    }
                } else {
                    List<Map<String, Object>> mapList = dateList.get(day);
                    if (CollectionUtils.isNotEmpty(mapList)) {
                        ((Map) (mapList.get(0).get("info"))).put("dateTitle", day);
                    }
                }
            }
            //寒假作业=需求强制在第一页的置顶位置
            if (createTime == null && new Date().before(NewHomeworkConstants.VH_PARENT_HOMEWORK_DYNAMIC_SHOW_END_TIME)) {
                List<VacationReportForParent> vacationReport = vacationHomeworkReportLoaderClient.loadVacationReportForParent(studentId);
                if (CollectionUtils.isNotEmpty(vacationReport)) {
                    generateVacationHomeworkInfo(vacationReport, resultList, studentDetail, ver);
                }
            }
            return successMessage("list", resultList);
        } catch (Exception ex) {
            if (ex instanceof UtopiaRuntimeException) {
                return failMessage(ex.getMessage());
            } else {
                log.error("load homeworks dynamic state failed. studentId:{}, createTime:{}, ver:{}",
                        studentId, createTime, ver, ex);
                return failMessage("获取作业动态失败");
            }
        }
    }

    @RequestMapping(value = "popup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getFinishHomeworkPopup() {
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        User parent = getCurrentParent();
        if (parent == null) {
            return failMessage(RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        StudentFinishHomeworkPopup homeworkPopup = newHomeworkCacheServiceClient.getNewHomeworkCacheService().studentFinishHomeworkPopupManager_loadStudentPopup(studentId, parent.getId());
        MapMessage mapMessage = successMessage();
        if (homeworkPopup != null) {
            String url = "/view/mobile/parent/homework/report_notice?sid=" + homeworkPopup.getStudentId() + "&hid=" + homeworkPopup.getHomeworkId();
            String homeworkId = homeworkPopup.getHomeworkId();
            NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
            if (newHomework != null && newHomework.getNewHomeworkType() == NewHomeworkType.OCR) {
                Subject subject = newHomework.getSubject();
                ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.OCR_DICTATION;
                if (subject == Subject.MATH) {
                    objectiveConfigType = ObjectiveConfigType.OCR_MENTAL_ARITHMETIC;
                }
                url = "/view/mobile/parent/ocrhomework/student_questions_detail?homeworkId=" + homeworkId + "&subject=" + subject + "&objectiveConfigType=" + objectiveConfigType;
            }
            mapMessage.add(RES_CONTENT, homeworkPopup.getContent()).add(RES_URL, url);
        }
        return mapMessage;
    }


    //所有作业转化为卡片
    private void generateTotalHomeworkInfo(List<NewHomework.Location> newHomeworkList, StudentDetail studentDetail, String ver, List<Map<String, Object>> resultList) {
        if (CollectionUtils.isEmpty(newHomeworkList) || studentDetail == null) {
            return;
        }
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(newHomeworkList.stream().map(NewHomework.Location::getTeacherId).collect(Collectors.toSet()));
        //完成情况
        Set<String> accomplishmentIds = new HashSet<>();
        newHomeworkList.forEach(p -> accomplishmentIds.add(NewAccomplishment.ID.build(p.getCreateTime(),
                p.getSubject(), p.getId()).toString()));
        Map<String, NewAccomplishment> accomplishmentMap = newAccomplishmentLoaderClient.loadNewAccomplishments(accomplishmentIds);

        Set<NewHomework.Location> newHomeworkLocations = new HashSet<>();
        Set<NewHomework.Location> finishedHomeworkLocations = new HashSet<>();
        Set<NewHomework.Location> checkedHomeworkLocations = new HashSet<>();
        Set<NewHomework.Location> unFinishedHomeworkLocations = new HashSet<>();
        for (NewHomework.Location location : newHomeworkList) {
            if (location == null) {
                continue;
            }
            NewAccomplishment newAccomplishment = accomplishmentMap.get(NewAccomplishment.ID.build(location.getCreateTime(), location.getSubject(), location.getId()).toString());
            boolean selfFinish = newAccomplishment == null ? Boolean.FALSE : newAccomplishment.contains(studentDetail.getId());
            if (!location.isChecked() && !selfFinish && new Date(location.getEndTime()).after(new Date())) {
                newHomeworkLocations.add(location);
            } else if (!location.isChecked() && selfFinish) {
                finishedHomeworkLocations.add(location);
            } else if (location.isChecked() && selfFinish) {
                checkedHomeworkLocations.add(location);
            } else {
                unFinishedHomeworkLocations.add(location);
            }
        }
        //待完成作业要load： Book、完成人数
        //已完成作业要load： 错题、个人完成用时、送花人数
        //已检查作业要load： 个人分数、学霸、错题、家长端可领取学豆奖励、老师推荐语音
        //未完成作业要load： 学霸、完成人数
        Set<NewHomework.Location> needLocations = new HashSet<>();
        //book
        needLocations.clear();
        needLocations.addAll(newHomeworkLocations);
        Set<String> bookIds = needLocations.stream().map(NewHomework.Location::getId).collect(Collectors.toSet());
        Map<String, NewHomeworkBook> homeworkBookMap = newHomeworkLoaderClient.loadNewHomeworkBooks(bookIds);
        //错题
        needLocations.clear();
        needLocations.addAll(finishedHomeworkLocations);
        needLocations.addAll(checkedHomeworkLocations);
//        Map<String, List<Map<String, Object>>> wrongQuestionIds = newHomeworkLoaderClient.getStudentWrongQuestionIds(studentDetail.getId(), null, needLocations.stream().map(NewHomework.Location::getId).collect(Collectors.toSet()));
        //newHomeworkResult===个人用时、个人分数
        needLocations.clear();
        needLocations.addAll(finishedHomeworkLocations);
        needLocations.addAll(checkedHomeworkLocations);
        Map<String, NewHomeworkResult> homeworkResultMap = newHomeworkResultLoaderClient.loadNewHomeworkResult(needLocations, studentDetail.getId(), false).stream().collect(Collectors.toMap(NewHomeworkResult::getHomeworkId, Function.identity()));
        //加回学霸（2017-06-07，wiki:http://wiki.17zuoye.net/pages/viewpage.action?pageId=30544712）
        //删除学霸（2017-09-25，redmine:http://project.17zuoye.net/redmine/issues/54122）
//        needLocations.clear();
//        needLocations.addAll(checkedHomeworkLocations);
//        needLocations.addAll(unFinishedHomeworkLocations);
//        Map<String, NewHomeworkStudyMaster> studyMasterMap = newHomeworkPartLoaderClient.getNewHomeworkStudyMasterMap(needLocations.stream().map(NewHomework.Location::getId).collect(Collectors.toSet()));
        //送花人数
        Set<String> flowerKeys = new HashSet<>();
        needLocations.clear();
        needLocations.addAll(finishedHomeworkLocations);
        needLocations.forEach(p -> flowerKeys.add(p.getSubject().name() + "-" + p.getId()));

        AlpsFutureMap<String, List<Flower>> futureMap = new AlpsFutureMap<>();
        for (String flowerKey : flowerKeys) {
            futureMap.put(flowerKey, flowerServiceClient.getFlowerService().loadSourceKeyFlowers(flowerKey));
        }
        Map<String, List<Flower>> flowerMap = futureMap.regularize();
        //老师推荐语音
        //语音推荐
        needLocations.clear();
        needLocations.addAll(checkedHomeworkLocations);
        List<VoiceRecommend> voiceRecommendList = getVoiceRecommendByEnglishHomework(needLocations.stream().filter(p -> p.getSubject() == Subject.ENGLISH).collect(Collectors.toSet()));
        //家长端可领取学豆奖励
        NewHomeworkFinishRewardInParentApp rewardInParentApp = newHomeworkPartLoaderClient.getRewardInParentApp(studentDetail.getId());
        //逐个遍历各种状态的作业
        for (NewHomework.Location doLocation : newHomeworkLocations) {
            String flowerKey = doLocation.getSubject().name() + "-" + doLocation.getId();
            Map<String, Object> homeworkInfoMap = generateHomeworkBasicInfo(doLocation, studentDetail.getId(), teacherMap.get(doLocation.getTeacherId()), flowerMap.get(flowerKey), ver);

            //这个字段是1.6.5以前的老版本在用
            homeworkInfoMap.put("homeworkPlan", StudentHomeworkStatus.NEW);
            //新版本所有卡片统一成status字段
            homeworkInfoMap.put("status", HomeworkDynamicStatus.NEW);
            homeworkInfoMap.put("title", "待完成");
            //结束时间
            String endTimeStr = DateUtils.dateToString(new Date(doLocation.getEndTime()), "MM月dd日HH:mm");
            String content = "截止:" + endTimeStr;
            //教材-单元名称
            NewHomeworkBook newHomeworkBook = homeworkBookMap.get(doLocation.getId());
            if (newHomeworkBook != null) {
                content += "\n作业内容：" + StringUtils.join(newHomeworkBook.processUnitNameList(), ",");
            }
            homeworkInfoMap.put("content", content);
            List<Map<String, Object>> extInfo = buildExtInfo_New(accomplishmentMap.get(NewAccomplishment.ID.build(doLocation.getCreateTime(), doLocation.getSubject(), doLocation.getId()).toString()));
            if (doLocation.getSubject() != Subject.MATH) {
                extInfo.addAll(buildExtInfo_SelfStudy(studentDetail.getId(), doLocation, StudentHomeworkStatus.NEW, ver));
            } else {
                //数学学科增加阿分题导流
                extInfo.add(buildExtInfo_MistakenNoteBook(studentDetail.getId(), doLocation.getSubject()));
            }

            homeworkInfoMap.put("extInfo", extInfo);
            //汇总这个作业的结果
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("type", HomeWorkDynamicType.NEW_HOMEWORK.template);
            resultMap.put("info", homeworkInfoMap);
            //除已完成已检查的作业以外全去作业详情
            //新作业跳转到新作业页面-----2017.2.8
            resultMap.put("buttonUrl", "view/mobile/parent/homework_detail?sid=" + studentDetail.getId() + "&hid=" + doLocation.getId() + "&htype=" + doLocation.getSubject().name() + "&tab=" + ParentHomeWorkReportTab.NEW_HOMEWORK_TAB.getTabType());
            resultMap.put("buttonText", "查看作业");
            resultMap.put("buttonColor", "#41bb54");
            resultList.add(resultMap);
        }
        for (NewHomework.Location doLocation : finishedHomeworkLocations) {
            String flowerKey = doLocation.getSubject().name() + "-" + doLocation.getId();
            Map<String, Object> homeworkInfoMap = generateHomeworkBasicInfo(doLocation, studentDetail.getId(), teacherMap.get(doLocation.getTeacherId()), flowerMap.get(flowerKey), ver);

            //这个字段是1.6.5以前的老版本在用
            homeworkInfoMap.put("homeworkPlan", StudentHomeworkStatus.FINISH);
            //新版本所有卡片统一成status字段
            homeworkInfoMap.put("status", HomeworkDynamicStatus.FINISH);
            homeworkInfoMap.put("title", "已完成");
            //做题结果-分数、用时
            NewHomeworkResult newHomeworkResult = homeworkResultMap.get(doLocation.getId());
            //mongo挂了。作业后处理错误。导致数据异常了。
            Integer score = newHomeworkResult == null ? Integer.valueOf(0) : newHomeworkResult.processScore();
            //错题
//            int wrongCount = ParentHomeworkUtil.getWrongCountWithHomeworkId(doLocation, wrongQuestionIds);
            int wrongCount = 0;
            String content;
            if (score != null) {
                content = "成绩" + generateHomeworkScoreLevel(score, studentDetail);
            } else {
                content = "作业已完成";
            }
            content += wrongCount < 1 ? "" : "，做错" + wrongCount + "题";
            homeworkInfoMap.put("content", content);
            List<Map<String, Object>> extInfo = buildExtInfo_Finish(rewardInParentApp, doLocation, flowerMap.get(doLocation.getSubject().name() + "-" + doLocation.getId()), studentDetail.getId());
            if (doLocation.getSubject() != Subject.MATH) {
                extInfo.addAll(buildExtInfo_SelfStudy(studentDetail.getId(), doLocation, StudentHomeworkStatus.FINISH, ver));
            } else {
                //数学学科增加阿分题导流
                extInfo.add(buildExtInfo_MistakenNoteBook(studentDetail.getId(), doLocation.getSubject()));
            }

            homeworkInfoMap.put("extInfo", extInfo);
            //汇总这个作业的结果
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("type", HomeWorkDynamicType.NEW_HOMEWORK.template);
            resultMap.put("info", homeworkInfoMap);
            //除已完成已检查的作业以外全去作业详情
            //已完成未检查的作业跳转到完成页面----2017.2.8
            resultMap.put("buttonUrl", "view/mobile/parent/homework_detail?sid=" + studentDetail.getId() + "&hid=" + doLocation.getId() + "&htype=" + doLocation.getSubject().name() + "&tab=" + ParentHomeWorkReportTab.FINISH_HOMEWORK_TAB.getTabType());
            resultMap.put("buttonText", wrongCount > 0 ? "查看错题" : "查看作业报告");
            resultMap.put("buttonColor", wrongCount > 0 ? "#ff8971" : "#41bb54");
            resultList.add(resultMap);
        }
        for (NewHomework.Location doLocation : checkedHomeworkLocations) {
            String flowerKey = doLocation.getSubject().name() + "-" + doLocation.getId();
            Map<String, Object> homeworkInfoMap = generateHomeworkBasicInfo(doLocation, studentDetail.getId(), teacherMap.get(doLocation.getTeacherId()), flowerMap.get(flowerKey), ver);
            //这个字段是1.6.5以前的老版本在用
            homeworkInfoMap.put("homeworkPlan", StudentHomeworkStatus.CHECK_FINISH);
            //新版本所有卡片统一成status字段
            homeworkInfoMap.put("status", HomeworkDynamicStatus.CHECKED);
            homeworkInfoMap.put("title", "已检查");
            //做题结果-得分
            NewHomeworkResult newHomeworkResult = homeworkResultMap.get(doLocation.getId());
            Integer score = newHomeworkResult == null ? Integer.valueOf(0) : newHomeworkResult.processScore();
            //错题
//            int wrongCount = ParentHomeworkUtil.getWrongCountWithHomeworkId(doLocation, wrongQuestionIds);
            String content;
            if (score != null) {
                content = "成绩" + generateHomeworkScoreLevel(score, studentDetail);
            } else {
                content = "作业已完成";
            }
//            if (wrongCount > 0) {
//                content += "，做错" + wrongCount + "题";
//            }
            //加回学霸（2017-06-07，wiki:http://wiki.17zuoye.net/pages/viewpage.action?pageId=30544712）
            //删除学霸（2017-09-25，redmine:http://project.17zuoye.net/redmine/issues/54122）
//            NewHomeworkStudyMaster studyMaster = studyMasterMap.get(doLocation.getId());
//            if (studyMaster != null && CollectionUtils.isNotEmpty(studyMaster.getMasterStudentList())) {
//                content += "\n本次学霸: " + StringUtils.join(studyMaster.getMasterStudentList().stream().map(NewHomeworkStudyMaster.MasterStudent::getUserName).collect(Collectors.toList()), "，");
//            }
            homeworkInfoMap.put("content", content);
            NewAccomplishment newAccomplishment = accomplishmentMap.get(NewAccomplishment.ID.build(doLocation.getCreateTime(), doLocation.getSubject(), doLocation.getId()).toString());
            List<Map<String, Object>> extInfo = buildExtInfo_Finish_Check(rewardInParentApp, doLocation, newHomeworkResult, newAccomplishment, voiceRecommendList);
            if (doLocation.getSubject() != Subject.MATH) {
                extInfo.addAll(buildExtInfo_SelfStudy(studentDetail.getId(), doLocation, StudentHomeworkStatus.CHECK_FINISH, ver));
            } else {
                //数学学科增加阿分题导流
                extInfo.add(buildExtInfo_MistakenNoteBook(studentDetail.getId(), doLocation.getSubject()));
            }

            homeworkInfoMap.put("extInfo", extInfo);
            //汇总这个作业的结果
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("type", HomeWorkDynamicType.NEW_HOMEWORK.template);
            resultMap.put("info", homeworkInfoMap);
            //已完成已检查的去作业报告。其他全去作业详情
            //已完成已检查的作业跳转到已检查的页面----2016.2.8
            resultMap.put("buttonUrl", "view/mobile/parent/homework_report?sid=" + studentDetail.getId() + "&hid=" + doLocation.getId() + "&htype=" + doLocation.getSubject().name() + "&tab=" + ParentHomeWorkReportTab.FINISH_HOMEWORK_TAB.getTabType());
            resultMap.put("buttonText", "查看作业报告");
            resultMap.put("buttonColor", "#41bb54");
            resultList.add(resultMap);
        }
        for (NewHomework.Location doLocation : unFinishedHomeworkLocations) {
            String flowerKey = doLocation.getSubject().name() + "-" + doLocation.getId();
            Map<String, Object> homeworkInfoMap = generateHomeworkBasicInfo(doLocation, studentDetail.getId(), teacherMap.get(doLocation.getTeacherId()), flowerMap.get(flowerKey), ver);

            //这个字段是1.6.5以前的老版本在用
            homeworkInfoMap.put("homeworkPlan", StudentHomeworkStatus.CHECK_UNFINISH);
            //新版本所有卡片统一成status字段
            homeworkInfoMap.put("status", HomeworkDynamicStatus.UN_FINISH);
            homeworkInfoMap.put("title", "未完成");
            String content = "本次作业未完成，请家长督促补做";
            homeworkInfoMap.put("content", content);
            List<Map<String, Object>> extInfo = new ArrayList<>();
            if (doLocation.getSubject() != Subject.MATH) {
                extInfo.addAll(buildExtInfo_SelfStudy(studentDetail.getId(), doLocation, StudentHomeworkStatus.CHECK_UNFINISH, ver));
            } else {
                //数学学科增加阿分题导流
                extInfo.add(buildExtInfo_MistakenNoteBook(studentDetail.getId(), doLocation.getSubject()));
            }

            homeworkInfoMap.put("extInfo", extInfo);
            //汇总这个作业的结果
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("type", HomeWorkDynamicType.NEW_HOMEWORK.template);
            resultMap.put("info", homeworkInfoMap);
            //除已完成已检查的作业以外全去作业详情
            //未完成已检查的作业跳转到已检查页面-----2017.2.8
            resultMap.put("buttonUrl", doLocation.isChecked() ? "view/mobile/parent/homework_detail?sid=" + studentDetail.getId() + "&hid=" + doLocation.getId() + "&htype=" + doLocation.getSubject().name() + "&tab=" + ParentHomeWorkReportTab.FINISH_HOMEWORK_TAB.getTabType() : "view/mobile/parent/homework_detail?sid=" + studentDetail.getId() + "&hid=" + doLocation.getId() + "&htype=" + doLocation.getSubject().name() + "&tab=" + ParentHomeWorkReportTab.NEW_HOMEWORK_TAB.getTabType());
            resultMap.put("buttonText", "查看作业");
            resultMap.put("buttonColor", "#41bb54");
            resultList.add(resultMap);
        }
    }

    //所有通知转化为卡片
    private void generateJxtNoticeInfo(List<JxtNotice> jxtNoticeList, StudentDetail studentDetail, Set<Long> groupIds, User parent, String ver, List<Map<String, Object>> resultList) {
        if (CollectionUtils.isEmpty(jxtNoticeList)) {
            return;
        }
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(jxtNoticeList.stream().map(JxtNotice::getTeacherId).collect(Collectors.toSet()));
        Map<String, List<JxtFeedBack>> feedBackListByNoticeIds = jxtLoaderClient.getFeedBackListByNoticeIds(jxtNoticeList.stream().map(JxtNotice::getId).collect(Collectors.toSet()));
        for (JxtNotice jxtNotice : jxtNoticeList) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("type", HomeWorkDynamicType.JXT_NOTICE.template);
            resultMap.put("info", buildNewJxtNoticeMap(jxtNotice, teacherMap.get(jxtNotice.getTeacherId()), parent.getId(), feedBackListByNoticeIds.get(jxtNotice.getId()), studentDetail, ver));
            if (groupIds.stream().anyMatch(p -> jxtNotice.getGroupIds().contains(p))) {
                resultMap.put("buttonUrl", "/view/mobile/common/notice_detail?user_type=parent&notice_id=" + jxtNotice.getId() + "&group_id=" + groupIds.stream().filter(p -> jxtNotice.getGroupIds().contains(p)).findFirst().get());
                resultMap.put("buttonText", "查看详情");
                resultMap.put("buttonColor", "#41bb54");
            }
            resultList.add(resultMap);
        }
    }

    //所有作业单转化为卡片
    private void generateOfflineHomeworkInfo(List<OfflineHomework> offlineHomeworkList, User parent, StudentDetail studentDetail, List<Map<String, Object>> resultList, String ver) {
        if (CollectionUtils.isEmpty(offlineHomeworkList)) {
            return;
        }
        Set<String> offlineHomeworkIds = offlineHomeworkList.stream().map(OfflineHomework::getId).collect(Collectors.toSet());
        Set<String> newHomeworkIds = offlineHomeworkList.stream().map(OfflineHomework::getNewHomeworkId).collect(Collectors.toSet());
        Map<String, NewHomework> newHomeworkMap = newHomeworkLoaderClient.loadNewHomeworks(newHomeworkIds);
        Map<String, NewHomeworkBook> newHomeworkBookMap = newHomeworkLoaderClient.loadNewHomeworkBooks(newHomeworkIds);
        Set<Long> teacherIds = offlineHomeworkList.stream().map(OfflineHomework::getTeacherId).collect(Collectors.toSet());
        Map<String, List<OfflineHomeworkSignRecord>> signRecordMap = jxtLoaderClient.getSignRecordByOfflineHomeworkIds(offlineHomeworkIds);
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIds);
        for (OfflineHomework offlineHomework : offlineHomeworkList) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("type", HomeWorkDynamicType.OFFLINE_HOMEWORK.template);
            resultMap.put("info", generateOfflineHomeworkMap(offlineHomework, newHomeworkMap, newHomeworkBookMap,
                    signRecordMap.get(offlineHomework.getId()), teacherMap.get(offlineHomework.getTeacherId()), parent.getId(), studentDetail, ver));
            resultMap.put("buttonUrl", "/view/offlinehomework/detail?ohids=" + offlineHomework.getId() + "&sid=" + studentDetail.getId());
            resultMap.put("buttonText", "查看详情");
            resultMap.put("buttonColor", "#41bb54");
            resultList.add(resultMap);
        }

    }

    private Map<String, Object> generateOfflineHomeworkMap(OfflineHomework offlineHomework, Map<String, NewHomework> newHomeworkMap, Map<String, NewHomeworkBook> newHomeworkBookMap,
                                                           List<OfflineHomeworkSignRecord> signRecordList, Teacher teacher, Long parentId, StudentDetail studentDetail, String ver) {
        Map<String, Object> offlineHomeworkMap = new HashMap<>();

        boolean hadSign = CollectionUtils.isNotEmpty(signRecordList) && signRecordList.stream().anyMatch(p -> parentId.equals(p.getParentId()) && studentDetail.getId().equals(p.getStudentId()));
        String title = "";
        HomeworkDynamicStatus status = HomeworkDynamicStatus.UNKNOWN;
        //需要签字的才有这个title
        if (offlineHomework.getNeedSign()) {
            if (hadSign) {
                title = "已签字";
                status = HomeworkDynamicStatus.FINISH;
            } else {
                title = "待签字";
                status = HomeworkDynamicStatus.NEW;
            }
        }
        //1.6.5之后的字段
        //必须字段
        offlineHomeworkMap.put("title", title);
        offlineHomeworkMap.put("leftTitle", HomeWorkDynamicType.OFFLINE_HOMEWORK.leftTitle);
        offlineHomeworkMap.put("subjectIcon", getSubjectIconUrl(teacher == null ? null : teacher.getSubject()));
        String newHomeworkId = offlineHomework.getNewHomeworkId();
        offlineHomeworkMap.put("content", OfflineHomeworkUtils.buildMessageContent(offlineHomework, newHomeworkMap.get(newHomeworkId), newHomeworkBookMap.get(newHomeworkId)));
        offlineHomeworkMap.put("createTime", offlineHomework.getCreateAt().getTime());
        offlineHomeworkMap.put("extInfo", buildOfflineHomeworkExtInfoMap(offlineHomework, signRecordList));
        if (VersionUtil.compareVersion(ver, "1.6.5.0") < 0) {
            //1.6.5之前的字段
            String teacherName = generateTeacherName(teacher, studentDetail.getClazzId());
            teacherName = teacherName + "老师：";
            offlineHomeworkMap.put("expireTime", "截止" + DateUtils.dateToString(offlineHomework.getEndTime(), "M月dd日 HH:mm"));
            offlineHomeworkMap.put("teacherName", teacherName);
        }
        //1.6.5版本作业动态统一使用一个卡片状态来处理所有卡片
        offlineHomeworkMap.put("status", status);
        return offlineHomeworkMap;
    }

    private List<Map<String, Object>> buildOfflineHomeworkExtInfoMap(OfflineHomework offlineHomework, List<OfflineHomeworkSignRecord> signRecordList) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        //截止时间
        Map<String, Object> expireDetail = new HashMap<>();
        expireDetail.put("iconUrl", getCdnBaseUrlStaticSharedWithSep() + ICO_DYNAMIC_DOT);
        if (offlineHomework.getEndTime().before(new Date())) {
            expireDetail.put("title", "截止" + DateUtils.dateToString(offlineHomework.getEndTime(), "MM月dd日") + "【已截止】");
        } else {
            expireDetail.put("title", "截止" + DateUtils.dateToString(offlineHomework.getEndTime(), "MM月dd日HH:mm"));
        }
        mapList.add(expireDetail);
        if (offlineHomework.getNeedSign()) {
            //签字人数
            int signCount = (CollectionUtils.isEmpty(signRecordList) ? 0 : signRecordList.size());
            Map<String, Object> signDetail = new HashMap<>();
            signDetail.put("iconUrl", getCdnBaseUrlStaticSharedWithSep() + ICO_DYNAMIC_DOT);
            signDetail.put("title", signCount + "位家长已签字");
            mapList.add(signDetail);
        }
        return mapList;
    }

    //单个通知转化为卡片内容
    private Map<String, Object> buildNewJxtNoticeMap(JxtNotice jxtNotice, Teacher teacher, Long parentId, List<JxtFeedBack> feedBackList, StudentDetail studentDetail, String ver) {
        Map<String, Object> jxtNoticeMap = new HashMap<>();

        String title;
        boolean hadFeedBack = CollectionUtils.isNotEmpty(feedBackList) && feedBackList.stream().anyMatch(p -> p.getParentId().equals(parentId));
        HomeworkDynamicStatus status;
        if (hadFeedBack) {
            status = HomeworkDynamicStatus.FINISH;
            if (jxtNotice.getNeedFeedBack()) {
                title = "已确认";
            } else {
                title = "已查看";
            }
        } else {
            status = HomeworkDynamicStatus.NEW;
            if (jxtNotice.getNeedFeedBack()) {
                title = "待确认";
            } else {
                title = "待查看";
            }
        }

        String img = CollectionUtils.isEmpty(jxtNotice.getImgUrl()) ? "" : "[图片]";
        String voice = StringUtils.isBlank(jxtNotice.getVoiceUrl()) ? "" : "[语音]";
        String content = img + voice + jxtNotice.getContent();
        //1.6.5之后的字段
        //必须字段
        jxtNoticeMap.put("title", title);
        jxtNoticeMap.put("leftTitle", HomeWorkDynamicType.JXT_NOTICE.leftTitle);
        jxtNoticeMap.put("subjectIcon", getSubjectIconUrl(teacher == null ? null : teacher.getSubject()));
        jxtNoticeMap.put("content", content);
        jxtNoticeMap.put("createTime", jxtNotice.getCreateTime().getTime());
        jxtNoticeMap.put("extInfo", buildNewJxtNoticeExtInfoMap(jxtNotice, feedBackList));
        if (VersionUtil.compareVersion(ver, "1.6.5.0") < 0) {
            //1.6.5之前的字段
            String teacherName = generateTeacherName(teacher, studentDetail.getClazzId());
            teacherName = teacherName + "老师：";
            jxtNoticeMap.put("expireTime", "截止" + DateUtils.dateToString(jxtNotice.getExpireTime(), "M月dd日 HH:mm"));
            jxtNoticeMap.put("teacherName", teacherName);
        }
        //1.6.5版本作业动态统一使用一个卡片状态来处理所有卡片
        jxtNoticeMap.put("status", status);
        return jxtNoticeMap;
    }

    //单个通知的扩展tab内容
    private List<Map<String, Object>> buildNewJxtNoticeExtInfoMap(JxtNotice jxtNotice, List<JxtFeedBack> feedBackList) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        //截止时间
        Map<String, Object> expireDetail = new HashMap<>();
        expireDetail.put("iconUrl", getCdnBaseUrlStaticSharedWithSep() + ICO_DYNAMIC_DOT);
        if (jxtNotice.getExpireTime().before(new Date())) {
            expireDetail.put("title", "截止" + DateUtils.dateToString(jxtNotice.getExpireTime(), "MM月dd日") + "【已截止】");
        } else {
            expireDetail.put("title", "截止" + DateUtils.dateToString(jxtNotice.getExpireTime(), "MM月dd日HH:mm"));
        }
        mapList.add(expireDetail);
        //查看人数
        int feedBackCount = (CollectionUtils.isEmpty(feedBackList) ? 0 : feedBackList.size());
        Map<String, Object> feedBackDetail = new HashMap<>();
        feedBackDetail.put("iconUrl", getCdnBaseUrlStaticSharedWithSep() + ICO_DYNAMIC_DOT);
        if (feedBackCount == 0) {
            if (jxtNotice.getNeedFeedBack()) {
                feedBackDetail.put("title", "去第一个确认");
            } else {
                feedBackDetail.put("title", "去第一个查看");
            }
        } else {
            if (jxtNotice.getNeedFeedBack()) {
                feedBackDetail.put("title", feedBackCount + "位家长已确认");
            } else {
                feedBackDetail.put("title", feedBackCount + "位家长已查看");
            }
        }

        mapList.add(feedBackDetail);
        return mapList;
    }

    //单个作业转化为卡片内容
    private Map<String, Object> generateHomeworkBasicInfo(NewHomework.Location location, Long studentId, Teacher teacher, List<Flower> flowerList, String ver) {
        Map<String, Object> homeworkMap = new HashMap<>();
        homeworkMap.put("leftTitle", HomeWorkDynamicType.NEW_HOMEWORK.leftTitle);
        homeworkMap.put("studentId", studentId);
        homeworkMap.put("homeworkType", location.getSubject());//这里用于是客户端的。参数名不能变了。但是值值一样的。
        homeworkMap.put("subjectIcon", getSubjectIconUrl(location.getSubject()));
        homeworkMap.put("homeworkId", location.getId());
        homeworkMap.put("createTime", location.getCreateTime());
        //1.6.2之前的版本需要的字段
        if (VersionUtil.compareVersion(ver, "1.6.5.0") < 0) {
            //老师姓名
            String teacherName = teacher == null ? "老师：" : teacher.fetchRealname() + "老师：";
            //个人是否送花
            boolean selfSend = CollectionUtils.isEmpty(flowerList) ? Boolean.FALSE : flowerList.stream().anyMatch(p -> p.getSenderId().equals(studentId));
            homeworkMap.put("teacherId", location.getTeacherId());
            homeworkMap.put("teacherName", teacherName);
            homeworkMap.put("sentFlag", selfSend);
        }
        return homeworkMap;
    }


    private List<VoiceRecommend> getVoiceRecommendByEnglishHomework(Collection<NewHomework.Location> locations) {
        if (CollectionUtils.isEmpty(locations)) {
            return new ArrayList<>();
        }
        Date now = new Date();
        List<String> homeworkIds = locations.stream().filter(p -> !now.after(DateUtils.calculateDateDay(new Date(p.getCreateTime()), 30)))
                .filter(NewHomework.Location::isChecked)
                .sorted((o1, o2) -> new Date(o2.getCreateTime()).compareTo(new Date(o1.getCreateTime())))
                .map(NewHomework.Location::getId)
                .collect(Collectors.toList());
        return voiceRecommendLoaderClient.loadExcludeNoRecommend(homeworkIds);
    }

    private List<Map<String, Object>> getReturnList(List<NewHomework.Location> newHomeworkLocations, List<JxtNotice> noticeList, List<OfflineHomework> offlineHomeworkList) {
        List<Map<String, Object>> compareList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(newHomeworkLocations)) {
            newHomeworkLocations.forEach(p -> {
                if (p != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("type", HomeWorkDynamicType.NEW_HOMEWORK);
                    map.put("ob", p);
                    map.put("createTime", p.getCreateTime());
                    compareList.add(map);
                }
            });
        }
        if (CollectionUtils.isNotEmpty(noticeList)) {
            noticeList.forEach(p -> {
                if (p != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("type", HomeWorkDynamicType.JXT_NOTICE);
                    map.put("ob", p);
                    map.put("createTime", p.getCreateTime().getTime());
                    compareList.add(map);
                }
            });
        }
        if (CollectionUtils.isNotEmpty(offlineHomeworkList)) {
            offlineHomeworkList.forEach(p -> {
                if (p != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("type", HomeWorkDynamicType.OFFLINE_HOMEWORK);
                    map.put("ob", p);
                    map.put("createTime", p.getCreateAt().getTime());
                    compareList.add(map);
                }
            });
        }
        List<Map<String, Object>> resultList = compareList.stream()
                .sorted((o1, o2) -> Long.compare(SafeConverter.toLong(o2.get("createTime")), SafeConverter.toLong(o1.get("createTime"))))
                .collect(Collectors.toList());
        resultList = resultList.subList(0, resultList.size() > 6 ? 6 : resultList.size());
        return resultList;
    }

    private String getSubjectIconUrl(Subject subject) {
        String iconSubject;
        if (subject == Subject.ENGLISH) {
            iconSubject = getCdnBaseUrlStaticSharedWithSep() + ICO_DYNAMIC_ENGLISH;
        } else if (subject == Subject.MATH) {
            iconSubject = getCdnBaseUrlStaticSharedWithSep() + ICO_DYNAMIC_MATH;
        } else {
            iconSubject = getCdnBaseUrlStaticSharedWithSep() + ICO_DYNAMIC_CHINESE;
        }
        return iconSubject;
    }

    private String generateTeacherName(Teacher teacher, Long clazzId) {
        String teacherName;
        if (clazzId != null && teacher != null) {
            List<GroupMapper> teacherAllGroupInClazz = teacherLoaderClient.findTeacherAllGroupInClazz(clazzId, teacher.getId());
            if (CollectionUtils.isNotEmpty(teacherAllGroupInClazz) && teacherAllGroupInClazz.size() > 1) {
                teacherName = teacher.fetchRealname();
            } else
                teacherName = (teacher.getSubject() != null ? teacher.getSubject().getValue() : "") + teacher.fetchRealname();
        } else {
            teacherName = teacher == null ? "" : ((teacher.getSubject() != null ? teacher.getSubject().getValue() : "") + teacher.fetchRealname());
        }
        return teacherName;
    }

    private String generateHomeworkScoreLevel(Integer score, StudentDetail studentDetail) {
        if (score == null) {
            return "";
        }
        if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList")) {
            return ScoreLevel.processLevel(score).getLevel();
        }
        return String.valueOf(score);
    }

    private void generateVacationHomeworkInfo(List<VacationReportForParent> vacationReport, List<Map<String, Object>> returnList, StudentDetail studentDetail, String ver) {
        if (CollectionUtils.isEmpty(vacationReport)) {
            return;
        }
        Set<Long> vacationTeacherIds = new HashSet<>();
        vacationReport.forEach(p -> vacationTeacherIds.add(p.getLocation().getTeacherId()));
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(vacationTeacherIds);
        for (int index = 0; index < vacationReport.size(); index++) {
            VacationReportForParent report = vacationReport.get(index);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("type", HomeWorkDynamicType.VACATION_HOMEWORK.template);
            resultMap.put("info", generateVacationHomeworkMap(report, teacherMap.get(report.getLocation().getTeacherId()), studentDetail, ver));
            resultMap.put("buttonUrl", "/view/mobile/activity/parent/vacation?packageId=" + report.getLocation().getId());
            resultMap.put("buttonText", "查看详情");
            resultMap.put("buttonColor", "#41bb54");
            if (returnList.size() > index) {
                returnList.add(index, resultMap);
            } else {
                returnList.add(resultMap);
            }
        }
    }

    private Map<String, Object> generateVacationHomeworkMap(VacationReportForParent report, Teacher teacher, StudentDetail studentDetail, String ver) {
        if (report == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> vacationHomeworkMap = new HashMap<>();
        String title = "未完成";
        HomeworkDynamicStatus status = HomeworkDynamicStatus.NEW;
        //需要签字的才有这个title
        if (report.isFinish()) {
            title = "已完成";
            status = HomeworkDynamicStatus.FINISH;
        }
        //1.6.5之后的字段
        //必须字段
        vacationHomeworkMap.put("title", title);
        vacationHomeworkMap.put("dateTitle", "置顶");
        vacationHomeworkMap.put("leftTitle", HomeWorkDynamicType.VACATION_HOMEWORK.leftTitle);
        vacationHomeworkMap.put("subjectIcon", getSubjectIconUrl(report.getSubject()));
        String content = "作业天数：共" + report.getTotalHomeworkNum() + "天任务\n" + "截止时间：" + DateUtils.dateToString(new Date(report.getLocation().getEndTime()), "yyyy年MM月dd日 HH:mm");
        vacationHomeworkMap.put("content", content);
        vacationHomeworkMap.put("createTime", report.getLocation().getCreateTime());
        //寒假作业扩展信息=临时的。直接放这里面了。之后方便一起删除
        List<Map<String, Object>> extMapList = new ArrayList<>();
        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("iconUrl", getCdnBaseUrlStaticSharedWithSep() + ICO_DYNAMIC_DOT);
        extInfo.put("title", report.getFinishedVacationHomework() + "名学生已开始");
        extMapList.add(extInfo);
        vacationHomeworkMap.put("extInfo", extMapList);
        if (VersionUtil.compareVersion(ver, "1.6.5.0") < 0) {
            //1.6.5之前的字段
            String teacherName = generateTeacherName(teacher, studentDetail.getClazzId());
            teacherName = teacherName + "老师：";
            vacationHomeworkMap.put("expireTime", "截止" + DateUtils.dateToString(new Date(report.getLocation().getEndTime()), "M月dd日 HH:mm"));
            vacationHomeworkMap.put("teacherName", teacherName);
        }
        //1.6.5版本作业动态统一使用一个卡片状态来处理所有卡片
        vacationHomeworkMap.put("status", status);
        return vacationHomeworkMap;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private enum HomeWorkDynamicType {
        COMMON_TYPE("homework_common_template", ""),
        NEW_HOMEWORK("homework", "在线作业"),
        JXT_NOTICE("teacher", "班务通知"),
        OFFLINE_HOMEWORK("teacher", "作业单"),
        @Deprecated
        TEACHER_REMINDER("teacher", ""),
        @Deprecated
        VOICE_RECOMMEND("homework_recommend_radio", ""),
        VACATION_HOMEWORK("homework", "寒假作业");

        private final String template;
        private final String leftTitle;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private enum HomeworkDynamicStatus {
        NEW,
        FINISH,
        CHECKED,
        UN_FINISH,
        UNKNOWN,;

    }
}
