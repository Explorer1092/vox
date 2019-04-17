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

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.dao.CrmUGCSchoolDao;
import com.voxlearning.utopia.admin.dao.CrmUGCSchoolDetailDao;
import com.voxlearning.utopia.admin.persist.entity.AdminLog;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCSchool;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCSchoolDetail;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCSchoolTask;
import com.voxlearning.utopia.service.crm.client.AdminLogServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.AuthenticationSource;
import com.voxlearning.utopia.service.user.api.entities.School;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Zhuan liu
 * @since 2015/12/31.
 */
@Named
public class CrmUGCSchoolService extends AbstractAdminService {

    private static final int UGC_SCHOOLNAME_TRIGGER_TYPE = 0; // 触发条件包括两种类型
    private static final int UGC_SCHOOLNAME_CHANGED = 3;//答案不唯一
    private static final int UGC_SCHOOLNAME_TASK_ASSIGNED = 100;//已下发任务
    private static final String DISPATCH_TASK_URL;

    static {
        String domain;
        if (RuntimeMode.isProduction()) {
            domain = "http://marketing.oaloft.com/";
        } else if (RuntimeMode.isStaging()) {
            domain = "http://10.0.1.106:16590/";
        } else if (RuntimeMode.isTest()) {
            domain = "http://marketing.test.17zuoye.net/";
        } else {
            domain = "http://localhost:8083/";
        }
        DISPATCH_TASK_URL = domain + "mobile/task/dispatch_ugc_school_task.vpage";
    }

    @Inject private RaikouSystem raikouSystem;
    @Inject private AdminLogServiceClient adminLogServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject CrmUGCSchoolDetailDao crmUGCSchoolDetailDao;
    @Inject CrmUGCSchoolDao crmUGCSchoolDao;

    public CrmUGCSchool load(Long schoolId) {
        if (schoolId == null) {
            return null;
        }
        List<CrmUGCSchool> ugcSchools = crmUGCSchoolDao.findSchoolIdIs(schoolId);
        return CollectionUtils.isEmpty(ugcSchools) ? null : ugcSchools.get(0);
    }

    public Page<CrmUGCSchool> crmUGCSchools(Integer triggerType, Integer checkupStatus, Boolean isTaskFinished, Pageable pageable) {
        Page<CrmUGCSchool> result;
        switch (triggerType) {
            case UGC_SCHOOLNAME_TRIGGER_TYPE:
                result = crmUGCSchoolDao.ugcSchoolFindIn(Arrays.asList(1, 2), checkupStatus, pageable);
                fillUgcSchoolShortNameAndStatus(result);
                break;
            case UGC_SCHOOLNAME_CHANGED:
                result = crmUGCSchoolDao.ugcSchoolAnswerChange(UGC_SCHOOLNAME_CHANGED, checkupStatus, pageable);
                fillUgcSchoolShortNameAndStatus(result);
                break;
            case UGC_SCHOOLNAME_TASK_ASSIGNED:
                result = crmUGCSchoolDao.ugcSchoolTaskAssignedIs(true, isTaskFinished, pageable);
                fillUgcSchoolShortNameAndStatus(result);
                break;
            default:
                result = crmUGCSchoolDao.ugcSchoolFindIs(triggerType, checkupStatus, pageable);
                fillUgcSchoolShortNameAndStatus(result);
                break;
        }
        return result;
    }

    private void fillUgcSchoolShortNameAndStatus(Page<CrmUGCSchool> result) {
        List<Long> schoolIds = result.getContent().stream().map(CrmUGCSchool::getSchoolId).collect(Collectors.toList());
        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolIds)
                .getUninterruptibly();

