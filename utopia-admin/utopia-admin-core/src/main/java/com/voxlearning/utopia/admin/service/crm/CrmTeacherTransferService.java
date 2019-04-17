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

package com.voxlearning.utopia.admin.service.crm;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.dao.AgentDictSchoolPersistence;
import com.voxlearning.utopia.admin.dao.AgentTaskDetailDao;
import com.voxlearning.utopia.admin.dao.CrmTaskDao;
import com.voxlearning.utopia.admin.dao.CrmTeacherTransferSchoolRecordDao;
import com.voxlearning.utopia.admin.data.TeacherTransferData;
import com.voxlearning.utopia.admin.entity.CrmTeacherTransferSchoolRecord;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.api.constant.CrmTaskStatus;
import com.voxlearning.utopia.api.constant.CrmTaskType;
import com.voxlearning.utopia.entity.agent.AgentDictSchool;
import com.voxlearning.utopia.entity.agent.AgentTaskDetail;
import com.voxlearning.utopia.entity.crm.CrmTask;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 转校审核
 * Created by Administrator on 2016/9/18.
 */
@Named
public class CrmTeacherTransferService extends AbstractAdminService {
    private static Date START_TIME = DateUtils.stringToDate("2016-09-01 00:00:00");

    @Inject private RaikouSDK raikouSDK;

    @Inject private CrmTaskService crmTaskService;
    @Inject private CrmTaskDao crmTaskDao;
    @Inject private AgentTaskDetailDao agentTaskDetailDao;
    @Inject private AgentDictSchoolPersistence agentDictSchoolPersistence;
    @Inject private CrmTeacherTransferSchoolRecordDao crmTeacherTransferSchoolRecordDao;


    public List<TeacherTransferData> loadTeacherTransferDataByTime(Date transferDate) {
        List<CrmTask> crmTasks = loadCrmTaskByTime(transferDate);
        if (CollectionUtils.isEmpty(crmTasks)) {
            return Collections.emptyList();
        }
        crmTasks = crmTasks.stream().filter(p -> p.getAgentTaskId() != null).collect(Collectors.toList());
        Set<String> taskIds = crmTasks.stream().map(CrmTask::getAgentTaskId).collect(Collectors.toSet());
        Map<String, AgentTaskDetail> agentTaskDetails = loadAgentTaskDetails(taskIds);
        if (MapUtils.isEmpty(agentTaskDetails)) {
            return Collections.emptyList();
        }
        List<AgentTaskDetail> AgentTaskDetailList = new ArrayList<>(agentTaskDetails.values());
        Set<Long> teacherIds = AgentTaskDetailList.stream().map(AgentTaskDetail::getTeacherId).collect(Collectors.toSet());
        Map<Long, Teacher> teacherInfo = teacherLoaderClient.loadTeachers(teacherIds);
        Map<Long, TeacherDetail> teacherDetailMap = teacherLoaderClient.loadTeacherDetails(teacherIds);
        List<TeacherTransferData> result = new ArrayList<>();
        crmTasks.forEach(p -> {

            String id = p.getId();
            String agentTaskId = p.getAgentTaskId();
            AgentTaskDetail agentTaskDetail = agentTaskDetails.get(agentTaskId);
            if (agentTaskId == null) {
                return;
            }
            Long teacherId = agentTaskDetail.getTeacherId();
            if (teacherId == null) {
                return;
            }
            Teacher teacher = teacherInfo.get(teacherId);
            if (teacher == null) {
                return;
            }
            TeacherDetail detail = teacherDetailMap.get(teacherId);
            if (detail == null) {
                return;
            }
            List<GroupMapper> groupMapperList = deprecatedGroupLoaderClient.loadTeacherGroupsByTeacherId(teacherId, false);
            List<String> className = new ArrayList<>();
            groupMapperList.forEach(p1 -> {
                if (teacherLoaderClient.isTeachingClazz(teacherId, p1.getClazzId())) {
                    Clazz clazzInfo = raikouSDK.getClazzClient()
                            .getClazzLoaderClient()
                            .loadClazz(p1.getClazzId());
                    if (clazzInfo == null) {
                        return;
                    }
                    className.add(clazzInfo.formalizeClazzName());
                }
            });

            Long inSchoolId = detail.getTeacherSchoolId();
            String inSchoolName = detail.getTeacherSchoolName();
            Long outSchoolId = agentTaskDetail.getSchoolId();


            TeacherTransferData data = new TeacherTransferData();
            data.setId(id);
            data.setTransferType(agentTaskDetail.getCategory().getValue());
            data.setTransferDate(p.getUpdateTime());
            data.setTeacherId(teacher.getId());
            data.setTeacherName(SafeConverter.toString(teacher.getProfile() != null ? teacher.getProfile().getRealname() : ""));

            UserAuthentication ua = userLoaderClient.loadUserAuthentication(teacher.getId());
            data.setTeacherMobile(SafeConverter.toString(ua != null ? ua.getSensitiveMobile() : ""));

            data.setAuthenticationState(teacher.fetchCertificationState().getDescription());

            data.setTransferOutSchoolId(outSchoolId);                 // 转出学校
            data.setTransferOutSchoolName(agentTaskDetail.getSchoolName());
            data.setIsEmphasisOutSchool(isSchoolDictSchool(outSchoolId));
            data.setBroughtClass(className);                           // 老师现在的班级
            data.setTransferInSchoolId(inSchoolId);                    // 转入学校
            data.setTransferInSchoolName(inSchoolName);
            data.setIsEmphasisInSchool(isSchoolDictSchool(inSchoolId));

            data.setApplicantName(agentTaskDetail.getExecutorName());
            data.setApplicantMobile(agentTaskDetail.getExecutorMobile());
            data.setTaskContent(agentTaskDetail.getContent());

            data.setExecutorName(p.getExecutorName());              //操作人

            data.setOtherLinkMan(p.getOtherLinkMan());
            data.setAffirmTransferSchool(p.getAffirmTransferSchool());
            data.setAffirmTransferClass(p.getAffirmTransferClass());
            data.setTransferSchoolReason(p.getTransferSchoolReason());
            data.setRemark(p.getRemark());
            data.setIsProof(SafeConverter.toBoolean(p.getIsProof()));
            result.add(data);
        });

        return result;
    }

