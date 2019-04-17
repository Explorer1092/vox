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

package com.voxlearning.washington.controller.rstaff;

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.business.api.entity.RSPaperAnalysisReport;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.Book;
import com.voxlearning.utopia.service.content.api.entity.MathBook;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.rstaff.api.constans.*;
import com.voxlearning.utopia.service.rstaff.consumer.SchoolMasterServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import com.voxlearning.washington.controller.schoolmaster.SchoolMasterBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Maofeng Lu
 * @since 13-8-6 下午6:03
 */
@Controller
@RequestMapping("/rstaff")
public class ResearchStaffController extends SchoolMasterBaseController {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;
    @Inject private SchoolMasterServiceClient schoolMasterServiceClient;

    /**
     * NEW 教研员首页
     * 教研员首页  标签1 - 金币 --> 试卷统计
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        ResearchStaff researchStaff = currentResearchStaff();

        if (researchStaff != null
                && (researchStaff.getKtwelve() == Ktwelve.JUNIOR_SCHOOL || researchStaff.getKtwelve() == Ktwelve.SENIOR_SCHOOL)
                && researchStaff.isResearchStaff()) {
            return "redirect:" + ProductConfig.getKuailexueUrl();
        }

        if (researchStaff != null && researchStaff.isAffairTeacher()) { //教务老师账号
            return "redirect:/specialteacher/index.vpage";
        }

        String result = validatePasswdAndMobile(researchStaff.getId());
        if (StringUtils.isNotBlank(result)) {
            return result;
        }

        if (researchStaff != null && researchStaff.isPresident()) { //校长账号
            return "redirect:/schoolmasterHomepage/index.vpage";
            //return "redirect:/schoolmaster/generaloverview.vpage";
        }

        // 数学教研员进入行为数据
//        if (researchStaff.getKtwelve().equals(Ktwelve.PRIMARY_SCHOOL) && researchStaff.getSubject() == Subject.MATH) {
//            return "redirect:/rstaff/report/behaviordata.vpage";
//        } else if (researchStaff.getKtwelve().equals(Ktwelve.JUNIOR_SCHOOL)) {
//            return "redirect:/rstaff/oral/index.vpage";
//        } else {
//            // 知识数据
//            return "redirect:/rstaff/report/knowledgedata.vpage";
//        }
        if (researchStaff.getRegion() == null) {
            model.addAttribute("message", String.format("请先设置教研员【ID:%d】的区域信息，客服热线 400-160-1717", researchStaff.getId()));
            return "common/message";
        }
        return "redirect:/rstaff/generaloverview.vpage";
    }

    /**
     * 教研员总体概览页面
     *
     * @return
     */
    @RequestMapping(value = "generaloverview.vpage", method = RequestMethod.GET)
    public String generalOverview(Model model) {
        ResearchStaff researchStaff = currentResearchStaff();
        if (researchStaff == null) {
            return "redirect:" + getBaseDomain();
        }
        String result = validatePasswdAndMobile(researchStaff.getId());
        if (StringUtils.isNotBlank(result)) {
            return result;
        }
        model.addAttribute("idType", "rstaff");
        model.addAttribute("pageType", "generaloverview");
        return "adminteacher/researchstaff/generaloverview";
    }

    /**
     * 教研员学情分析页面
     *
     * @return
     */
    @RequestMapping(value = "learninganalysis.vpage", method = RequestMethod.GET)
    public String learningAnalysis(Model model) {
        ResearchStaff researchStaff = currentResearchStaff();
        if (researchStaff == null) {
            return "redirect:" + getBaseDomain();
        }
        String result = validatePasswdAndMobile(researchStaff.getId());
        if (StringUtils.isNotBlank(result)) {
            return result;
        }
        model.addAttribute("idType", "rstaff");
        model.addAttribute("pageType", "learninganalysis");
        return "adminteacher/researchstaff/learninganalysis";
    }

    /**
     * 教研员模考统测页面
     *
     * @return
     */
    @RequestMapping(value = "systemtest.vpage", method = RequestMethod.GET)
    public String systemTest(Model model) {
        //在这里确定登录的人员是校长，还是市教研员，区教研员，还是街道教研员
        ResearchStaff researchStaff = currentResearchStaff();
        String regionLevel = "";
        if (researchStaff == null) {
            return "redirect:" + getBaseDomain();
        }
        String result = validatePasswdAndMobile(researchStaff.getId());
        if (StringUtils.isNotBlank(result)) {
            return result;
        }
        Set<Long> cityCodes = researchStaff.getManagedRegion().getCityCodes();//市code
        if (CollectionUtils.isNotEmpty(cityCodes)) {
            regionLevel = "city";
        }
        Set<Long> regionCodes = researchStaff.getManagedRegion().getAreaCodes();//区code
        if (CollectionUtils.isNotEmpty(regionCodes)) {
            regionLevel = "county";
        }
        Set<Long> schoolIds = researchStaff.getManagedRegion().getSchoolIds();
        if (CollectionUtils.isNotEmpty(schoolIds)) {
            regionLevel = "school";
        }
        model.addAttribute("idType", "rstaff");
        model.addAttribute("pageType", "systemtest");
        model.addAttribute("regionLevel", regionLevel);
        return "adminteacher/researchstaff/systemtest";
    }

    /**
     * 教研员模考报告页面
     *
     * @return
     */
    @RequestMapping(value = "testreport.vpage", method = RequestMethod.GET)
    public String testReport(Model model) {
        model.addAttribute("idType", "rstaff");
        model.addAttribute("pageType", "testreport");
        return "adminteacher/testreport/index";
    }

    /**
     * 教研员个人中心页面
     *
     * @return
     */
    @RequestMapping(value = "admincenter.vpage", method = RequestMethod.GET)
    public String adminCenter(Model model) {
        model.addAttribute("idType", "rstaff");
        model.addAttribute("pageType", "admincenter");
        return "adminteacher/personalcenter/index";
    }

