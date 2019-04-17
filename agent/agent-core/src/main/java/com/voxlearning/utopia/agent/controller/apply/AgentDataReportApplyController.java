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

package com.voxlearning.utopia.agent.controller.apply;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.hierarchicalstructure.TreeNode;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.apply.AgentDataReportApplyService;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author song.wang
 * @date 2017/6/7
 */

@Controller
@RequestMapping("/apply/data_report")
public class AgentDataReportApplyController extends AbstractAgentController {

    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject private AgentDataReportApplyService agentDataReportApplyService;

    @RequestMapping("/data_report.vpage")
    public String createPage(Model model) {

        AuthCurrentUser user = getCurrentUser();

        // 学科列表
        List<Integer> subjectList = Arrays.asList(1, 2);
        model.addAttribute("subjectList", subjectList);
        // 级别
        List<Integer> reportLevelList = new ArrayList<>();
        if (user.isRegionManager() || user.isCityManager()) {
            reportLevelList.add(1);
        }
        reportLevelList.add(2);
        reportLevelList.add(3);
        model.addAttribute("reportLevelList", reportLevelList);

        // 区域
        List<TreeNode> regionCategory = agentDataReportApplyService.generateCategory(user);
        model.addAttribute("regionCategory", JsonUtils.toJson(regionCategory));

        return "apply/datareport/data_report_apply";
    }

    @RequestMapping(value = "add_data_report.vpage", method = RequestMethod.POST)
    @ResponseBody
    @OperationCode("5f90f6c26e134e2f")
    public MapMessage addDataReport() {
        Integer subject = getRequestInt("subject");
        Integer reportLevel = getRequestInt("reportLevel");
        Integer city = getRequestInt("city");
        Integer county = getRequestInt("county");
        Long schoolId = getRequestLong("schoolId");
        Integer englishStartGrade = getRequestInt("englishStartGrade");
        Integer reportType = getRequestInt("reportType");
        Integer reportTerm = getRequestInt("reportTerm");
        Integer reportMonth = getRequestInt("reportMonth");
        Long sampleSchoolId = getRequestLong("sampleSchoolId");
        String comment = getRequestString("comment");
        return agentDataReportApplyService.addDataReportApply(subject, reportLevel, city, county, schoolId, englishStartGrade,
                reportType, reportTerm, reportMonth, sampleSchoolId, comment);
    }

    @RequestMapping(value = "search_school.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage searchSchool(@RequestBody String schoolJson) {
        List<Long> schoolIds = JsonUtils.fromJsonToList(schoolJson, Long.class);
        List<Long> manageSchoolIds = baseOrgService.getManagedSchoolList(getCurrentUserId());
        Integer schoolLevel = getRequestInt("schoolLevel");
        if (schoolLevel == 0) {
            schoolLevel = 1;
        }
        if (CollectionUtils.isEmpty(schoolIds)) {
            return MapMessage.errorMessage("需要查询的学校信息有误");
        }
        if (CollectionUtils.isEmpty(manageSchoolIds)) {
            return MapMessage.errorMessage(StringUtils.formatMessage("该用户的权限下未找到学校"));
        }
        MapMessage result = MapMessage.successMessage();
        List<Long> success = new ArrayList<>();
        List<Long> error = new ArrayList<>();
        List<Long> notLevel = new ArrayList<>();
        schoolIds.forEach(p -> {
            if (manageSchoolIds.contains(p)) {
                success.add(p);
            } else {
                error.add(p);
            }
        });
        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(success)
                .getUninterruptibly();
        Integer level = schoolLevel;
        List<School> values = schoolMap.values().stream().filter(p -> Objects.equals(p.getLevel(), level)).collect(Collectors.toList());
        notLevel.addAll(schoolMap.values().stream().filter(p -> !Objects.equals(p.getLevel(), level)).map(School::getId).collect(Collectors.toList()));
        result.add("successSchool", values);
        result.add("errorSchool", error);
        result.add("notLevel", notLevel);
        return result;
    }

    /**
     * 补充workflowId对应的审核记录的文档
     *
     * @return
     */
    @RequestMapping(value = "add_document.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addDocument() {
        String firstDocument = getRequestString("firstDocument");
        String secondDocument = getRequestString("secondDocument");
        Long workflowId = getRequestLong("workflowId");
        return agentDataReportApplyService.addDocument(workflowId, firstDocument, secondDocument);
    }

}