        List<CrmUGCSchool> temp = result.getContent();
        for (CrmUGCSchool crmUGCSchool : temp) {
            School school = schoolMap.get(crmUGCSchool.getSchoolId());
            if (school != null) {
                crmUGCSchool.setSchoolName(school.getCname());
                crmUGCSchool.setShortName(school.getShortName());
                crmUGCSchool.setAuthStatus(school.getAuthenticationState());
            }
        }
    }

    public List<CrmUGCSchool> allTriggerTypeSchoolExportData(int limit, int skip) {
        return crmUGCSchoolDao.allUgcSchoolData(limit, skip);
    }

    public List<CrmUGCSchoolDetail> allSchoolDetailExportData(int limit, int skip) {
        return crmUGCSchoolDetailDao.allUgcSchoolDetailData(limit, skip);
    }

    public List<CrmUGCSchoolDetail> ugcSchoolDetail(Long schoolId) {
        return crmUGCSchoolDetailDao.ugcSchoolDetail(schoolId);
    }

    public void updateSchoolShortName(Long schoolId, String schoolShortName, AuthCurrentAdminUser adminUser, String actionUrl) {
        School school = raikouSystem.loadSchoolIncludeDisabled(schoolId);
        if (school != null) {
            School source = new School();
            BeanUtils.copyProperties(school, source);
            school.setShortName(schoolShortName);
            school.setAuthenticationState(AuthenticationState.SUCCESS.getState());
            school.setAuthenticationSource(AuthenticationSource.UGC_MANUALLY);
            deprecatedSchoolServiceClient.getRemoteReference().upsertSchool(school, adminUser.getAdminUserName());
            schoolTouchLog(source, school, adminUser, actionUrl);
        }
    }

    private void schoolTouchLog(School source, School target, AuthCurrentAdminUser adminUser, String actionUrl) {
        StringBuilder builder = new StringBuilder();
        if (!target.getShortName().equals(source.getShortName())) {
            builder.append("shortName:").append(source.getShortName()).append("->").append(target.getShortName()).append("; ");
        }
        if (!target.getAuthenticationState().equals(source.getAuthenticationState())) {
            builder.append("authenticationState:").append(source.getAuthenticationState()).append("->").append(target.getAuthenticationState()).append("; ");
        }
        if (target.getAuthenticationSource() != source.getAuthenticationSource()) {
            builder.append("authenticationSource:").append(source.getAuthenticationSource()).append("->").append(target.getAuthenticationSource()).append("; ");
        }
        if (builder.length() > 0) {
            AdminLog adminLog = new AdminLog();
            String admin = adminUser.getAdminUserName();
            adminLog.setAdminUserName(admin);
            adminLog.setCreateDatetime(new Timestamp(System.currentTimeMillis()));
            Long schoolId = source.getId();
            String operation = "管理员[" + admin + "] 修改学校简称[" + schoolId + "]";
            adminLog.setOperation(operation);
            adminLog.setWebActionUrl(actionUrl);
            adminLog.setTargetId(schoolId);
            adminLog.setTargetData(builder.toString());
            adminLog.setComment("UGC学校简称人工修改，同步修改关联的学校信息");
            adminLogServiceClient.getAdminLogService()
                    .persistAdminLog(adminLog)
                    .awaitUninterruptibly();
        }
    }

    public long getUgcSchoolCount() {
        return crmUGCSchoolDao.getUgcSchoolCount();
    }

    public long getUgcSchoolDetailCount() {
        return crmUGCSchoolDetailDao.getUgcSchoolDetailCount();
    }

    public List<CrmUGCSchool> getUgcSchoolInfo(Long schoolId) {
        return crmUGCSchoolDao.getUgcSchoolInfo(schoolId);
    }

    public CrmUGCSchoolTask dispatchUGCSchoolTask(Long schoolId, Boolean branchSchool, AuthCurrentAdminUser adminUser) {
        if (schoolId == null) {
            return null;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("schoolId", schoolId);
        params.put("branchSchool", branchSchool);
        params.put("creater", adminUser.getAdminUserName());
        params.put("createrName", adminUser.getRealName());
        long timestamp = System.currentTimeMillis();
        params.put("timestamp", timestamp);
        String sign = taskPushSign(String.valueOf(schoolId), timestamp);
        params.put("sign", sign);
        String url = UrlUtils.buildUrlQuery(DISPATCH_TASK_URL, params);
        String response = HttpRequestExecutor.defaultInstance().post(url).execute().getResponseString();
        CrmUGCSchoolTask ugcSchoolTask = null;
        try {
            ugcSchoolTask = JsonUtils.fromJson(response, CrmUGCSchoolTask.class);
        } catch (Exception e) {
            logger.error("Dispatch UGCSchoolTask Excp : {}; schoolId = {}, response = {}", e, schoolId, response);
        }
        if (ugcSchoolTask != null) {
            CrmUGCSchool ugcSchool = load(schoolId);
            if (ugcSchool != null) {
                ugcSchool.assignTask();
                crmUGCSchoolDao.update(ugcSchool.getId(), ugcSchool);
            }
        }
        return ugcSchoolTask;
    }

    public static String taskPushSign(String key, long timestamp) {
        return DigestUtils.md5Hex(key + Constants.FLOW_TASK_PUSH_SECRET + timestamp);
    }
}