    public Boolean isSchoolDictSchool(Long schoolId) {
        List<AgentDictSchool> agentDictSchoolList = agentDictSchoolPersistence.findBySchoolId(schoolId);
        return CollectionUtils.isNotEmpty(agentDictSchoolList);
    }

    private Map<String, AgentTaskDetail> loadAgentTaskDetails(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return agentTaskDetailDao.loads(ids);
    }

    private List<CrmTask> loadCrmTaskByTime(Date transferDate) {
        Date createStart = transferDate == null ? START_TIME : DateUtils.getDayStart(transferDate);
        Date createEnd = transferDate == null ? null : DateUtils.getDayEnd(transferDate);
        return crmTaskDao.findByTime(createStart, createEnd, CrmTaskType.老师转校, CrmTaskStatus.FINISHED);
    }

    public MapMessage addTeacherTransferExtInfo(AuthCurrentAdminUser user, String id, String otherLinkMan, Boolean affirmTransferSchool, Boolean affirmTransferClass, String transferSchoolReason, String remark) {
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("审核记录的ID不存在");
        }
        CrmTask task = crmTaskService.loadTask(id);
        if (task == null) {
            return MapMessage.errorMessage("审核记录不存在");
        }
        task.setOtherLinkMan(otherLinkMan);
        task.setAffirmTransferClass(affirmTransferClass);
        task.setAffirmTransferClass(affirmTransferSchool);
        task.setTransferSchoolReason(transferSchoolReason);
        task.setRemark(remark);
        task.setAuditorId(user.getAdminUserName());
        task.setAuditorName(user.getAdminUserName());
        task.setIsProof(Boolean.TRUE);
        crmTaskDao.update(id, task);
        return MapMessage.successMessage();
    }

    public List<CrmTeacherTransferSchoolRecord> findCrmTeacherTransferSchoolRecords(Boolean isSourceSchoolDict, CrmTeacherTransferSchoolRecord.ChangeType changeType, Boolean authenticationState, CrmTeacherTransferSchoolRecord.CheckResult checkResult) {
        //isSourceSchoolDict 为null表示查询时不分是否是重点校;authenticationState为null表示查询时老师不分是否认证;changeType为null表示查询时不分是否是带班转校
        if (checkResult == null) {
            return Collections.emptyList();
        }
        return crmTeacherTransferSchoolRecordDao.findCrmTeacherTransferSchoolRecords(isSourceSchoolDict, changeType, authenticationState, checkResult);
    }

    public CrmTeacherTransferSchoolRecord loadCrmTeacherTransferSchoolRecord(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return crmTeacherTransferSchoolRecordDao.load(id);
    }

    public void upsertCrmTeacherTransferSchoolRecord(CrmTeacherTransferSchoolRecord crmTeacherTransferSchoolRecord) {
        if (crmTeacherTransferSchoolRecord == null) {
            return;
        }
        crmTeacherTransferSchoolRecordDao.upsert(crmTeacherTransferSchoolRecord);
    }

}
