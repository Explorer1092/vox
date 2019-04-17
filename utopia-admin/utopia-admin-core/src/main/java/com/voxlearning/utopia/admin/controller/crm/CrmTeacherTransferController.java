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

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.data.TeacherTransferData;
import com.voxlearning.utopia.admin.entity.CrmTeacherTransferSchoolRecord;
import com.voxlearning.utopia.admin.service.crm.CrmTeacherTransferService;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 老师转校
 * Created by yaguang.wang on 2016/9/18.
 */
@Controller
@RequestMapping("/crm/teachertransfer")
public class CrmTeacherTransferController extends CrmAbstractController {

    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private CrmTeacherTransferService crmTeacherTransferService;

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String teacherTransferIndex(Model model) {
        String transferTime = getRequestString("transferDate");
        Date transferDate = DateUtils.stringToDate(transferTime, DateUtils.FORMAT_SQL_DATE);
        List<TeacherTransferData> dataResult = crmTeacherTransferService.loadTeacherTransferDataByTime(transferDate);
        model.addAttribute("transferDate", transferTime);
        model.addAttribute("dataResult", dataResult);
        return "crm/teacher/teacherTransfer";
    }

    @RequestMapping(value = "update_review_info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateReviewInfo() {
        AuthCurrentAdminUser user = getCurrentAdminUser();
        String id = getRequestString("id");
        String otherLinkMan = getRequestString("otherLinkMan");
        Boolean affirmTransferSchool = getRequestBool("affirmTransferSchool");
        Boolean affirmTransferClass = getRequestBool("affirmTransferClass");
        String transferSchoolReason = getRequestString("transferSchoolReason");
        String remark = getRequestString("remark");
        return crmTeacherTransferService.addTeacherTransferExtInfo(user, id, otherLinkMan, affirmTransferSchool, affirmTransferClass, transferSchoolReason, remark);
    }

    @RequestMapping(value = "teacherTransferSchool.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String teacherTransferSchool() {
        return "crm/teacher/teacherTransferSchool";
    }

