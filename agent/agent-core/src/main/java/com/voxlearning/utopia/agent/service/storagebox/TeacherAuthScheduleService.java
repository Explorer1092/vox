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

package com.voxlearning.utopia.agent.service.storagebox;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.agent.bean.storagebox.TeacherAuthInfo;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.resource.TeacherResourceService;
import com.voxlearning.utopia.agent.service.region.AgentRegionService;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.agent.view.teacher.TeacherBasicInfo;
import com.voxlearning.utopia.agent.view.teacher.TeacherGroupInfo;
import com.voxlearning.utopia.agent.view.teacher.TeacherSubject;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.user.api.entities.InviteHistory;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.TeacherSummaryEsInfo;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * 新老师认证进度的服务
 * Created by yaguang.wang on 2016/12/7.
 */
@Named
public class TeacherAuthScheduleService extends AbstractAgentService {

    @Inject private BaseOrgService baseOrgService;
    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;
    @Inject private TeacherResourceService teacherResourceService;
    @Inject protected AgentRegionService agentRegionService;
    @Inject private SearchService searchService;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    private static final Integer TEACHER_SCOPE = 30;

    public List<TeacherAuthInfo> loadSchoolTeacherAuthInfoByUserId(Long userId) {
        List<Long> allSchoolIds = baseOrgService.loadBusinessSchoolByUserId(userId);
        if (CollectionUtils.isEmpty(allSchoolIds)) {
            return Collections.emptyList();
        }
        List<Long> juniorSchoolIds = baseOrgService.getSchoolListByLevel(allSchoolIds,SchoolLevel.JUNIOR);
        List<SchoolLevel> middleSchoolLevels = new ArrayList<>();
        middleSchoolLevels.add(SchoolLevel.MIDDLE);
        middleSchoolLevels.add(SchoolLevel.HIGH);
        List<Long> middleSchoolIds = baseOrgService.getSchoolListByLevels(allSchoolIds,middleSchoolLevels);

        List<Long> schoolIds = new ArrayList<>();
        schoolIds.addAll(juniorSchoolIds);
        schoolIds.addAll(middleSchoolIds);
        Date startDate = DateUtils.addDays(DateUtils.stringToDate(DateUtils.dateToString(new Date(), "yyyyMMdd"), "yyyyMMdd"), -TEACHER_SCOPE);
        Page<TeacherSummaryEsInfo> esInfoPage = searchService.queryUnAuthTeacherFromEsInSchools(schoolIds,DateUtils.dateToString(startDate,"yyyyMMddHHmmss"),String.valueOf(AuthenticationState.WAITING),0,500);
        Set<Long> teacherIds = new HashSet<>();


        if(CollectionUtils.isNotEmpty(esInfoPage.getContent())){
            esInfoPage.getContent().stream().forEach(p -> {
                if(Objects.equals(SchoolLevel.MIDDLE,SchoolLevel.valueOf(p.getSchoolLevel()))  || Objects.equals(SchoolLevel.HIGH,SchoolLevel.valueOf(p.getSchoolLevel()))){
                    if (Objects.equals(Subject.ofWithUnknown(p.getSubject()), Subject.ENGLISH) || Objects.equals(Subject.ofWithUnknown(p.getSubject()), Subject.MATH)){
                        teacherIds.add(p.getTeacherId());
                    }
                }else{
                    teacherIds.add(p.getTeacherId());
                }
            });
        }

        List<TeacherAuthInfo> result = new ArrayList<>();
        List<Future<List<TeacherAuthInfo>>> futureList = new ArrayList<>();
        List<List<Long>> splitIds = splitList(teacherIds,20);
        for(List<Long> itemList : splitIds){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getTeacherAuthInfos(itemList)));
        }
        for(Future<List<TeacherAuthInfo>> future : futureList) {
            try {
                List<TeacherAuthInfo> item = future.get();
                if(CollectionUtils.isNotEmpty(item)){
                    result.addAll(item);
                }
            } catch (Exception e) {
                logger.error("新注册老师异常",e);
            }
        }
