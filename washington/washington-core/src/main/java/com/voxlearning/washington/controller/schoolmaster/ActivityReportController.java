package com.voxlearning.washington.controller.schoolmaster;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.enums.ActivityTypeEnum;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.rstaff.consumer.ActivityReportServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/schoolmaster/activityReport")
public class ActivityReportController extends SchoolMasterBaseController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private ActivityConfigServiceClient activityConfigServiceClient;

    @Inject
    private ActivityReportServiceClient activityReportServiceClient;

    /**
     * 活动报告列表页面
     *
     * @return
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String activityList(Model model) {
        ResearchStaff researchStaff = currentResearchStaff();
        if (researchStaff == null) {
            return "redirect:" + getBaseDomain();
        }
        String result = validatePasswdAndMobile(researchStaff.getId());
        if (StringUtils.isNotBlank(result)) {
            return result;
        }
        if (researchStaff.isPresident()) {
            model.addAttribute("idType", "schoolmaster");
        } else {
            model.addAttribute("idType", "rstaff");
        }
        model.addAttribute("pageType", "activityreport");
        return "adminteacher/activityreport/list";
    }

    /**
     * 活动状况
     */
    @RequestMapping(value = "getActivityReportCondition.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map getReportCondition() {
        MapMessage result = new MapMessage();
        List<Map<String, String>> activityTypes = new LinkedList<>();
        Map<String, String> activityType0 = new LinkedHashMap<>();
        activityType0.put("activityType", "");
        activityType0.put("name", "全部");
        activityTypes.add(activityType0);
        for (ActivityTypeEnum activityTypeEnum : ActivityTypeEnum.values()) {
            Map<String, String> type = new LinkedHashMap<>();
            type.put("activityType", activityTypeEnum.name());
            type.put("name", activityTypeEnum.getName());
            activityTypes.add(type);
        }
        result.add("activityTypes", activityTypes);
        Date startDate = DateUtils.stringToDate("201809", "yyyyMM");
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        List<Map<String, String>> dateList = new LinkedList<>();
        String currentDate = DateUtils.dateToString(new Date(), "yyyyMM");
        while (true) {
            String value = DateUtils.dateToString(cal.getTime(), "yyyyMM");
            String text = DateUtils.dateToString(cal.getTime(), "yyyy年MM月");
            Map<String, String> dateMap = new LinkedHashMap<>();
            dateMap.put("name", text);
            dateMap.put("value", value);
            if (value.compareTo("201809") >= 0) {
                dateList.add(dateMap);
            }
            if (currentDate.compareTo(value) == 0) {
                break;
            }
            cal.add(Calendar.MONTH, 1);
        }
        result.add("dateList", dateList);
        result.add("result", true);
        return result;
    }


    /**
     * 活动状况
     */
    @RequestMapping(value = "getActivityReportList.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map getReportList() {
        MapMessage result = new MapMessage();
        ResearchStaff researchStaff = currentResearchStaff();
        if (Objects.isNull(researchStaff)) {
            return MapMessage.errorMessage("请重新登录");
        }
        String res = validatePasswdAndMobile(researchStaff.getId());
        if (StringUtils.isNotBlank(res)) {
            return MapMessage.errorMessage("请绑定手机或者重置秘密");
        }
        try {
            List<Map<String, Object>> activityReportList = new LinkedList<>();
            String activityType = getRequestString("activityType");
            String dateStr = getRequestString("date");
            Date startDate = DateUtils.getFirstDayOfMonth(DateUtils.stringToDate(dateStr, "yyyyMM"));
            List<ActivityConfig> activityConfigs = activityConfigServiceClient.getActivityConfigService().loadActivityConfigListByTypeAndDate(activityType, startDate);
            for (ActivityConfig activityConfig : activityConfigs) {
                if (!activityConfig.isEnd(new Date())) {
                    continue;
                }
                Map<String, Object> activityReport = new LinkedHashMap<>();
                activityReport.put("id", activityConfig.getId());
                activityReport.put("name", activityConfig.getTitle());
                if (CollectionUtils.isNotEmpty(activityConfig.getAreaIds())) {
                    Set<Integer> cityCodes = new HashSet<>();
                    Set<Integer> regionCodes = new HashSet<>();
                    for (Long areaId : activityConfig.getAreaIds()) {
                        regionCodes.add(SafeConverter.toInt(areaId));
                        ExRegion region = raikouSystem.loadRegion(SafeConverter.toInt(areaId));
                        cityCodes.add(region.getCityCode());
                    }
                    activityReport.put("regionCodes", regionCodes);
                    activityReport.put("cityCodes", cityCodes);
                } else if (CollectionUtils.isNotEmpty(activityConfig.getSchoolIds())) {
                    activityReport.put("schoolIds", activityConfig.getSchoolIds());
                    Map<Long, School> schools = raikouSystem.loadSchools(activityConfig.getSchoolIds());
                    Set<Integer> regionCodes = schools.values().stream().map(School::getRegionCode).collect(Collectors.toSet());
                    activityReport.put("regionCodes", regionCodes);
                    Map<Integer, ExRegion> regions = raikouSystem.getRegionBuffer().loadRegions(regionCodes);
                    Set<Integer> cityCodes = regions.values().stream().map(ExRegion::getCityCode).collect(Collectors.toSet());
                    activityReport.put("cityCodes", cityCodes);
                }
                activityReport.put("activityDate", DateUtils.dateToString(activityConfig.getStartTime(), "yyyy.MM.dd") + "~" + DateUtils.dateToString(activityConfig.getEndTime(), "yyyy.MM.dd"));
                activityReport.put("startTime", activityConfig.getStartTime());
                activityReport.put("limitTime", activityConfig.getRules().getLimitTime() + "分钟");
                activityReport.put("limitAmount", activityConfig.getRules().getLimitAmount());
                activityReportList.add(activityReport);
            }

            List<Map<String, Object>> activityReportList11 = new LinkedList<>();
            if (researchStaff.isPresident()) {//校长
                Long schoolId = researchStaff.getManagedRegion().getSchoolIds().iterator().next();
                for (int i = 0; i < activityReportList.size(); i++) {
                    Map<String, Object> temp = activityReportList.get(i);
                    Object schoolIds = temp.get("schoolIds");
                    if (schoolIds != null) {
                        List<Long> a = (List<Long>) schoolIds;
                        if (a.contains(schoolId)) {
                            temp.put("regionLevel", "school");
                            temp.put("regionCode", schoolId);
                            activityReportList11.add(temp);
                        }
                    }
                }
            } else {
                List<Integer> cityCodes = getCityCodesParam(researchStaff);//市code
                List<Integer> regionCodes = getRegionCodesParam(researchStaff);//区code
                Set<Long> schoolIds = researchStaff.getManagedRegion().getSchoolIds();//配置的多学校
                if (CollectionUtils.isNotEmpty(cityCodes)) {
                    for (Integer cityCode : cityCodes) {
                        for (int i = 0; i < activityReportList.size(); i++) {
                            Map<String, Object> temp = activityReportList.get(i);
                            Object cityCodess = temp.get("cityCodes");
                            if (cityCodess != null) {
                                Set<Integer> a = (Set<Integer>) cityCodess;
                                if (a.contains(cityCode)) {
                                    temp.put("regionLevel", "city");
                                    temp.put("regionCode", cityCode);
                                    Map<String, Object> newTemp = new LinkedHashMap<>();
                                    newTemp.putAll(temp);
                                    activityReportList11.add(newTemp);
                                }
                            }
                        }
                    }
                } else if (CollectionUtils.isNotEmpty(regionCodes)) {
                    for (Integer regionCode : regionCodes) {
                        for (int i = 0; i < activityReportList.size(); i++) {
                            Map<String, Object> temp = activityReportList.get(i);
                            Object regionCodess = temp.get("regionCodes");
                            if (regionCodess != null) {
                                Set<Integer> a = (Set<Integer>) regionCodess;
                                if (a.contains(regionCode)) {
                                    temp.put("regionLevel", "county");
                                    temp.put("regionCode", regionCode);
                                    Map<String, Object> newTemp = new LinkedHashMap<>();
                                    newTemp.putAll(temp);
                                    activityReportList11.add(newTemp);
                                }
                            }
                        }
                    }
                } else if (CollectionUtils.isNotEmpty(schoolIds)) {
                    Iterator<Long> schoolIdIts = schoolIds.iterator();
                    while (schoolIdIts.hasNext()) {
                        Long schoolIdd = schoolIdIts.next();
                        for (int i = 0; i < activityReportList.size(); i++) {
                            Map<String, Object> temp = activityReportList.get(i);
                            Object schoolIdss = temp.get("schoolIds");
                            if (schoolIdss != null) {
                                List<Long> a = (List<Long>) schoolIdss;
                                if (a.contains(schoolIdd)) {
                                    temp.put("regionLevel", "school");
                                    temp.put("regionCode", schoolIdd);
                                    Map<String, Object> newTemp = new LinkedHashMap<>();
                                    newTemp.putAll(temp);
                                    activityReportList11.add(newTemp);
                                }
                            }
                        }
                    }
                }
            }
            result.add("result", true);
            //按开始时间倒序排列
            Collections.sort(activityReportList11, (o1, o2) -> {
                Date startTime1 = (Date) o1.get("startTime");
                Date startTime2 = (Date) o2.get("startTime");
                return startTime2.compareTo(startTime1);
            });
            //
            Map<String, List<Map<String, Object>>> activitReportMap = new LinkedHashMap<>();
            for (Map<String, Object> temp : activityReportList11) {
                String activityId = (String) temp.get("id");
                List<Map<String, Object>> tempList = (List<Map<String, Object>>) activitReportMap.get(activityId);
                if (tempList == null) {
                    tempList = new LinkedList<>();
                }
                tempList.add(temp);
                activitReportMap.put(activityId, tempList);
            }
            //有多个
            //增加扩展名
            Iterator<String> idIt = activitReportMap.keySet().iterator();
            while (idIt.hasNext()) {
                String id = idIt.next();
                List<Map<String, Object>> tempList = activitReportMap.get(id);
                if (tempList.size() >= 2) {
                    for (Map<String, Object> temp : tempList) {
                        String regionLevel = (String) temp.get("regionLevel");
                        String name = (String) temp.get("name");
                        if ("school".equals(regionLevel)) {
                            Long regionCode = (Long) temp.get("regionCode");
                            School school = raikouSystem.loadSchool(regionCode);
                            name = name + "(" + school.getShortName() + ")";
                        } else {
                            Integer regionCode = (Integer) temp.get("regionCode");
                            ExRegion region = raikouSystem.loadRegion(regionCode);
                            name = name + "(" + region.getName() + ")";
                        }
                        temp.put("name", name);
                    }
                }
            }

            if (RuntimeMode.current().le(Mode.TEST)) {
                Map<String, Object> temp = new LinkedHashMap<>();
                temp.put("id", "5bac9f10ac7459733718eb36");
                temp.put("name", "测试报告");
                temp.put("regionLevel", "city");
                temp.put("regionCode", 110100);
                temp.put("activityDate", DateUtils.dateToString(new Date(), "yyyy.MM.dd") + "~" + DateUtils.dateToString(new Date(), "yyyy.MM.dd"));
                temp.put("startTime", new Date(0));
                temp.put("limitTime", "5分钟");
                temp.put("limitAmount", 5);
                activityReportList11.add(temp);
                result.add("activityReportList", activityReportList11);
            } else {
                result.add("activityReportList", activityReportList11);
            }
        } catch (Exception e) {
            logger.error("获得活动报告列表数据异常", e);
            result.add("result", false);
            result.add("info", "暂无数据");
        }
        return result;
    }


    /**
     * 活动报告页面
     *
     * @return
     */
    @RequestMapping(value = "report.vpage", method = RequestMethod.GET)
    public String activityReport(Model model) {
        return "adminteacher/activityreport/report";
    }


    /**
     * 活动状况
     */
    @RequestMapping(value = "loadActivityReportSurvey.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadActivityReportSurvey() {
        MapMessage result = new MapMessage();
        ResearchStaff researchStaff = currentResearchStaff();
        if (Objects.isNull(researchStaff)) {
            return MapMessage.errorMessage("请重新登录");
        }
        String res = validatePasswdAndMobile(researchStaff.getId());
        if (StringUtils.isNotBlank(res)) {
            return MapMessage.errorMessage("请绑定手机或者重置秘密");
        }
        try {
            if (RuntimeMode.current().le(Mode.TEST)) {
                String jsonStr = "{\"regionLevel\":\"school\",\"activityType\":\"七巧板\",\"clazz\":[\"一年级\",\"二年级\",\"三年级\",\"四年级\",\"五年级\",\"六年级\"],\"activityDate\":\"2018.10.16~2018.10.20\",\"limitTime\":\"12分钟\",\"regions\":null,\"schools\":null,\"clazzs\":4,\"students\":10,\"participationNums\":1.20,\"grid\":{\"gridHead\":[\"序号\",\"年级\",\"班级\",\"实际参与学生数\",\"人均参与次数\"],\"gridData\":[[1,\"二年级\",\"2班\",1,1.00],[2,\"三年级\",\"1班\",5,1.40],[3,\"四年级\",\"2班\",3,1.00],[4,\"五年级\",\"16班\",1,1.00]]},\"barMaps\":[{\"title\":\"二年级各班级-人均参与次数\",\"viewRegion\":\"班级\",\"legendData\":[\"整体\",\"各班级\"],\"xAxisData\":[{\"no\":1,\"name\":\"2班\"}],\"seriesData\":[1.00],\"clazzLevel\":\"二年级\",\"regions\":1,\"unit\":\"次/人\",\"wholeAvgNums\":1.00,\"topThree\":[],\"lastOne\":{}},{\"title\":\"三年级各班级-人均参与次数\",\"viewRegion\":\"班级\",\"legendData\":[\"整体\",\"各班级\"],\"xAxisData\":[{\"no\":2,\"name\":\"1班\"}],\"seriesData\":[1.40],\"clazzLevel\":\"三年级\",\"regions\":1,\"unit\":\"次/人\",\"wholeAvgNums\":1.40,\"topThree\":[],\"lastOne\":{}},{\"title\":\"四年级各班级-人均参与次数\",\"viewRegion\":\"班级\",\"legendData\":[\"整体\",\"各班级\"],\"xAxisData\":[{\"no\":3,\"name\":\"2班\"}],\"seriesData\":[1.00],\"clazzLevel\":\"四年级\",\"regions\":1,\"unit\":\"次/人\",\"wholeAvgNums\":1.00,\"topThree\":[],\"lastOne\":{}},{\"title\":\"五年级各班级-人均参与次数\",\"viewRegion\":\"班级\",\"legendData\":[\"整体\",\"各班级\"],\"xAxisData\":[{\"no\":4,\"name\":\"16班\"}],\"seriesData\":[1.00],\"clazzLevel\":\"五年级\",\"regions\":1,\"unit\":\"次/人\",\"wholeAvgNums\":1.00,\"topThree\":[],\"lastOne\":{}},{\"xAxisData\":[\"二年级\",\"三年级\",\"四年级\",\"五年级\"],\"seriesData\":[1.00,1.40,1.00,1.00],\"legendData\":[\"各年级\",\"整体\"],\"unit\":\"次/人\",\"grades\":4,\"topThree\":[],\"lastOne\":{}}],\"result\":true}";
                Map resultData = JsonUtils.fromJson(jsonStr);
                Iterator it = resultData.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    result.add(key, resultData.get(key));
                }
                return result;
            }

            String id = getRequestString("id");
            String regionLevel = getRequestString("regionLevel");
            String regionCode = getRequestString("regionCode");

            ActivityConfig activityConfig = activityConfigServiceClient.getActivityConfigService().load(id);
            result.add("regionLevel", regionLevel);
            //根据活动配置表的区域来判断是区级报告，校级报告....
            //概况数据
            result.add("activityType", activityConfig.getType().getName()); //活动类型
            List<String> clazz = new LinkedList<>();
            if (CollectionUtils.isNotEmpty(activityConfig.getClazzLevels())) {
                for (Integer clazzLevel : activityConfig.getClazzLevels()) {
                    clazz.add(ClazzLevel.getDescription(clazzLevel));
                }
            }
            result.add("clazz", clazz);
            result.add("activityDate", DateUtils.dateToString(activityConfig.getStartTime(), "yyyy.MM.dd") + "~" + DateUtils.dateToString(activityConfig.getEndTime(), "yyyy.MM.dd"));
            result.add("limitTime", activityConfig.getRules().getLimitTime() + "分钟");
            Map<String, Object> dataMap = activityReportServiceClient.loadActivityReportSurvey(regionLevel, regionCode, id);

            result.add("regions", dataMap.get("regions"));
            result.add("schools", dataMap.get("schools"));
            result.add("clazzs", dataMap.get("clazzs"));
            result.add("students", dataMap.get("students"));
            result.add("participationNums", dataMap.get("participationNums"));
            //表格数据
            result.add("grid", dataMap.get("grid"));

            result.add("barMaps", dataMap.get("viewVarList"));
            result.add("result", true);
        } catch (Exception e) {
            logger.error("获得活动报告概况数据异常", e);
            result.add("result", false);
            result.add("info", "暂无数据");
        }
        return result;
    }


    /**
     * 活动得分状况
     */
    @RequestMapping(value = "loadActivityScoreState.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadActivityScoreState() {
        MapMessage result = new MapMessage();
        ResearchStaff researchStaff = currentResearchStaff();
        if (Objects.isNull(researchStaff)) {
            return MapMessage.errorMessage("请重新登录");
        }
        String res = validatePasswdAndMobile(researchStaff.getId());
        if (StringUtils.isNotBlank(res)) {
            return MapMessage.errorMessage("请绑定手机或者重置秘密");
        }
        try {
            if (RuntimeMode.current().le(Mode.TEST)) {
                String jsonStr = "{\"wholeScoreMap\":{\"highAvgScore\":15.00,\"fullMarks\":\"不限总分\",\"grades\":4,\"wholeViewRegion\":\"班级\",\"regions\":4},\"grid\":{\"gridHead\":[\"序号\",\"年级\",\"班级\",\"最高分平均分\"],\"gridData\":[[1,\"二年级\",\"2班\",15.00],[2,\"三年级\",\"1班\",14.60],[3,\"四年级\",\"2班\",13.00],[4,\"五年级\",\"16班\",23.00]]},\"barMaps\":[{\"title\":\"二年级各班级-最高分平均分\",\"viewRegion\":\"班级\",\"legendData\":[\"整体\",\"各班级\"],\"xAxisData\":[{\"no\":1,\"name\":\"2班\"}],\"seriesData\":[15.00],\"clazzLevel\":\"二年级\",\"regions\":1,\"unit\":\"分\",\"wholeAvgNums\":15.00,\"topThree\":[],\"lastOne\":{}},{\"title\":\"三年级各班级-最高分平均分\",\"viewRegion\":\"班级\",\"legendData\":[\"整体\",\"各班级\"],\"xAxisData\":[{\"no\":2,\"name\":\"1班\"}],\"seriesData\":[14.60],\"clazzLevel\":\"三年级\",\"regions\":1,\"unit\":\"分\",\"wholeAvgNums\":14.60,\"topThree\":[],\"lastOne\":{}},{\"title\":\"四年级各班级-最高分平均分\",\"viewRegion\":\"班级\",\"legendData\":[\"整体\",\"各班级\"],\"xAxisData\":[{\"no\":3,\"name\":\"2班\"}],\"seriesData\":[13.00],\"clazzLevel\":\"四年级\",\"regions\":1,\"unit\":\"分\",\"wholeAvgNums\":13.00,\"topThree\":[],\"lastOne\":{}},{\"title\":\"五年级各班级-最高分平均分\",\"viewRegion\":\"班级\",\"legendData\":[\"整体\",\"各班级\"],\"xAxisData\":[{\"no\":4,\"name\":\"16班\"}],\"seriesData\":[23.00],\"clazzLevel\":\"五年级\",\"regions\":1,\"unit\":\"分\",\"wholeAvgNums\":23.00,\"topThree\":[],\"lastOne\":{}}],\"result\":true}";
                Map resultData = JsonUtils.fromJson(jsonStr);
                Iterator it = resultData.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    result.add(key, resultData.get(key));
                }
                return result;
            }
            String id = getRequestString("id");
            String regionLevel = getRequestString("regionLevel");
            String regionCode = getRequestString("regionCode");

            Map<String, Object> dataMap = activityReportServiceClient.loadActivityScoreState(regionLevel, regionCode, id);

            result.add("wholeScoreMap", dataMap.get("wholeScoreMap"));
            result.add("grid", dataMap.get("gridMap"));
            result.add("barMaps", dataMap.get("viewBarList"));
            result.add("result", true);
        } catch (Exception e) {
            logger.error("获得活动得分状况数据异常", e);
            result.add("result", false);
            result.add("info", "暂无数据");
        }
        return result;
    }

    /**
     * 成绩分布
     */
    @RequestMapping(value = "loadActivityScoreLevel.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadActivityScoreLevel() {
        MapMessage result = new MapMessage();
        ResearchStaff researchStaff = currentResearchStaff();
        if (Objects.isNull(researchStaff)) {
            return MapMessage.errorMessage("请重新登录");
        }
        String res = validatePasswdAndMobile(researchStaff.getId());
        if (StringUtils.isNotBlank(res)) {
            return MapMessage.errorMessage("请绑定手机或者重置秘密");
        }
        try {
            if (RuntimeMode.current().le(Mode.TEST)) {
                String jsonStr = "{\"wholeGrid\":{\"gridHead\":[\"分数\",\"人数\",\"比例\"],\"gridData\":[[\"0-9分\",2,\"20.00%\"],[\"10-19分\",6,\"60.00%\"],[\"20-29分\",2,\"20.00%\"],[\"30-39分\",0,\"0.00%\"],[\"40-49分\",0,\"0.00%\"],[\"50分以上\",0,\"0.00%\"]],\"topOne\":\"10-19分\",\"lastOne\":\"50分以上\"},\"pieMap\":{\"pieLegendData\":[\"0-9分\",\"10-19分\",\"20-29分\",\"30-39分\",\"40-49分\",\"50分以上\"],\"pieSeriesData\":[{\"name\":\"10-19分\",\"value\":\"60.00\"},{\"name\":\"0-9分\",\"value\":\"20.00\"},{\"name\":\"20-29分\",\"value\":\"20.00\"},{\"name\":\"30-39分\",\"value\":\"0.00\"},{\"name\":\"40-49分\",\"value\":\"0.00\"},{\"name\":\"50分以上\",\"value\":\"0.00\"}]},\"grid\":{\"gridHead\":[\"序号\",\"年级\",\"班级\",\"0-9分\",\"10-19分\",\"20-29分\",\"30-39分\",\"40-49分\",\"50分以上\"],\"gridData\":[[1,\"二年级\",\"2班\",0,1,0,0,0,0],[2,\"三年级\",\"1班\",1,3,1,0,0,0],[3,\"四年级\",\"2班\",1,2,0,0,0,0],[4,\"五年级\",\"16班\",0,0,1,0,0,0]]},\"barMaps\":[{\"title\":\"二年级各班级-高分人数占比图\",\"viewRegion\":\"班级\",\"legendData\":[\"整体\",\"各班级\"],\"xAxisData\":[{\"no\":1,\"name\":\"2班\"}],\"seriesData\":[0.00],\"clazzLevel\":\"二年级\",\"regions\":1,\"unit\":\"%\",\"wholeAvgNums\":0.00,\"topScoreDefineMsg\":\"20分以上为高分\",\"topThree\":[],\"lastOne\":{}},{\"title\":\"三年级各班级-高分人数占比图\",\"viewRegion\":\"班级\",\"legendData\":[\"整体\",\"各班级\"],\"xAxisData\":[{\"no\":2,\"name\":\"1班\"}],\"seriesData\":[20.00],\"clazzLevel\":\"三年级\",\"regions\":1,\"unit\":\"%\",\"wholeAvgNums\":20.00,\"topScoreDefineMsg\":\"20分以上为高分\",\"topThree\":[],\"lastOne\":{}},{\"title\":\"四年级各班级-高分人数占比图\",\"viewRegion\":\"班级\",\"legendData\":[\"整体\",\"各班级\"],\"xAxisData\":[{\"no\":3,\"name\":\"2班\"}],\"seriesData\":[0.00],\"clazzLevel\":\"四年级\",\"regions\":1,\"unit\":\"%\",\"wholeAvgNums\":0.00,\"topScoreDefineMsg\":\"20分以上为高分\",\"topThree\":[],\"lastOne\":{}},{\"title\":\"五年级各班级-高分人数占比图\",\"viewRegion\":\"班级\",\"legendData\":[\"整体\",\"各班级\"],\"xAxisData\":[{\"no\":4,\"name\":\"16班\"}],\"seriesData\":[100.00],\"clazzLevel\":\"五年级\",\"regions\":1,\"unit\":\"%\",\"wholeAvgNums\":100.00,\"topScoreDefineMsg\":\"20分以上为高分\",\"topThree\":[],\"lastOne\":{}}],\"result\":true}";
                Map resultData = JsonUtils.fromJson(jsonStr);
                Iterator it = resultData.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    result.add(key, resultData.get(key));
                }
                return result;
            }

            String id = getRequestString("id");
            String regionLevel = getRequestString("regionLevel");
            String regionCode = getRequestString("regionCode");
            Map<String, Object> dataMap = activityReportServiceClient.loadActivityScoreLevel(regionLevel, regionCode, id);
            //总体表格数据
            result.add("wholeGrid", dataMap.get("wholeGrid"));
            result.add("pieMap", dataMap.get("pieMap"));

            //第二个表格数据
            result.add("grid", dataMap.get("grid"));
            result.add("barMaps", dataMap.get("viewBarList"));
            result.add("result", true);
        } catch (Exception e) {
            logger.error("获得活动成绩分布数据异常", e);
            result.add("result", false);
            result.add("info", "暂无数据");
        }
        return result;
    }

    /**
     * 答题速度
     */
    @RequestMapping(value = "loadActivityAnswerSpeed.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadActivityAnswerSpeed() {
        MapMessage result = new MapMessage();
        ResearchStaff researchStaff = currentResearchStaff();
        if (Objects.isNull(researchStaff)) {
            return MapMessage.errorMessage("请重新登录");
        }
        String res = validatePasswdAndMobile(researchStaff.getId());
        if (StringUtils.isNotBlank(res)) {
            return MapMessage.errorMessage("请绑定手机或者重置秘密");
        }
        try {
            if (RuntimeMode.current().le(Mode.TEST)) {
                String jsonStr = "{\"wholeGrid\":{\"gridHead\":[\"年级\",\"每分钟答对题目数量\"],\"gridData\":[[\"六年级\",0.26],[\"五年级\",0.25],[\"三年级\",0],[\"整体\",0.25]],\"topOneData\":{\"clazzLevel\":\"六年级\",\"clazzLevelSpeed\":0.26,\"topOneName\":\"六年级\",\"topOneVal\":\"0.26题/分钟\",\"topDiff\":\"0.01题/分钟\"},\"lastOneData\":{\"clazzLevel\":\"三年级\",\"clazzLevelSpeed\":0,\"lastOneName\":\"三年级\",\"lastOneVal\":\"0题/分钟\",\"lastDiff\":\"0.25题/分钟\"}},\"wholeBarMap\":{\"title\":\"答题速度\",\"xAxisData\":[\"六年级\",\"五年级\",\"三年级\"],\"legendData\":[\"年级\",\"整体\"],\"seriesData\":[0.26,0.25,0],\"wholeAnswerSpeed\":0.25},\"grid2\":{\"gridHead\":[\"序号\",\"年级\",\"班级\",\"答题速度\"],\"gridData\":[[1,\"五年级\",\"1班\",\"0.32题/分钟\"],[2,\"五年级\",\"2班\",\"0.24题/分钟\"],[3,\"五年级\",\"3班\",\"0.25题/分钟\"],[4,\"五年级\",\"4班\",\"0.28题/分钟\"],[5,\"五年级\",\"5班\",\"0.23题/分钟\"],[6,\"五年级\",\"6班\",\"0.23题/分钟\"],[7,\"六年级\",\"1班\",\"0.26题/分钟\"],[8,\"六年级\",\"2班\",\"0.21题/分钟\"],[9,\"六年级\",\"3班\",\"0.25题/分钟\"],[10,\"六年级\",\"4班\",\"0.27题/分钟\"]]},\"barMaps\":[{\"title\":\"五年级各班级-答题速度\",\"viewRegion\":\"班级\",\"legendData\":[\"整体\",\"各班级\"],\"xAxisData\":[{\"no\":1,\"name\":\"1班\"},{\"no\":2,\"name\":\"2班\"},{\"no\":3,\"name\":\"3班\"},{\"no\":4,\"name\":\"4班\"},{\"no\":5,\"name\":\"5班\"},{\"no\":6,\"name\":\"6班\"}],\"seriesData\":[0.32,0.24,0.25,0.28,0.23,0.23],\"clazzLevel\":\"五年级\",\"unit\":\"题/分钟\",\"wholeAvgNums\":0.25,\"topThree\":[\"1班(0.32题/分钟)\",\"4班(0.28题/分钟)\",\"3班(0.25题/分钟)\"],\"lastOne\":{\"lastName\":\"6班\",\"lastValue\":\"(0.23题/分钟)\"},\"diff\":0.02},{\"title\":\"六年级各班级-答题速度\",\"viewRegion\":\"班级\",\"legendData\":[\"整体\",\"各班级\"],\"xAxisData\":[{\"no\":7,\"name\":\"1班\"},{\"no\":8,\"name\":\"2班\"},{\"no\":9,\"name\":\"3班\"},{\"no\":10,\"name\":\"4班\"}],\"seriesData\":[0.26,0.21,0.25,0.27],\"clazzLevel\":\"六年级\",\"unit\":\"题/分钟\",\"wholeAvgNums\":0.26,\"topThree\":[\"4班(0.27题/分钟)\",\"1班(0.26题/分钟)\",\"3班(0.25题/分钟)\"],\"lastOne\":{\"lastName\":\"2班\",\"lastValue\":\"(0.21题/分钟)\"},\"diff\":0.05}],\"result\":true}";
                Map resultData = JsonUtils.fromJson(jsonStr);
                Iterator it = resultData.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    result.add(key, resultData.get(key));
                }
                return result;
            }

            String id = getRequestString("id");
            String regionLevel = getRequestString("regionLevel");
            String regionCode = getRequestString("regionCode");
            ActivityConfig activityConfig = activityConfigServiceClient.getActivityConfigService().load(id);
            if (activityConfig.getType() != ActivityTypeEnum.SUDOKU) {
                result.add("result", false);
                result.add("info", "暂无数据");
                return result;
            }

            Map<String, Object> dataMap = activityReportServiceClient.loadActivityAnswerSpeed(regionLevel, regionCode, id);
            //整体表格
            result.add("wholeGrid", dataMap.get("wholeGrid"));
            //整体柱图数据
            result.add("wholeBarMap", dataMap.get("wholeBarMap"));
            //第二个表格的数据
            result.add("grid2", dataMap.get("grid"));
            //柱图
            result.add("barMaps", dataMap.get("viewBarList"));

            result.add("result", true);
        } catch (Exception e) {
            logger.error("获得活动答题速度数据异常", e);
            result.add("result", false);
            result.add("info", "暂无数据");
        }
        return result;
    }


}