    /**
     * NEW 教研员
     * 组卷统考 - 组卷
     */
    @RequestMapping(value = "testpaper/index.vpage", method = RequestMethod.GET)
    public String testPaper(Model model) {
        //上学期
        model.addAttribute("lastTerm", Term.上学期);
        //下学期
        model.addAttribute("nextTerm", Term.下学期);
        //教研员所拥有的书(只查询3-6年级的书)
        ResearchStaff researchStaff = currentResearchStaff();
        switch (researchStaff.getSubject()) {
            case ENGLISH:
                List<Book> rstaffBookList = userBookLoaderClient.loadUserEnglishBooks(currentUser());
                engPaintedSkin(rstaffBookList);
                model.addAttribute("rstaffBookList", rstaffBookList);
                break;
            case MATH:
                List<MathBook> rstaffMathBookList = userBookLoaderClient.loadUserMathBooks(currentUser());
                mathPaintedSkin(rstaffMathBookList);
                model.addAttribute("rstaffBookList", rstaffMathBookList);
                break;
            default:
                break;
        }
        //从新建试卷跳回所带bookId参数
        model.addAttribute("bookId", getRequestLong("bookId"));
        return "rstaffv3/testpaper/index";
    }

    /**
     * NEW 教研员
     * <p>
     * 根据区域编码来获得该区域下所有学校
     * <p>
     * schoolCode 学校编码
     */
    @RequestMapping(value = "getschool.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSchool() {
        int schoolCode = getRequestInt("schoolCode");
        MapMessage mesg = new MapMessage();
        List<KeyValuePair<Long, String>> pairs = new ArrayList<>();
        List<School> schools = loadAuthenticatedSchoolsUnderRegion(schoolCode);
        if (null == schools || schools.isEmpty()) {
            mesg.setInfo("没有此地区学校的数据");
            mesg.setSuccess(true);
        } else {
            for (School school : schools) {
                KeyValuePair<Long, String> pair = new KeyValuePair<>();
                pair.setKey(school.getId());
                pair.setValue(school.getShortName());
                pairs.add(pair);
            }
            mesg.add("rows", pairs);
            mesg.add("total", pairs.size());
            mesg.setInfo("学校信息列表");
            mesg.setSuccess(true);
        }
        return mesg;
    }


    /**
     * NEW 教研员
     * 组卷统考 -- 试卷及报告
     */
    @RequestMapping(value = "testpaper/paperreport/list.vpage", method = RequestMethod.GET)
    public String getResearchStaffUsedBooks(Model model) {
        return "redirect:/rstaff/report/behaviordata.vpage";
    }

    /**
     * NEW 教研员
     * 组卷统考 -- 试卷及报告 -- 展开
     * 通过paperIds获取试卷详细信息
     */
    @RequestMapping(value = "testpaper/paperreport/paper.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getResearchStaffPaperListChip() {
        return MapMessage.errorMessage("功能已下线");
    }


    /**
     * NEW 教研员
     * 组卷统考 -- 试卷及报告 --本学期报告
     */
    @RequestMapping(value = "testpaper/paperreport/reportdetail.vpage", method = RequestMethod.GET)
    public String getPaperReportDetail(Model model) {
        ResearchStaff researchStaff = currentResearchStaff();
        String regionCode = getRequestParameter("regionCode", "");
        String paperId = getRequestParameter("paperId", "");
        List<RSPaperAnalysisReport> rsPaperAnalysisReports;
        if (StringUtils.isBlank(regionCode)) {
//            rsPaperAnalysisReports = researchStaffServiceClient.getRemoteReference()
//                    .getPaperAnalysisReport(paperId, researchStaff.getRegionCode(), researchStaff.getRegionType());
            rsPaperAnalysisReports = researchStaffServiceClient.getRemoteReference()
                    .getPaperAnalysisReport(paperId, researchStaff.getManagedRegion().getCityCodes(),
                            researchStaff.getManagedRegion().getAreaCodes(),
                            researchStaff.getManagedRegion().getSchoolIds());
        } else {
            rsPaperAnalysisReports = researchStaffServiceClient.getRemoteReference()
                    .getPaperAnalysisReport(paperId, Integer.valueOf(regionCode), RegionType.COUNTY);
        }
        model.addAttribute("reportDetailList", rsPaperAnalysisReports);
        return "rstaffv3/testpaper/paperreport/reportdetail";
    }

    /**
     * 教研员体验布置作业
     * -----获取更多课外阅读练习
     */
    @RequestMapping(value = "reading/search.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage readingSearch(@RequestBody Map<String, Object> jsonMap) {
        return MapMessage.errorMessage("功能已下线");
    }