//        List<TeacherAuthInfo> result = getTeacherAuthInfos(teacherIds);
        result.sort((o1, o2) -> o2.getRegisterDate().compareTo(o1.getRegisterDate()));
        return result;
    }

    public List<TeacherAuthInfo> getTeacherAuthInfos(Collection<Long> teacherIds){
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIds);
        Map<Long, School> schoolMap = asyncTeacherServiceClient.getAsyncTeacherService().loadTeacherSchools(teacherIds).getUninterruptibly();
//        List<TeacherBasicInfo> teacherBasicInfos = teacherResourceService.generateTeacherBasicInfo(teacherIds, true, false, true,true);
        Map<Long, CrmTeacherSummary>  teacherSummaryMap = crmSummaryLoaderClient.loadTeacherSummary(teacherIds);

        List<InviteHistory> inviteHistoryList = asyncInvitationServiceClient.loadByInvitees(teacherIds).toList();
        Map<Long,Long> userIdMap = new HashMap<>();
        Map<Long,User> inviterMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(inviteHistoryList)) {
            //被邀请老师id 邀请人id
            userIdMap = inviteHistoryList.stream().collect(Collectors.toMap(InviteHistory :: getInviteeUserId,InviteHistory::getUserId));
//            List<Long> userIds = inviteHistoryList.stream().map(InviteHistory::getUserId).collect(toList());
            inviterMap = userLoaderClient.loadUsersIncludeDisabled(inviteHistoryList.stream().map(InviteHistory::getUserId).collect(toList()));

        }

        List<TeacherAuthInfo> authList = new ArrayList<>();
        Map<Long, Long> finalUserIdMap = userIdMap;
        Map<Long, User> finalInviterMap = inviterMap;

        teacherMap.forEach((k,teacher) ->{
            School school = schoolMap.get(k);
            TeacherAuthInfo teacherAuthInfo = new TeacherAuthInfo();
            teacherAuthInfo.setSchoolName(school.getCmainName());
            teacherAuthInfo.setTeacherId(k);
            teacherAuthInfo.setTeacherName(teacher.fetchRealname());
//            String phone = sensitiveUserDataServiceClient.showUserMobile(k, "agent:generateTeacherExtStateInfo", "");
//            teacherAuthInfo.setMobile(phone);
            teacherAuthInfo.setRegisterDate(teacher.getCreateTime());
            long dayDiff = DateUtils.hourDiff(DateUtils.getDayStart(new Date()), teacher.getCreateTime());
            int registerDayNum =  dayDiff < 1 ? 1 : Double.valueOf(Math.ceil(dayDiff/24d)).intValue();
            if(registerDayNum > TEACHER_SCOPE){//因主副账号问题可能会查出主账号的注册时间大于30  在这过滤下
                return;
            }
            teacherAuthInfo.setRegisterDayNum(registerDayNum);
            //从es里查出来的数据 主副账号的都有  根据id查出来老师信息就可以了  主账号可能会取出来多个学科 但是前端只取第一个  这里就不需要单独处理了
            teacherAuthInfo.setSubjects(teacher.getSubjects());
            User inviter = finalInviterMap.get(finalUserIdMap.get(teacherAuthInfo.getTeacherId()));
            if (inviter != null) {
                List<String> inviterNames = new ArrayList<>();
                inviterNames.add(inviter.getProfile().getRealname());
                teacherAuthInfo.setInviterNames(inviterNames);
            }

            CrmTeacherSummary v = teacherSummaryMap.get(k);
            if (v != null){
                int classCount = SafeConverter.toInt(v.getGroupCount());
                teacherAuthInfo.setClassCount(classCount);
                int stuCount = SafeConverter.toInt(v.getAllStudentCount());
                teacherAuthInfo.setStuCount(stuCount);

                double averageClazzSize =MathUtils.doubleDivide(stuCount,classCount,2);
                if(averageClazzSize > 1){
                    teacherAuthInfo.setStudentJoin(true);
                }else
                    teacherAuthInfo.setStudentJoin(false);
                teacherAuthInfo.setLast30DaysHwSc(SafeConverter.toInt(v.getLast30DaysHwSc()));
                teacherAuthInfo.setUnUsed(SafeConverter.toInt(v.getLast30DaysHwSc()) < 1);
                boolean unAuthIn10Days = SafeConverter.toLong(v.fetchRegisterTimeStamp()) < DateUtils.addDays(new Date(), -10).getTime() && v.getAuthState() != 1;
                teacherAuthInfo.setUnAuthIn10Days(unAuthIn10Days);
                boolean maybeFakeTeacher = v.getAuthState() != 1
                        && CollectionUtils.isEmpty(teacherAuthInfo.getInviterNames())
                        && SafeConverter.toLong(v.fetchRegisterTimeStamp()) < DateUtils.addDays(new Date(), -3).getTime()
                        && teacherAuthInfo.getLast30DaysHwSc() == 0
                        && teacherAuthInfo.getStuCount() <= 5
                        && (SafeConverter.toInt(v.getGroupCount()) == 0 || SafeConverter.toInt(v.getMaxSameSubjAuTeaCountInClass()) > 0);

                teacherAuthInfo.setMaybeFakeTeacher(maybeFakeTeacher);
                //这俩指标不需要查 跟东伟确认了
                teacherAuthInfo.setVacnHwGroupCount(v.getVacationHwGroupCount());//布置假期作业的班组数
                teacherAuthInfo.setTermReviewGroupCount(0);//布置期末作业的班组数
            }else {
                if(DateUtils.isSameDay(new Date(),teacherAuthInfo.getRegisterDate())){//当天注册老师
                    isStudentJoin(teacherAuthInfo);
                }else{
                    teacherAuthInfo.setStudentJoin(false);
                }
                teacherAuthInfo.setUnUsed(true);
            }
            authList.add(teacherAuthInfo);
        });
        return authList;
    }
    private List<List<Long>> splitList(Collection<Long> ids, int size){
        List<List<Long>> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(ids)){
            return resultList;
        }
        if(size < 1){
            List<Long> item = new ArrayList<>(ids);
            resultList.add(item);
            return resultList;
        }
        List<Long> idList = new ArrayList<>(ids);
        Map<Integer, List<Long>> map = idList.stream().collect(Collectors.groupingBy(p -> idList.indexOf(p) / size, Collectors.toList()));
        map.values().forEach(resultList::add);
        return resultList;
    }

    private void isStudentJoin(TeacherAuthInfo teacherAuthInfo){
        List<TeacherGroupInfo> groupList = teacherResourceService.generateTeacherGroupList(teacherAuthInfo.getTeacherId());
        int classCount = 0;
        int studentCount = 0;
        if(CollectionUtils.isNotEmpty(groupList)){
            classCount = groupList.size();
            for (TeacherGroupInfo groupInfo : groupList){
                List<User> studentList = studentLoaderClient.loadGroupStudents(Collections.singleton(groupInfo.getGroupId())).get(groupInfo.getGroupId());
                studentCount += CollectionUtils.isNotEmpty(studentList) ?  studentList.size() : 0;
            }
            teacherAuthInfo.setStuCount(studentCount);
            teacherAuthInfo.setClassCount(classCount);
            double averageClazzSize =MathUtils.doubleDivide(studentCount,classCount,2);
            if(averageClazzSize > 1){
                teacherAuthInfo.setStudentJoin(true);
            }else
                teacherAuthInfo.setStudentJoin(false);
        }
        teacherAuthInfo.setStuCount(studentCount);
        teacherAuthInfo.setClassCount(classCount);

    }
}
