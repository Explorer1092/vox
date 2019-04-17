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

package com.voxlearning.utopia.schedule.schedule;

import com.unitever.cif.core.query.Query;
import com.unitever.cif.operation.AgentTemplate;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.cjlschool.support.CJLDataRequestTemplate;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLConstants;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLEntityType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * 请求陈经纶学校全量数据
 * Only Invoke Manually
 * CIFTeacher --> CIFEclass --> CIFStudents --> CIFTeacherCourse
 * Please Synchronize Data This Order
 * Created by Yuechen.Wang on 2017/07/21.
 */
@Named
@ScheduledJobDefinition(
        jobName = "陈经纶学校同步数据",
        jobDescription = "同步陈经纶学校数据",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.TEST, Mode.STAGING, Mode.PRODUCTION},
        cronExpression = "0 0 4 * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
public class SyncCJLSchoolDataJob extends ScheduledJobWithJournalSupport {

    @Inject private CJLDataRequestTemplate cjlDataRequestTemplate;
    private AgentTemplate agentTemplate;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        agentTemplate = cjlDataRequestTemplate.getTemplate();
        if (agentTemplate == null) {
            return;
        }
        String type = SafeConverter.toString(parameters.get("type"));
        CJLEntityType entityType = CJLEntityType.parse(type);
        if (StringUtils.isBlank(type) || entityType == null) {
            logger.info("Invalid CJLSchool Data Entity Type Found : {}, And Just Ignore It.", type);
            return;
        }

        logger.info("Start Sync CJLSchoolData, type= " + entityType);

        progressMonitor.worked(10);

        final String CJLHighSchoolId = CJLConstants.DEFAULT_CJL_HIGH_SCHOOL_ID;  // FIXME 本次只做本部高中的
        final String CJLHighSchoolTermId = "20170206145725872498463825652963";
        try {
            switch (entityType) {
                case CLASS:
                    requestSchoolClasses(CJLHighSchoolId);
                    break;
                case TEACHER:
                    requestSchoolTeachers(CJLHighSchoolId);
                    break;
                case STUDENT:
                    requestSchoolStudents(CJLHighSchoolId);
                    break;
                case TEACHER_COURSE:
                    requestCurrentTeacherCourse(CJLHighSchoolTermId);
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            logger.error("Failed Process CJLData For Some Reason, type ={}  ", entityType, ex);
        }

        progressMonitor.done();
    }

    private void requestSchoolClasses(String schoolId) {
        if (StringUtils.isBlank(schoolId)) {
            return;
        }
        Query query = new Query(CJLEntityType.CLASS.getEntityName());
        query.addConditionEq("id_school", schoolId);
        agentTemplate.request(query, "CIFDefault");
    }

    private void requestSchoolTeachers(String schoolId) {
        if (StringUtils.isBlank(schoolId)) {
            return;
        }
        Query query = new Query(CJLEntityType.TEACHER.getEntityName());
        query.addConditionEq("id_school", schoolId);
        agentTemplate.request(query);
    }

    private void requestSchoolStudents(String schoolId) {
        if (StringUtils.isBlank(schoolId)) {
            return;
        }
        Query query = new Query(CJLEntityType.STUDENT.getEntityName());
        query.addConditionEq("id_school", schoolId);
        agentTemplate.request(query, "CIFDefault");
    }

    private void requestCurrentTeacherCourse(String schoolTermId) {
        Query query = new Query(CJLEntityType.TEACHER_COURSE.getEntityName());
        query.addConditionEq("e.id_schoolterm", schoolTermId);
        agentTemplate.request(query, "CIFDefault");
    }

}
