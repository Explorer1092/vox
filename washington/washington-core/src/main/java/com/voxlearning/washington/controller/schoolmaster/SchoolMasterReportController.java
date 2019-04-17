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

package com.voxlearning.washington.controller.schoolmaster;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.business.api.entity.ClassStudySitutation;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.business.consumer.SchoolMasterDataLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.*;

/**
 * @author fugui.chang
 * @since 2016-9-24 20:33
 */
@Controller
@RequestMapping("/schoolmaster/report")
public class SchoolMasterReportController extends AbstractController {

    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private SchoolMasterDataLoaderClient schoolMasterDataLoaderClient;

    @RequestMapping(value = "schoolsitutation.vpage", method = RequestMethod.GET)
    public String schoolSitutation(Model model) {
        ResearchStaff researchStaff = currentResearchStaff();
        Long schoolId = null;
        if (researchStaff.getManagedRegion() != null && CollectionUtils.isNotEmpty(researchStaff.getManagedRegion().getSchoolIds())) {
            schoolId = researchStaff.getManagedRegion().getSchoolIds().iterator().next();
        }
        if (schoolId == null) {
            logger.warn("SchoolMasterReportController warn :" + researchStaff.getId() + " not has school");
            model.addAttribute("infofailed", true);
            return "schoolmaster/report/schoolsitutation";
        }


        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();
        if (school == null) {
            model.addAttribute("infofailed", true);
            return "schoolmaster/report/schoolsitutation";
        }
        //默认month为第一月
        String month = getRequestString("month");
        if (StringUtils.isBlank(month)) {
            Integer currentmonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
            month = currentmonth.toString();
            if (month.length() == 1) {
                month = "0" + month;
            }
        }
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String yearmonth = year + month;

        List<Map<String, Object>> schoolSitutationInfoList = schoolMasterDataLoaderClient.getSchoolSitutaion(schoolId, SafeConverter.toLong(yearmonth));
        model.addAttribute("schoolName", school.getCname());
        model.addAttribute("month", month);
        model.addAttribute("schoolSitutationInfoList", schoolSitutationInfoList);
        return "schoolmaster/report/schoolsitutation";
    }

    @RequestMapping(value = "classstudysitutation.vpage", method = RequestMethod.GET)
    public String classStudySitutation(Model model) {
        ResearchStaff researchStaff = currentResearchStaff();

        Long schoolId = null;
        if (researchStaff.getManagedRegion() != null && CollectionUtils.isNotEmpty(researchStaff.getManagedRegion().getSchoolIds())) {
            schoolId = researchStaff.getManagedRegion().getSchoolIds().iterator().next();
        }

        if (schoolId == null) {
            logger.warn("SchoolMasterReportController warn :" + researchStaff.getId() + " not has school");
            model.addAttribute("infofailed", true);
            return "schoolmaster/report/schoolsitutation";
        }

        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();

        //入口参数 月份time=1&学科subject=CHINESE
        String month = getRequestString("month");
        if (StringUtils.isBlank(month)) {
            Integer currentmonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
            month = currentmonth.toString();
            if (month.length() == 1) {
                month = "0" + month;
            }
        }
        String subject = getRequestParameter("subject", Subject.ENGLISH.name());
        String yearmonth = Calendar.getInstance().get(Calendar.YEAR) + month;

        //数据信息  模拟数据List<ClassStudySitutation> classStudySitutations = schoolMasterDataLoaderClient.loadClassStudySitutationBySchoolIdDtSubject(11990L, 201609L, "ENGLISH");
        Map<String, List<ClassStudySitutation>> classStudySitutationData = schoolMasterDataLoaderClient.loadClassStudySitutationBySchoolIdDtSubjectData(schoolId, SafeConverter.toLong(yearmonth), subject);
        model.addAttribute("resultList", JsonUtils.toJson(classStudySitutationData));
        //学校名信息
        String title = school.getCname() + "学情分析";
        model.addAttribute("title", title);
        //学科选择
        List<Map<String, Object>> subjectData = new LinkedList<>();
        if (!school.isJuniorSchool()) {
            Boolean isMath = false;
            if (Subject.MATH == Subject.of(subject)) {
                isMath = true;
            }
            Map<String, Object> mapMath = new LinkedHashMap<>();
            mapMath.put("name", Subject.MATH.getValue());
            mapMath.put("value", Subject.MATH.name());
            mapMath.put("isSelected", isMath);
            subjectData.add(mapMath);
        }
        Boolean isEnglish = false;
        if (Subject.ENGLISH == Subject.of(subject)) {
            isEnglish = true;
        }
        Map<String, Object> mapEnghlish = new LinkedHashMap<>();
        mapEnghlish.put("name", Subject.ENGLISH.getValue());
        mapEnghlish.put("value", Subject.ENGLISH.name());
        mapEnghlish.put("isSelected", isEnglish);
        subjectData.add(mapEnghlish);
        model.addAttribute("subjectData", subjectData);
        //年级信息
        List<String> gradeList = new LinkedList<>(classStudySitutationData.keySet());
        List<Map<String, Object>> gradeData = new LinkedList<>();
        boolean flag = true;
        for (String gradestr : gradeList) {
            Map<String, Object> grade = new LinkedHashMap<>();
            grade.put("name", gradestr + "年级");
            grade.put("value", gradestr);
            grade.put("isActive", flag);
            if (flag) {
                flag = false;
            }
            gradeData.add(grade);
        }
        model.addAttribute("gradeData", gradeData);

        model.addAttribute("month", SafeConverter.toLong(month));
        model.addAttribute("firstGrade", gradeList.isEmpty() ? "0" : gradeList.get(0));
        return "schoolmaster/report/classstudysitutation";
    }

