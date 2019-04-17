package com.voxlearning.utopia.agent.listener.handler;


import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.dao.mongo.AgentNeedFollowUpDao;
import com.voxlearning.utopia.agent.persist.entity.AgentNeedFollowTeacher;
import com.voxlearning.utopia.agent.persist.entity.AgentNeedFollowUp;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.api.constant.CrmTeacherFakeValidationType;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.crm.CrmClueType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.entities.crm.CrmTeacherClue;
import com.voxlearning.utopia.service.crm.consumer.loader.crm.CrmTeacherClueLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.crm.CrmTeacherClueServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * NeedFollowUpHandler
 * 首页线索数据生成
 *
 * @author song.wang
 * @date 2016/7/29
 */
@Named
public class NeedFollowUpHandler extends SpringContainerSupport {

    @Inject private CrmTeacherClueLoaderClient crmTeacherClueLoaderClient;
    @Inject private CrmTeacherClueServiceClient crmTeacherClueServiceClient;

    @Inject
    private AgentNeedFollowUpDao agentNeedFollowUpDao;
    @Inject
    BaseOrgService baseOrgService;
    @Inject
    CrmSummaryLoaderClient crmSummaryLoaderClient;

    public void executeCommand(Integer day) {
        createNeedFollowData(AgentRoleType.BusinessDeveloper, day);
    }

    private void createNeedFollowData(AgentRoleType roleType, Integer day) {

        // 获取指定角色所有的用户
        AgentGroup marketGroup = baseOrgService.getGroupByName("市场部");
        if (marketGroup == null) {
            return;
        }

        List<AgentNeedFollowUp> needFollowUpList = new ArrayList<>();
        List<Long> userIdList = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(marketGroup.getId(), roleType.getId());
        for (Long userId : userIdList) {
            List<Long> schoolIdList = baseOrgService.getUserSchools(userId);
            Map<Long, CrmSchoolSummary> schoolSummaryMap = crmSummaryLoaderClient.loadSchoolSummary(schoolIdList);
            if (MapUtils.isEmpty(schoolSummaryMap)) {
                continue;
            }

            List<CrmSchoolSummary> schoolSummaryList = new ArrayList<>(schoolSummaryMap.values());
            if (CollectionUtils.isNotEmpty(schoolSummaryList)) {
                // 生成 15天未拜访的学校， 出现新注册老师的数据
                // 过滤出 15 天未拜访的学校
                Date endDate = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
                Date startDate = DayUtils.addDay(endDate, -15);
                List<CrmSchoolSummary> targetSchoolList = schoolSummaryList.stream().filter(p -> p != null && !DayUtils.judgeDateRange(startDate, endDate, p.getLatestVisitTime())).collect(Collectors.toList());
                List<AgentNeedFollowUp> newRegTeacherFollowDataList = createNewRegTeacherFollowData(day, targetSchoolList);
                if (CollectionUtils.isNotEmpty(newRegTeacherFollowDataList)) {
                    needFollowUpList.addAll(newRegTeacherFollowDataList);
                }


                // 生成 9月1日布置过作业，但最近20天没布置作业的数据
                List<AgentNeedFollowUp> noActiveTeacherFollowDataList = createNoActiveTeacherFollowData(day, schoolSummaryList);
                if (CollectionUtils.isNotEmpty(noActiveTeacherFollowDataList)) {
                    needFollowUpList.addAll(noActiveTeacherFollowDataList);
                }

                // 生成满足条件为认证老师的线索数据  客服端设置，市场app端显示
                List<AgentNeedFollowUp> noAuthTeacherFollowDataList = createNoAuthTeacherFollowData(day, schoolSummaryList);
                if (CollectionUtils.isNotEmpty(noAuthTeacherFollowDataList)) {
                    needFollowUpList.addAll(noAuthTeacherFollowDataList);
                }
            }

            if (CollectionUtils.isNotEmpty(needFollowUpList)) {
                agentNeedFollowUpDao.inserts(needFollowUpList);
                needFollowUpList.clear();
            }
        }
    }

    private List<AgentNeedFollowUp> createNewRegTeacherFollowData(Integer day, List<CrmSchoolSummary> schoolSummaryList) {
        if (CollectionUtils.isEmpty(schoolSummaryList)) {
            return null;
        }

        Date endDate = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
        Date startDate = DayUtils.addDay(endDate, -15);
        // TODO 上线时需放开 从 9 月 15 日开始推送新注册老师的数据
        if (DayUtils.getMonth(startDate) < Calendar.SEPTEMBER) {
            return Collections.emptyList();
        }

        List<AgentNeedFollowUp> retList = new ArrayList<>();

        for (CrmSchoolSummary schoolSummary : schoolSummaryList) {
            List<CrmTeacherSummary> teacherSummaryList = crmSummaryLoaderClient.loadSchoolTeachers(schoolSummary.getSchoolId());
            if (CollectionUtils.isEmpty(teacherSummaryList)) {
                continue;
            }
            teacherSummaryList = teacherSummaryList.stream().filter(p -> (Boolean.FALSE.equals(p.getFakeTeacher()) || !CrmTeacherFakeValidationType.MANUAL_VALIDATION.getName().equals(p.getValidationType())) && DayUtils.judgeDateRange(startDate, endDate, p.fetchRegisterTimeStamp())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(teacherSummaryList)) {
                continue;
            }
            AgentNeedFollowUp needFollowUp = new AgentNeedFollowUp();
            needFollowUp.setDay(day);
            needFollowUp.setType(1);
            needFollowUp.setSchoolId(schoolSummary.getSchoolId());
            needFollowUp.setSchoolName(schoolSummary.getSchoolName());

            List<AgentNeedFollowTeacher> teacherList = new ArrayList<>();
            for (CrmTeacherSummary teacherSummary : teacherSummaryList) {
                AgentNeedFollowTeacher teacher = new AgentNeedFollowTeacher();
                teacher.setTeacherId(teacherSummary.getTeacherId());
                teacher.setTeacherName(teacherSummary.getRealName());
                teacher.setSubject(teacherSummary.getSubject());
                teacherList.add(teacher);
            }
            needFollowUp.setTeacherList(teacherList);
            retList.add(needFollowUp);
        }

        return retList;
    }