    @RequestMapping(value = "teacherTransferSchoolInfo.vpage", method = RequestMethod.POST)
    public String teacherTransferSchoolInfo(Model model) {
        String sourceSchoolDictStr = getRequestParameter("sourceSchoolDict", "");
        String changeTypeStr = getRequestParameter("changeType", "");
        String authenticationStateStr = getRequestParameter("authenticationState", "");
        String checkResultStr = getRequestParameter("checkResult", "");
        int currentPage = SafeConverter.toInt(getRequestParameter("currentPage", ""));
        Boolean isSourceSchoolDict = null;
        Boolean authenticationState = null;
        CrmTeacherTransferSchoolRecord.ChangeType changeType = null;
        if (!StringUtils.equals(sourceSchoolDictStr, "all")) {
            isSourceSchoolDict = SafeConverter.toBoolean(sourceSchoolDictStr);
        }
        if (!StringUtils.equals(authenticationStateStr, "all")) {
            authenticationState = SafeConverter.toBoolean(authenticationStateStr);
        }
        if (!StringUtils.equals(changeTypeStr, "ALL")) {
            changeType = CrmTeacherTransferSchoolRecord.ChangeType.valueOf(changeTypeStr);
        }
        CrmTeacherTransferSchoolRecord.CheckResult checkResult = CrmTeacherTransferSchoolRecord.CheckResult.valueOf(checkResultStr);
        ;
        Map<String, String> conditions = new HashMap<>();
        conditions.put("sourceSchoolDict", sourceSchoolDictStr);
        conditions.put("changeType", changeTypeStr);
        conditions.put("authenticationState", authenticationStateStr);
        conditions.put("checkResult", checkResultStr);


        List<CrmTeacherTransferSchoolRecord> crmTeacherTransferSchoolRecords = crmTeacherTransferService.findCrmTeacherTransferSchoolRecords(isSourceSchoolDict, changeType, authenticationState, checkResult);

        if (currentPage == 0) {
            currentPage = 1;
        }

        int pageSize = 30;
        int beginIndex = pageSize * (currentPage - 1);
        int endIndex = pageSize * currentPage;
        if (endIndex > crmTeacherTransferSchoolRecords.size()) {
            endIndex = crmTeacherTransferSchoolRecords.size();
        }
        int totalPage;
        if (crmTeacherTransferSchoolRecords.size() % pageSize == 0) {
            totalPage = crmTeacherTransferSchoolRecords.size() / pageSize;
        } else {
            totalPage = crmTeacherTransferSchoolRecords.size() / pageSize + 1;
        }
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("conditions", conditions);

        Set<Long> teacherIds = new HashSet<>();
        Set<Long> schoolIds = new HashSet<>();
        crmTeacherTransferSchoolRecords.subList(beginIndex, endIndex).forEach(crmTeacherTransferSchoolRecord -> {
            teacherIds.add(crmTeacherTransferSchoolRecord.getTeacherId());
            schoolIds.add(crmTeacherTransferSchoolRecord.getSourceSchoolId());
            schoolIds.add(crmTeacherTransferSchoolRecord.getTargetSchoolId());
        });

        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIds);
        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolIds)
                .getUninterruptibly();

        List<Map<String, Object>> usedInfo = crmTeacherTransferSchoolRecords.subList(beginIndex, endIndex).stream().map(crmTeacherTransferSchoolRecord -> {
            Map<String, Object> tempMap = new HashMap<>();
            Teacher tempTeacher = teacherMap.get(crmTeacherTransferSchoolRecord.getTeacherId());
            School tempSourceSchool = schoolMap.get(crmTeacherTransferSchoolRecord.getSourceSchoolId());
            School tempTargetSchool = schoolMap.get(crmTeacherTransferSchoolRecord.getTargetSchoolId());

            tempMap.put("teacherName", tempTeacher == null ? crmTeacherTransferSchoolRecord.getTeacherId().toString() : tempTeacher.fetchRealname());
            tempMap.put("sourceSchoolName", tempSourceSchool == null ? crmTeacherTransferSchoolRecord.getSourceSchoolId().toString() : tempSourceSchool.getCname());
            tempMap.put("targetSchoolName", tempTargetSchool == null ? crmTeacherTransferSchoolRecord.getTargetSchoolId().toString() : tempTargetSchool.getCname());

            tempMap.put("id", crmTeacherTransferSchoolRecord.getId());
            tempMap.put("teacherId", crmTeacherTransferSchoolRecord.getTeacherId());
            tempMap.put("authenticationState", crmTeacherTransferSchoolRecord.getAuthenticationState());
            tempMap.put("changeType", crmTeacherTransferSchoolRecord.getChangeType().name());
            tempMap.put("sourceSchoolId", crmTeacherTransferSchoolRecord.getSourceSchoolId());
            tempMap.put("targetSchoolId", crmTeacherTransferSchoolRecord.getTargetSchoolId());
            tempMap.put("operator", crmTeacherTransferSchoolRecord.getOperator());
            tempMap.put("operationTime", crmTeacherTransferSchoolRecord.getOperationTime());
            tempMap.put("changeSchoolDesc", crmTeacherTransferSchoolRecord.getChangeSchoolDesc());
            tempMap.put("checkOperator", crmTeacherTransferSchoolRecord.getCheckOperator());
            tempMap.put("checkResult", crmTeacherTransferSchoolRecord.getCheckResult());
            tempMap.put("checkDesc", crmTeacherTransferSchoolRecord.getCheckDesc());
            tempMap.put("updateTime", crmTeacherTransferSchoolRecord.getUpdateTime());

            return tempMap;
        }).collect(Collectors.toList());

        model.addAttribute("crmTeacherTransferSchoolRecords", usedInfo);
        if (CollectionUtils.isEmpty(crmTeacherTransferSchoolRecords)) {
            model.addAttribute("nodata", true);
        }
        model.addAttribute("checkResultType", checkResult);
        return "crm/teacher/teacherTransferSchool";
    }

    @RequestMapping(value = "setcheckresult.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setCheckResult() {
        String id = getRequestString("id");
        String checkResult = getRequestString("checkResult");

        String checkOperator = getCurrentAdminUser().getAdminUserName();

        CrmTeacherTransferSchoolRecord crmTeacherTransferSchoolRecord = crmTeacherTransferService.loadCrmTeacherTransferSchoolRecord(id);
        if (crmTeacherTransferSchoolRecord == null) {
            return MapMessage.errorMessage("数据异常,请刷新后重试");
        }
        if (crmTeacherTransferSchoolRecord.getCheckResult() == CrmTeacherTransferSchoolRecord.CheckResult.TRUE) {
            return MapMessage.errorMessage("数据已经被操作为正确");
        }

        if (crmTeacherTransferSchoolRecord.getCheckResult() == CrmTeacherTransferSchoolRecord.CheckResult.FALSE) {
            return MapMessage.errorMessage("数据已经被操作为错误,判错描述是" + crmTeacherTransferSchoolRecord.getCheckDesc());
        }

        if (StringUtils.equals(checkResult, CrmTeacherTransferSchoolRecord.CheckResult.TRUE.name())) {
            crmTeacherTransferSchoolRecord.setCheckResult(CrmTeacherTransferSchoolRecord.CheckResult.TRUE);
        } else if (StringUtils.equals(checkResult, CrmTeacherTransferSchoolRecord.CheckResult.FALSE.name())) {
            String checkDesc = getRequestString("checkDesc").trim();
            if (StringUtils.isBlank(checkDesc)) {
                return MapMessage.errorMessage("请填写判错原因!");
            }
            crmTeacherTransferSchoolRecord.setCheckResult(CrmTeacherTransferSchoolRecord.CheckResult.FALSE);
            crmTeacherTransferSchoolRecord.setCheckDesc(checkDesc);
        }
        crmTeacherTransferSchoolRecord.setCheckOperator(checkOperator);
        crmTeacherTransferService.upsertCrmTeacherTransferSchoolRecord(crmTeacherTransferSchoolRecord);
        return MapMessage.successMessage();
    }

}