    /**
     * 教研员体验布置作业
     * ----根据单元ID推送应试题目
     */
    @RequestMapping(value = "exam/{unitId}.vpage", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getFollowerPaperInterface(@PathVariable("unitId") Long unitId) {
        return Collections.emptyList();
    }

    /**
     * 教研员体验布置作业
     * ----更换教材
     */
    @RequestMapping(value = "homework/changebook.vpage", method = RequestMethod.GET)
    public String changebook(Model model) {
        return "redirect:/rstaff/index.vpage";
    }

    /**
     * Load all authenticated schools under specified region.
     *
     * @param regionCode the region codes.
     * @return all authenticated schools.
     */
    private List<School> loadAuthenticatedSchoolsUnderRegion(Integer regionCode) {
        List<ExRegion> regions = new LinkedList<>();
        ExRegion region = raikouSystem.loadRegion(regionCode);
        if (region != null) {
            regions.add(region);
            regions.addAll(ExRegion.fetchAllChildren(region));
            Collections.sort(regions);
        }
        if (regions.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Integer> regionCodes = regions.stream()
                .map(ExRegion::getCode)
                .collect(Collectors.toSet());

        return raikouSystem.querySchoolLocations(regionCodes)
                .enabled()
                .authenticated()
                .transform()
                .asList()
                .stream()
                .sorted((o1, o2) -> {
                    String n1 = StringUtils.defaultString(o1.getCname());
                    String n2 = StringUtils.defaultString(o2.getCname());
                    return n1.compareTo(n2);
                })
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "loadResearchUsageData.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadResearchUsageData(Model model) {
        MapMessage result = new MapMessage();
        try {
            ResearchStaff researchStaff = currentResearchStaff();
            if (Objects.isNull(researchStaff)) {
                return MapMessage.errorMessage("请重新登录");
            }
            String res = validatePasswdAndMobile(researchStaff.getId());
            if (StringUtils.isNotBlank(res)) {
                return MapMessage.errorMessage("请绑定手机或者重置秘密");
            }
            List<Long> schoolIds = new ArrayList<>(researchStaff.getManagedRegion().getSchoolIds());
            List<Integer> regionCodes = getRegionCodesParam(researchStaff);
            List<Integer> cityCodes = getCityCodesParam(researchStaff);
            String subject = researchStaff.getSubject().toString();
            Date curDate = new Date();
            int day = curDate.getDate();
            Calendar cal = Calendar.getInstance();
            if (day <= 3) {
                cal.add(Calendar.MONTH, -1);
            }
            String dateStr = DateUtils.dateToString(cal.getTime(), "yyyyMM");

            Map<String, Object> data = schoolMasterServiceClient.loadResearchUsageData(schoolIds, regionCodes, cityCodes, subject, dateStr);
            if (data == null) {
                result.add("result", false);
                result.add("info", "没有数据");
                return result;
            }
            //教研员总使用人数
            long usageTeachers = (long) data.get("usageTeachers");
            //教研员总使用人数
            long usageStudents = (long) data.get("usageStudents");
            //教研员老师总使用人数
            long increaseTeachers = (long) data.get("increaseTeachers");
            //教研员学生总使用人数
            long increaseStudents = (long) data.get("increaseStudents");

            result.add("usageTeachers", usageTeachers);
            result.add("usageStudents", usageStudents);
            result.add("increaseTeachers", increaseTeachers);
            result.add("increaseStudents", increaseStudents);
            result.set("result", true);
        } catch (Exception e) {
            logger.info("获取使用情况数据异常", e);
            result.add("result", false);
            result.add("info", "没有数据");
        }
        return result;
    }

    @RequestMapping(value = "loadResearchHomeworkCondition.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadResearchHomeworkCondition(Model model) {
        MapMessage result = new MapMessage();
        //月份  ，如果当前时间是六月四号，就是包含六月份，如果当前时间是六月三号之前不包含当前月份
        Date curDate = new Date();
        int day = curDate.getDate();
        Calendar cal = Calendar.getInstance();
        if (day <= 3) {
            cal.add(Calendar.MONTH, -6);
        } else {
            cal.add(Calendar.MONTH, -5);
        }

        List<Map<String, String>> dateList = new LinkedList<>();
        for (int i = 0; i < 6; i++) {
            String value = DateUtils.dateToString(cal.getTime(), "yyyyMM");
            String text = DateUtils.dateToString(cal.getTime(), "yyyy年MM月");
            Map<String, String> dateMap = new LinkedHashMap<>();
            dateMap.put("name", text);
            dateMap.put("value", value);
            if (value.compareTo("201806") >= 0) {
                dateList.add(dateMap);
            }
            cal.add(Calendar.MONTH, 1);
        }
        result.add("dateList", dateList);

        List<Map<String, String>> gradeList = new LinkedList<>();
        Map<String, String> temp0 = new LinkedHashMap<>();
        temp0.put("name", "全部年级");
        temp0.put("value", "0");
        gradeList.add(temp0);
        List<String> tempGrade = getGradeList();
        for (int i = 1; i <= tempGrade.size(); i++) {
            Map<String, String> temp = new LinkedHashMap<>();
            temp.put("name", tempGrade.get(i - 1));
            temp.put("value", "" + i);
            gradeList.add(temp);
        }
        result.add("gradeList", gradeList);
        result.add("result", true);
        return result;
    }

    @RequestMapping(value = "loadResearchHomework.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadResearchHomework(Model model) {
        MapMessage result = new MapMessage();
        try {
            ResearchStaff researchStaff = currentResearchStaff();
            if (Objects.isNull(researchStaff)) {
                return MapMessage.errorMessage("请重新登录");
            }
            String res = validatePasswdAndMobile(researchStaff.getId());
            if (StringUtils.isNotBlank(res)) {
                return MapMessage.errorMessage("请绑定手机或者重置秘密");
            }
            List<Long> schoolIds = new ArrayList<>(researchStaff.getManagedRegion().getSchoolIds());
            List<Integer> regionCodes = getRegionCodesParam(researchStaff);
            List<Integer> cityCodes = getCityCodesParam(researchStaff);
            String subject = researchStaff.getSubject().toString();
            String dateStr = getRequestString("dateStr");
            String grade = getRequestString("grade");
            Map<Long, School> schoolMap = raikouSystem.loadSchools(schoolIds);

            Map<String, Object> data = schoolMasterServiceClient.loadResearchHomework(schoolMap, regionCodes, cityCodes, grade, subject, dateStr);
            if (data == null) {
                result.add("result", false);
                result.add("info", "没有数据");
                return result;
            }

            //饼图
            Map<String, Integer> homeworkScenes = (Map<String, Integer>) data.get("homeworkScenes");
            //饼图的数据
            List<String> pieLegendData = new LinkedList<>();
            pieLegendData.addAll(homeworkScenes.keySet());
            Collections.sort(pieLegendData);
            List<Map<String, Object>> pieSeriesData = new LinkedList<>();
            for (String scenes : pieLegendData) {
                Integer counts = homeworkScenes.get(scenes);
                Map<String, Object> pieData = new LinkedHashMap<>();
                pieData.put("name", scenes);
                pieData.put("value", counts);
                pieSeriesData.add(pieData);
            }
            result.add("pieLegendData", pieLegendData);
            result.add("pieSeriesData", pieSeriesData);

            //柱图数据
            Map<String, Map<String, Integer>> weekHomeworkScenes = (Map<String, Map<String, Integer>>) data.get("weekHomeworkScenes");
            Iterator<String> tempWeeks = weekHomeworkScenes.keySet().iterator();
            Set<String> barScenesSet = new HashSet();
            while (tempWeeks.hasNext()) {
                String week = tempWeeks.next();
                Map<String, Integer> scenesMap = weekHomeworkScenes.get(week);
                barScenesSet.addAll(scenesMap.keySet());
            }
            List<String> barLegendData = new LinkedList<>();
            barLegendData.addAll(barScenesSet);
            Collections.sort(barLegendData);

            Set<String> barWeekData = new TreeSet<>();
            List<Map<String, Object>> barSeriesData = new LinkedList<>();
            for (String scenes : barLegendData) {
                List<Integer> countsList = new LinkedList<>();
                Iterator<String> weeks = weekHomeworkScenes.keySet().iterator();
                while (weeks.hasNext()) {
                    String week = weeks.next();
                    barWeekData.add(week);
                    Map<String, Integer> countMap = weekHomeworkScenes.get(week);
                    Integer counts = countMap.get(scenes);
                    if (counts == null) {
                        counts = 0;
                    }
                    countsList.add(counts);
                }
                Map<String, Object> tempBarMap = new LinkedHashMap<>();
                tempBarMap.put("name", scenes);
                tempBarMap.put("data", countsList);
                barSeriesData.add(tempBarMap);
            }

            result.add("barWeekData", barWeekData);
            result.add("barLegendData", barLegendData);
            result.add("barSeriesData", barSeriesData);


            //班级作业排名，  sortMapByValue
            Map<String, Integer> clazzDoHomeworkCounts = (Map<String, Integer>) data.get("clazzDoHomeworkCounts");
            Map<String, Integer> clazzDoHomeworkData = sortMapByValue(clazzDoHomeworkCounts);
            List<Map<String, Object>> clazzDoHomeworkDataL = new LinkedList();
            if (MapUtils.isNotEmpty(clazzDoHomeworkData)) {
                for (Map.Entry<String, Integer> entry : clazzDoHomeworkData.entrySet()) {
                    Map<String, Object> temp = new LinkedHashMap<>();
                    temp.put("school", entry.getKey());
                    temp.put("value", entry.getValue());
                    if (entry.getValue().intValue() > 0) {
                        clazzDoHomeworkDataL.add(temp);
                    }
                }
            }
            reckonRanking(clazzDoHomeworkDataL);
            if (clazzDoHomeworkDataL.size() > 5) {
                result.add("schoolDoHomeworkData", clazzDoHomeworkDataL.subList(0, 5));
            } else {
                result.add("schoolDoHomeworkData", clazzDoHomeworkDataL);
            }

            //老师排名
            Map<String, Integer> assignmentCounts = (Map<String, Integer>) data.get("assignmentCounts");
            Map<String, Integer> assignmentData = sortMapByValue(assignmentCounts);
            List<Map<String, Object>> assignmentDataL = new LinkedList();
            if (MapUtils.isNotEmpty(assignmentData)) {
                for (Map.Entry<String, Integer> entry : assignmentData.entrySet()) {
                    Map<String, Object> temp = new LinkedHashMap<>();
                    String[] names = entry.getKey().split("-*-");
                    temp.put("school", names[2]);
                    temp.put("teacher", names[0]);
                    temp.put("value", entry.getValue());
                    if (entry.getValue().intValue() > 0) {
                        assignmentDataL.add(temp);
                    }
                }
            }
            reckonRanking(assignmentDataL);
            if (assignmentDataL.size() > 5) {
                result.add("schoolAssignmentData", assignmentDataL.subList(0, 5));
            } else {
                result.add("schoolAssignmentData", assignmentDataL);
            }

            result.set("result", true);
        } catch (Exception e) {
            logger.error("教研员获取作业情况数据异常", e);
            result.add("result", false);
            result.add("info", "没有数据");
        }
        return result;
    }

    @RequestMapping(value = "loadUnitAvgResearchCondition.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadUnitAvgResearchCondition(Model model) {
        MapMessage result = new MapMessage();
        //年级
        List<Map<String, String>> gradeList = new LinkedList<>();
        List<String> tempGrade = getGradeList();
        for (int i = 1; i <= tempGrade.size(); i++) {
            Map<String, String> temp = new LinkedHashMap<>();
            temp.put("name", tempGrade.get(i - 1));
            temp.put("value", "" + i);
            gradeList.add(temp);
        }
        result.add("gradeList", gradeList);

        result.add("termList", getTerms());
        //家长教研员所管辖的学校
        List<Map<String, Object>> citySchoolList = new LinkedList<>();
        ResearchStaff researchStaff = currentResearchStaff();
        Set<Long> schoolIds = researchStaff.getManagedRegion().getSchoolIds();
        Set<Long> areaCodes = researchStaff.getManagedRegion().getAreaCodes();
        Set<Long> cityCodes = researchStaff.getManagedRegion().getCityCodes();
        if (CollectionUtils.isNotEmpty(schoolIds)) {
            Map<Long, School> schoolMap = raikouSystem.loadSchools(schoolIds);
            List<Map<String, Object>> schoolList = new LinkedList<>();
            Integer regionCode = 0;
            for (Map.Entry<Long, School> entry : schoolMap.entrySet()) {
                School school = entry.getValue();
                if (SchoolLevel.JUNIOR.getLevel() == school.getLevel().intValue()) {
                    Map<String, Object> temp = new LinkedHashMap<>();
                    temp.put("schoolId", entry.getKey());
                    temp.put("schoolName", StringUtils.isNotEmpty(school.getShortName()) ? school.getShortName() : school.getCname());
                    regionCode = entry.getValue().getRegionCode();
                    schoolList.add(temp);
                }

            }
            Map<String, Object> cityMap = new LinkedHashMap<>();
            ExRegion exRegion = raikouSystem.loadRegion(regionCode);
            cityMap.put("cityName", exRegion.getCountyName());
            cityMap.put("cityId", exRegion.getCountyCode());
            cityMap.put("schoolList", schoolList);
            citySchoolList.add(cityMap);
        }

        if (CollectionUtils.isNotEmpty(areaCodes)) {
            Iterator<Long> it = areaCodes.iterator();
            while (it.hasNext()) {
                Integer regionCode = SafeConverter.toInt(it.next());
                List<School> schools = raikouSystem.querySchoolLocations(regionCode)
                        .transform()
                        .asList();

                List<Map<String, Object>> schoolList = new LinkedList<>();
                for (School school : schools) {
                    if (!school.getDisabled()) {
                        if (SchoolLevel.JUNIOR.getLevel() == school.getLevel().intValue()) {
                            Map<String, Object> temp = new LinkedHashMap<>();
                            temp.put("schoolId", school.getId());
                            temp.put("schoolName", StringUtils.isNotEmpty(school.getShortName()) ? school.getShortName() : school.getCname());
                            schoolList.add(temp);
                        }
                    }
                }
                Map<String, Object> cityMap = new LinkedHashMap<>();
                ExRegion exRegion = raikouSystem.loadRegion(regionCode);
                cityMap.put("cityName", exRegion.getCountyName());
                cityMap.put("cityId", exRegion.getCountyCode());
                cityMap.put("schoolList", schoolList);
                citySchoolList.add(cityMap);
            }
        }

        if (CollectionUtils.isNotEmpty(cityCodes)) {
            Iterator<Long> it = cityCodes.iterator();
            while (it.hasNext()) {
                Integer cityCode = SafeConverter.toInt(it.next());
                ExRegion cityExRegion = raikouSystem.loadRegion(cityCode);
                List<ExRegion> regionList = cityExRegion.getChildren();
                for (ExRegion exRegion : regionList) {
                    Integer regionCode = exRegion.getCountyCode();
                    List<Map<String, Object>> schoolList = new LinkedList<>();
                    List<School> schools = raikouSystem.querySchoolLocations(regionCode)
                            .transform()
                            .asList();
                    for (School school : schools) {
                        if (!school.getDisabled()) {
                            if (SchoolLevel.JUNIOR.getLevel() == school.getLevel().intValue()) {
                                Map<String, Object> temp = new LinkedHashMap<>();
                                temp.put("schoolId", school.getId());
                                temp.put("schoolName", StringUtils.isNotEmpty(school.getShortName()) ? school.getShortName() : school.getCname());
                                schoolList.add(temp);
                            }
                        }
                    }
                    Map<String, Object> cityMap = new LinkedHashMap<>();
                    cityMap.put("cityName", exRegion.getCountyName());
                    cityMap.put("cityId", exRegion.getCountyCode());
                    cityMap.put("schoolList", schoolList);
                    citySchoolList.add(cityMap);
                }
            }
        }
        result.add("citySchoolList", citySchoolList);
        result.add("result", true);
        return result;
    }


    @RequestMapping(value = "completionSchool.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map completionSchool(Model model) {
        MapMessage result = new MapMessage();
        String schoolName = getRequestString("schoolName");

        //家长教研员所管辖的学校
        List<Map<String, Object>> schoolList = new LinkedList<>();
        ResearchStaff researchStaff = currentResearchStaff();
        Set<Long> schoolIds = researchStaff.getManagedRegion().getSchoolIds();
        Set<Long> areaCodes = researchStaff.getManagedRegion().getAreaCodes();
        Set<Long> cityCodes = researchStaff.getManagedRegion().getCityCodes();
        if (CollectionUtils.isNotEmpty(schoolIds)) {
            Map<Long, School> schoolMap = raikouSystem.loadSchools(schoolIds);
            for (Map.Entry<Long, School> entry : schoolMap.entrySet()) {
                School school = entry.getValue();
                if (SchoolLevel.JUNIOR.getLevel() == school.getLevel().intValue()) {
                    Map<String, Object> temp = new LinkedHashMap<>();
                    temp.put("cityId", entry.getValue().getRegionCode());
                    temp.put("schoolId", entry.getKey());
                    temp.put("schoolName", StringUtils.isNotEmpty(school.getShortName()) ? school.getShortName() : school.getCname());
                    schoolList.add(temp);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(areaCodes)) {
            List<Integer> areaCodes1 = new LinkedList<>();
            Iterator<Long> it = areaCodes.iterator();
            while (it.hasNext()) {
                areaCodes1.add(SafeConverter.toInt(it.next()));
            }
            List<School> schools = raikouSystem.querySchoolLocations(areaCodes1)
                    .transform()
                    .asList();
            for (School school : schools) {
                if (!school.getDisabled()) {
                    if (SchoolLevel.JUNIOR.getLevel() == school.getLevel().intValue()) {
                        Map<String, Object> temp = new LinkedHashMap<>();
                        temp.put("cityId", school.getRegionCode());
                        temp.put("schoolId", school.getId());
                        temp.put("schoolName", StringUtils.isNotEmpty(school.getShortName()) ? school.getShortName() : school.getCname());
                        schoolList.add(temp);
                    }
                }
            }
        }

        if (CollectionUtils.isNotEmpty(cityCodes)) {
            Iterator<Long> it = cityCodes.iterator();
            while (it.hasNext()) {
                Integer cityCode = SafeConverter.toInt(it.next());
                ExRegion cityExRegion = raikouSystem.loadRegion(cityCode);
                List<ExRegion> regionList = cityExRegion.getChildren();
                for (ExRegion exRegion : regionList) {
                    Integer regionCode = exRegion.getCountyCode();
                    List<School> schools = raikouSystem.querySchoolLocations(regionCode)
                            .transform()
                            .asList();
                    for (School school : schools) {
                        if (!school.getDisabled()) {
                            if (SchoolLevel.JUNIOR.getLevel() == school.getLevel().intValue()) {
                                Map<String, Object> temp = new LinkedHashMap<>();
                                temp.put("cityId", school.getRegionCode());
                                temp.put("schoolId", school.getId());
                                temp.put("schoolName", StringUtils.isNotEmpty(school.getShortName()) ? school.getShortName() : school.getCname());
                                schoolList.add(temp);
                            }
                        }
                    }
                }
            }
        }

        List<Map<String, Object>> newSchoolList = new LinkedList<>();
        for (Map<String, Object> temp : schoolList) {
            String tempSchoolName = (String) temp.get("schoolName");
            if (tempSchoolName.contains(schoolName)) {
                newSchoolList.add(temp);
            }
        }
        //根据学校名字去匹配相似的school
        result.add("schoolList", newSchoolList);
        result.add("result", true);
        return result;
    }


    @RequestMapping(value = "loadResearchUnitAvgQuestions.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadResearchUnitAvgQuestions(Model model) {
        MapMessage result = new MapMessage();
        try {
            ResearchStaff researchStaff = currentResearchStaff();
            if (Objects.isNull(researchStaff)) {
                return MapMessage.errorMessage("请重新登录");
            }
            String res = validatePasswdAndMobile(researchStaff.getId());
            if (StringUtils.isNotBlank(res)) {
                return MapMessage.errorMessage("请绑定手机或者重置秘密");
            }
            Long schoolId = getRequestLong("schoolId");
            School school = raikouSystem.loadSchool(schoolId);
            Integer regionCode = school.getRegionCode();
            String subject = researchStaff.getSubject().toString();
            String grade = getRequestString("grade");
            String schoolYearTerm = getRequestString("schoolYearTerm"); //17~18下学期
            String schoolYear = schoolYearTerm.split("-")[0];
            String term = schoolYearTerm.split("-")[1];
            String clazz = getRequestString("clazz");
            if (StringUtils.isEmpty(clazz)) {
                clazz = "0";
            }

            Map<String, Object> data = schoolMasterServiceClient.loadResearchUnitAvgQuestions(schoolId, regionCode, subject, grade, clazz, schoolYear, term);
            if (data == null) {
                result.add("result", false);
                result.add("info", "暂无数据");
                return result;
            }
            String countyName = raikouSystem.loadRegion(school.getRegionCode()).getCountyName();
            List<String> legendData = new LinkedList<>();
            legendData.add(school.getShortName() + " 人均题数");
            legendData.add(countyName + " 人均题数");
            legendData.add(school.getShortName() + " 正确率");
            legendData.add(countyName + " 正确率");
            result.add("legendData", legendData);
            Set<String> unitNameList = new TreeSet<>();

            Map<String, Double> unitAvgQuestions = (Map<String, Double>) data.get("unitAvgQuestions");
            Map<String, Double> areaUnitAvgQuestions = (Map<String, Double>) data.get("areaUnitAvgQuestions");
            Map<String, Double> unitQuestionRightRatios = (Map<String, Double>) data.get("unitQuestionRightRatio");
            Map<String, Double> areaUnitQuestionRightRatios = (Map<String, Double>) data.get("areaUnitQuestionRightRatio");
            unitNameList.addAll(unitAvgQuestions.keySet());

            String bookId = (String) data.get("bookId");
            List<NewBookCatalog> unitList = newContentLoaderClient.loadChildren(Collections.singleton(bookId), BookCatalogType.UNIT)
                    .getOrDefault(bookId, Collections.emptyList()).stream().sorted(new NewBookCatalog.RankComparator()).collect(Collectors.toList());

            //按照unitList的顺序去排序unitNameList
            List<String> newUnitNameList = new LinkedList<>();
            List<String> unitNames = new LinkedList<>();
            for (NewBookCatalog newBookCatalog : unitList) {
                String unitId = newBookCatalog.getId();
                for (String unitIdandName : unitNameList) {
                    String tempUnitId = unitIdandName.split("-*-")[0];
                    String tempUnitName = unitIdandName.split("-*-")[2];
                    if (unitId.equals(tempUnitId)) {
                        newUnitNameList.add(tempUnitId + "-*-" + tempUnitName);
                        unitNames.add(tempUnitName);
                    }
                }
            }
            result.add("unitNames", unitNames);

            List<Double> unitAvgQuestionsList = new LinkedList<>();
            List<Double> areaUnitAvgQuestionsList = new LinkedList<>();
            List<Double> unitQuestionRightRatioList = new LinkedList<>();
            List<Double> areaUnitQuestionRightRatioList = new LinkedList<>();

            DecimalFormat dfFormat = new DecimalFormat("#.00");
            for (String unitIdAndUnitName : newUnitNameList) {
                Double unitQuestions = unitAvgQuestions.get(unitIdAndUnitName);
                if (unitQuestions == null) {
                    unitQuestions = 0D;
                }
                unitAvgQuestionsList.add(Double.valueOf(dfFormat.format(unitQuestions)));

                Double areaUnitQuestions = areaUnitAvgQuestions.get(unitIdAndUnitName);
                if (areaUnitQuestions == null) {
                    areaUnitQuestions = 0D;
                }
                areaUnitAvgQuestionsList.add(Double.valueOf(dfFormat.format(areaUnitQuestions)));

                Double unitQuestionRightRatio = SafeConverter.toDouble(unitQuestionRightRatios.get(unitIdAndUnitName));
                if (unitQuestionRightRatio == null) {
                    unitQuestionRightRatio = 0D;
                }
                unitQuestionRightRatioList.add(Double.valueOf(dfFormat.format(unitQuestionRightRatio)));

                Double areaUnitQuestionRightRatio = SafeConverter.toDouble(areaUnitQuestionRightRatios.get(unitIdAndUnitName));
                if (areaUnitQuestionRightRatio == null) {
                    areaUnitQuestionRightRatio = 0D;
                }
                areaUnitQuestionRightRatioList.add(Double.valueOf(dfFormat.format(areaUnitQuestionRightRatio)));
            }

            List<Map<String, Object>> seriesData = new LinkedList<>();

            Map<String, Object> unitAvgQuestionsMap = new LinkedHashMap<>();
            unitAvgQuestionsMap.put("name", school.getShortName() + " 人均题数");
            unitAvgQuestionsMap.put("type", "bar");
            unitAvgQuestionsMap.put("data", unitAvgQuestionsList);

            Map<String, Object> areaUnitAvgQuestionsMap = new LinkedHashMap<>();
            areaUnitAvgQuestionsMap.put("name", countyName + " 人均题数");
            areaUnitAvgQuestionsMap.put("type", "bar");
            areaUnitAvgQuestionsMap.put("data", areaUnitAvgQuestionsList);

            Map<String, Object> unitQuestionRightRatioMap = new LinkedHashMap<>();
            unitQuestionRightRatioMap.put("name", school.getShortName() + " 正确率");
            unitQuestionRightRatioMap.put("type", "line");
            unitQuestionRightRatioMap.put("data", unitQuestionRightRatioList);

            Map<String, Object> areaUnitQuestionRightRatioMap = new LinkedHashMap<>();
            areaUnitQuestionRightRatioMap.put("name", countyName + " 正确率");
            areaUnitQuestionRightRatioMap.put("type", "line");
            areaUnitQuestionRightRatioMap.put("data", areaUnitQuestionRightRatioList);

            seriesData.add(unitAvgQuestionsMap);
            seriesData.add(areaUnitAvgQuestionsMap);
            seriesData.add(unitQuestionRightRatioMap);
            seriesData.add(areaUnitQuestionRightRatioMap);
            result.add("seriesData", seriesData);

            result.set("result", true);
        } catch (Exception e) {
            logger.error("教研员获取学情分析数据异常", e);
            result.add("result", false);
            result.add("info", "暂无数据");
        }
        return result;
    }


    @RequestMapping(value = "loadLearningSkills.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadLearningSkills(Model model) {
        MapMessage result = new MapMessage();
        try {
            ResearchStaff researchStaff = currentResearchStaff();
            if (Objects.isNull(researchStaff)) {
                return MapMessage.errorMessage("请重新登录");
            }
            String res = validatePasswdAndMobile(researchStaff.getId());
            if (StringUtils.isNotBlank(res)) {
                return MapMessage.errorMessage("请绑定手机或者重置秘密");
            }
            Long schoolId = getRequestLong("schoolId");
            School school = raikouSystem.loadSchool(schoolId);
            Integer areaId = school.getRegionCode();
            ExRegion exRegion = raikouSystem.loadRegion(areaId);
            String subject = researchStaff.getSubject().toString();
            String grade = getRequestString("grade");
            String schoolYearTerm = getRequestString("schoolYearTerm"); //17~18下学期
            String schoolYear = schoolYearTerm.split("-")[0];
            String term = schoolYearTerm.split("-")[1];
            String clazz = getRequestString("clazz");
            if (StringUtils.isEmpty(clazz)) {
                clazz = "0";
            }

            Map<String, Object> data = schoolMasterServiceClient.loadLearningSkills(areaId, schoolId, subject, grade, clazz, schoolYear, term);
            if (data == null) {
                result.add("result", false);
                result.add("info", "暂无数据");
                return result;
            }

            Map<String, Double> schoolSkillMap = (Map<String, Double>) data.get("schoolSkillRightRates");
            Map<String, Double> areaSkillMap = (Map<String, Double>) data.get("areaSkillRightRates");
            Map<String, Double> nationSkillMap = (Map<String, Double>) data.get("nationSkillRightRates");

            //组织柱状图数据,数学和英语
            List<String> skillNames = null;
            List<Double> schoolSeriesData = new LinkedList<>();
            List<Double> areaSeriesData = new LinkedList<>();
            List<Double> nationSeriesData = new LinkedList<>();
            if (Subject.MATH.name().equals(subject)) {
                skillNames = MathSkill.getAllMathSkillNames();
                getSkillSeriesData(schoolSkillMap, areaSkillMap, nationSkillMap, skillNames, schoolSeriesData, areaSeriesData, nationSeriesData);
            } else if (Subject.ENGLISH.name().equals(subject)) {
                skillNames = EnglishSkill.getAllEnglishSkill();
                getSkillSeriesData(schoolSkillMap, areaSkillMap, nationSkillMap, skillNames, schoolSeriesData, areaSeriesData, nationSeriesData);
            } else {
                skillNames = ChineseSkill.getAllChineseSkillNames();
                getSkillSeriesData(schoolSkillMap, areaSkillMap, nationSkillMap, skillNames, schoolSeriesData, areaSeriesData, nationSeriesData);
            }

            List<String> barLegendData = new LinkedList<>();
            barLegendData.add(school.getShortName());
            barLegendData.add(exRegion.getCountyName());
            barLegendData.add("全国");

            result.add("barSkillData", skillNames);
            result.add("barLegendData", barLegendData);
            List<Map<String, Object>> barSeriesData = new LinkedList<>();
            Map<String, Object> schoolSeriesDataMap = new LinkedHashMap<>();
            schoolSeriesDataMap.put("name", school.getShortName());
            schoolSeriesDataMap.put("data", schoolSeriesData);
            barSeriesData.add(schoolSeriesDataMap);

            Map<String, Object> areaSeriesDataMap = new LinkedHashMap<>();
            areaSeriesDataMap.put("name", exRegion.getCountyName());
            areaSeriesDataMap.put("data", areaSeriesData);
            barSeriesData.add(areaSeriesDataMap);

            Map<String, Object> nationSeriesDataMap = new LinkedHashMap<>();
            nationSeriesDataMap.put("name", "全国");
            nationSeriesDataMap.put("data", nationSeriesData);
            barSeriesData.add(nationSeriesDataMap);

            result.add("barSeriesData", barSeriesData);
            result.set("result", true);
        } catch (Exception e) {
            logger.error("教研员获取学科能力养成数据异常", e);
            result.add("result", false);
            result.add("info", "暂无数据");
        }
        return result;
    }

    /**
     * 学情分析--学科及知识板块
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "loadKnowledgeModule.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadKnowledgeModule(Model model) {
        MapMessage result = new MapMessage();
        try {
            ResearchStaff researchStaff = currentResearchStaff();
            if (Objects.isNull(researchStaff)) {
                return MapMessage.errorMessage("请重新登录");
            }
            String res = validatePasswdAndMobile(researchStaff.getId());
            if (StringUtils.isNotBlank(res)) {
                return MapMessage.errorMessage("请绑定手机或者重置秘密");
            }
            Long schoolId = getRequestLong("schoolId");
            School school = raikouSystem.loadSchool(schoolId);
            Integer areaId = school.getRegionCode();
            ExRegion exRegion = raikouSystem.loadRegion(areaId);
            String grade = getRequestString("grade");
            String subject = researchStaff.getSubject().toString();
            String schoolYearTerm = getRequestString("schoolYearTerm"); //17~18下学期
            String schoolYear = schoolYearTerm.split("-")[0];
            String term = schoolYearTerm.split("-")[1];
            String clazz = getRequestString("clazz");
            if (StringUtils.isEmpty(clazz)) {
                clazz = "0";
            }
            String knowledgeModuleLevel = getRequestString("knowledgeModuleLevel");
            Map<String, Object> data = schoolMasterServiceClient.loadKnowledgeModule(areaId, schoolId, subject, grade, clazz, schoolYear, term, knowledgeModuleLevel);
            if (data == null) {
                result.add("result", false);
                result.add("info", "暂无数据");
                return result;
            }

            Map<String, Double> schoolKnowledgeRightRates = (Map<String, Double>) data.get("schoolKnowledgeRightRates");
            Map<String, Double> areaKnowledgeRightRates = (Map<String, Double>) data.get("areaKnowledgeRightRates");
            Map<String, Double> nationKnowledgeRightRates = (Map<String, Double>) data.get("nationKnowledgeRightRates");

            //组织柱状图数据,数学和英语
            List<String> knowledgeModuleNames = null;
            List<Double> schoolSeriesData = new LinkedList<>();
            List<Double> areaSeriesData = new LinkedList<>();
            List<Double> nationSeriesData = new LinkedList<>();
            if (Subject.MATH.name().equals(subject)) {
                if (Objects.equals("1", knowledgeModuleLevel)) {
                    knowledgeModuleNames = MathKnowledgeModule.getAllMathKnowledgeModule();
                } else {
                    knowledgeModuleNames = MathSecondKnowledgeModule.getAllMathSecondKnowledgeModule();
                }
                getSkillSeriesData(schoolKnowledgeRightRates, areaKnowledgeRightRates, nationKnowledgeRightRates, knowledgeModuleNames, schoolSeriesData, areaSeriesData, nationSeriesData);
            } else if (Subject.ENGLISH.name().equals(subject)) {
                if (Objects.equals("1", knowledgeModuleLevel)) {
                    knowledgeModuleNames = EnglishKnowledgeModule.getAllEnglishKnowledgeModule();
                } else {
                    result.add("result", false);
                    result.add("info", "暂无数据");
                    return result;
                }
                getSkillSeriesData(schoolKnowledgeRightRates, areaKnowledgeRightRates, nationKnowledgeRightRates, knowledgeModuleNames, schoolSeriesData, areaSeriesData, nationSeriesData);
            } else {
                //语文暂无
                result.add("result", false);
                result.add("info", "暂无数据");
                return result;
            }
            List<String> barLegendData = new LinkedList<>();
            barLegendData.add(school.getShortName());
            barLegendData.add(exRegion.getCountyName());
            barLegendData.add("全国");

            result.add("barSkillData", knowledgeModuleNames);
            result.add("barLegendData", barLegendData);
            List<Map<String, Object>> barSeriesData = new LinkedList<>();
            Map<String, Object> schoolSeriesDataMap = new LinkedHashMap<>();
            schoolSeriesDataMap.put("name", school.getShortName());
            schoolSeriesDataMap.put("data", schoolSeriesData);
            barSeriesData.add(schoolSeriesDataMap);

            Map<String, Object> areaSeriesDataMap = new LinkedHashMap<>();
            areaSeriesDataMap.put("name", exRegion.getCountyName());
            areaSeriesDataMap.put("data", areaSeriesData);
            barSeriesData.add(areaSeriesDataMap);

            Map<String, Object> nationSeriesDataMap = new LinkedHashMap<>();
            nationSeriesDataMap.put("name", "全国");
            nationSeriesDataMap.put("data", nationSeriesData);
            barSeriesData.add(nationSeriesDataMap);

            result.add("barSeriesData", barSeriesData);
            result.set("result", true);
        } catch (Exception e) {
            logger.error("校长获取知识板块数据异常", e);
            result.add("result", false);
            result.add("info", "暂无数据");
        }
        return result;
    }

}