    private List<AgentNeedFollowUp> createNoActiveTeacherFollowData(Integer day, List<CrmSchoolSummary> schoolSummaryList) {
        if (CollectionUtils.isEmpty(schoolSummaryList)) {
            return null;
        }
        Date endDate = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
        Date startDate = DayUtils.addDay(endDate, -20);

        // TODO 上线是需放开该段  从 9 月 21 日开始推送不活跃老师的数据
        if (DayUtils.getMonth(startDate) < Calendar.SEPTEMBER) {
            return Collections.emptyList();
        }
        Date spetemberStart = DateUtils.stringToDate("20160901", "yyyyMMdd");


        List<AgentNeedFollowUp> retList = new ArrayList();
        for (CrmSchoolSummary schoolSummary : schoolSummaryList) {


            List<CrmTeacherSummary> teacherSummaryList = crmSummaryLoaderClient.loadSchoolTeachers(schoolSummary.getSchoolId());
            if (CollectionUtils.isEmpty(teacherSummaryList)) {
                continue;
            }
            // 9月1日以后布置过作业， 但最近20 天没布置过作业的老师
            teacherSummaryList = teacherSummaryList.stream().filter(p -> (Boolean.FALSE.equals(p.getFakeTeacher()) || !CrmTeacherFakeValidationType.MANUAL_VALIDATION.getName().equals(p.getValidationType())) && DayUtils.judgeDateRange(spetemberStart, endDate, p.getLatestAssignHomeworkTime()) && !DayUtils.judgeDateRange(startDate, endDate, p.getLatestAssignHomeworkTime())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(teacherSummaryList)) {
                continue;
            }

            AgentNeedFollowUp needFollowUp = new AgentNeedFollowUp();
            needFollowUp.setDay(day);
            needFollowUp.setType(2);
            needFollowUp.setSchoolId(schoolSummary.getSchoolId());
            needFollowUp.setSchoolName(schoolSummary.getSchoolName());

            List<AgentNeedFollowTeacher> teacherList = new ArrayList<>();
            for (CrmTeacherSummary teacherSummary : teacherSummaryList) {
                AgentNeedFollowTeacher teacher = new AgentNeedFollowTeacher();
                teacher.setTeacherId(teacherSummary.getTeacherId());
                teacher.setTeacherName(teacherSummary.getRealName());
                teacher.setSubject(teacherSummary.getSubject());
                teacherList.add(teacher);
            }
            needFollowUp.setTeacherList(teacherList);
            retList.add(needFollowUp);
        }
        return retList;
    }


    private List<AgentNeedFollowUp> createNoAuthTeacherFollowData(Integer day, List<CrmSchoolSummary> schoolSummaryList) {
        if (CollectionUtils.isEmpty(schoolSummaryList)) {
            return null;
        }
        Date endDate = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
        Date startDate = DayUtils.addDay(endDate, -1);


        List<AgentNeedFollowUp> retList = new ArrayList();
        for (CrmSchoolSummary schoolSummary : schoolSummaryList) {
            List<CrmTeacherClue> teacherClueList = crmTeacherClueLoaderClient.findBySchoolId(schoolSummary.getSchoolId(), CrmClueType.核实老师认证, startDate, endDate);
            if (CollectionUtils.isEmpty(teacherClueList)) {
                continue;
            }
            AgentNeedFollowUp needFollowUp = new AgentNeedFollowUp();
            needFollowUp.setDay(day);
            needFollowUp.setType(3);
            needFollowUp.setSchoolId(schoolSummary.getSchoolId());
            needFollowUp.setSchoolName(schoolSummary.getSchoolName());

            List<AgentNeedFollowTeacher> teacherList = new ArrayList<>();
            for (CrmTeacherClue teacherClue : teacherClueList) {
                AgentNeedFollowTeacher teacher = new AgentNeedFollowTeacher();
                teacher.setTeacherId(teacherClue.getTeacherId());
                teacher.setTeacherName(teacherClue.getTeacherName());
                teacher.setSubject(teacherClue.getSubject());
                teacherList.add(teacher);
            }
            needFollowUp.setTeacherList(teacherList);
            retList.add(needFollowUp);

            // 设置线索的接收者
            List<AgentUser> userList = baseOrgService.getSchoolManager(schoolSummary.getSchoolId());
            if (CollectionUtils.isNotEmpty(userList)) {
                for (AgentUser user : userList) {
                    AgentRoleType userRole = baseOrgService.getUserRole(user.getId());
                    if (AgentRoleType.BusinessDeveloper == userRole) {
                        teacherClueList.forEach(p -> {
                            p.setReceiver(user.getRealName());
                            crmTeacherClueServiceClient.replace(p);
                        });
                        break;
                    }
                }
            }
        }
        return retList;
    }

}