    @RequestMapping(value = "knowledgeabilityanalysis.vpage", method = RequestMethod.GET)
    public String knowledgeAbilityAnalysis(Model model) throws Exception {
        //可查询的历史学年
        SchoolYear schoolYear = SchoolYear.newInstance();
        DateRange dateRange = schoolYear.currentTermDateRange();
        List<String> historyYearStr = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(2016, 9, 1);   // 起始可查询年份
        while (calendar.getTime().before(dateRange.getEndDate())) {
            int year = calendar.get(Calendar.YEAR);
            String str = year + "-" + (year + 1);
            historyYearStr.add(str);
            calendar.add(Calendar.YEAR, 1);
        }
        //查询使用的学年信息
        String usedyear = getRequestString("year");
        if (StringUtils.isBlank(usedyear)) {
            int endYear = DayRange.newInstance(dateRange.getEndTime()).getYear();
            usedyear = (endYear - 1) + "-" + endYear;
        }
        //查询使用的学期
        String term = getRequestString("term");
        if (StringUtils.isBlank(term)) {
            term = "" + schoolYear.currentTerm().getKey();
        }
        //获取usedYear term 时的查询月份范围 ; 上下学期的开始月份与结束月份来自于Term
        Long beginDt = null, endDt = null;
        if (Term.上学期 == Term.of(SafeConverter.toInt(term))) {
            String[] yearStr = usedyear.split("-");
            beginDt = SafeConverter.toLong(yearStr[0] + "09");
            endDt = SafeConverter.toLong(yearStr[1] + "02");
        } else if (Term.下学期 == Term.of(SafeConverter.toInt(term))) {
            String[] yearStr = usedyear.split("-");
            beginDt = SafeConverter.toLong(yearStr[1] + "03");
            endDt = SafeConverter.toLong(yearStr[1] + "08");
        }

        //获取数据
        ResearchStaff researchStaff = currentResearchStaff();
        Long schoolId = null;
        if (researchStaff.getManagedRegion() != null && CollectionUtils.isNotEmpty(researchStaff.getManagedRegion().getSchoolIds())) {
            schoolId = researchStaff.getManagedRegion().getSchoolIds().iterator().next();
        }

        if (schoolId == null) {
            logger.warn("SchoolMasterReportController warn :" + researchStaff.getId() + " not has school");
            model.addAttribute("infofailed", true);
            return "schoolmaster/report/schoolsitutation";
        }

        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();
        schoolYear.getSchoolYearDateRange().getStartDate();
        schoolYear.getSchoolYearDateRange().getEndDate();
        String subject = getRequestParameter("subject", "ENGLISH");
        Map<String, List<Map<String, Object>>> knowledgeAbilityAnalysisData = schoolMasterDataLoaderClient.getKnowledgeAbilityAnalysisData(school, schoolId, subject, beginDt, endDt);
        List<String> gradeList = new LinkedList<>(knowledgeAbilityAnalysisData.keySet());
        List<Map<String, Object>> gradeData = new LinkedList<>();
        boolean flag = true;
        for (String gradestr : gradeList) {
            Map<String, Object> grade = new LinkedHashMap<>();
            grade.put("name", gradestr + "年级");
            grade.put("value", gradestr);
            grade.put("isActive", flag);
            if (flag) {
                flag = false;
            }
            gradeData.add(grade);
        }

        String title = school.getCname() + "知识能力分析";
        model.addAttribute("historyYears", historyYearStr);
        model.addAttribute("title", title);
        model.addAttribute("year", usedyear);
        model.addAttribute("term", term);
        model.addAttribute("gradeData", gradeData);
        model.addAttribute("resultList", JsonUtils.toJson(knowledgeAbilityAnalysisData));
        model.addAttribute("firstGrade", gradeList.size() > 0 ? gradeList.get(0) : "0");
        model.addAttribute("isJuniorSchool", school.isJuniorSchool());
        return "schoolmaster/report/knowledgeabilityanalysis";
    }
}
