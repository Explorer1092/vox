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

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.business.api.entity.BizStudentVoice;
import com.voxlearning.utopia.business.api.mapper.ResearchInfo;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.mapper.rstaff.*;
import com.voxlearning.utopia.service.business.api.entity.BizMarketingSchoolData;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegralHistoryPagination;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff.ResearchStaffType;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.POIExcel;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: xinqiang.wang
 * Date: 13-8-18
 * Time: 下午1:29
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/rstaff/report")
public class ResearchStaffReportController extends AbstractController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private SchoolLoaderClient schoolLoaderClient;


    /**
     * NEW教研员
     * 大数据报告 -- 知识数据
     */
    @RequestMapping(value = "knowledgedata.vpage", method = RequestMethod.GET)
    public String knowledgedata(Model model) {
        SchoolYear schoolYear = SchoolYear.newInstance();
        DateRange dateRange = schoolYear.currentTermDateRange();
        Term term = schoolYear.currentTerm();
        String termText;
        int endYear = DayRange.newInstance(dateRange.getEndTime()).getYear();
        if (term == Term.下学期) {
            termText = (endYear - 1) + "-" + endYear + "学年第二学期";
        } else {
            termText = (endYear - 1) + "-" + endYear + "学年第一学期";
        }
        model.addAttribute("termText", termText);
        //数据更新日期(从大数据端取数据频度是每周一次,数据更新日期取上周的周末)
        model.addAttribute("updateDate", com.voxlearning.alps.calendar.DateUtils.dateToString(WeekRange.current().previous().getEndDate(), com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE));

        //历史学期
        List<String> historyYearStr = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, 9, 1);   // 起始可查询年份
        while (calendar.getTime().before(dateRange.getEndDate())) {
            int year = calendar.get(Calendar.YEAR);
            String str = year + "-" + (year + 1);
            historyYearStr.add(str);

            calendar.add(Calendar.YEAR, 1);
        }
        model.addAttribute("historyYears", historyYearStr);

        //根据输入确定所在学期
        String year = getRequest().getParameter("year");
        if (year != null) {
            model.addAttribute("year", year);
            model.addAttribute("term", getRequest().getParameter("term"));
        } else {
            int startYear = schoolYear.year();
            model.addAttribute("year", String.valueOf(startYear) + "-" + String.valueOf(startYear + 1));
            model.addAttribute("term", String.valueOf(schoolYear.currentTerm().getKey()));
        }

        return "rstaffv3/report/knowledgedata";
    }


    /**
     * NEW 教研员
     * 大数据报告 -->积分统计（即原来用户汇总）
     */
    @RequestMapping(value = "integralstat/summary.vpage", method = RequestMethod.GET)
    public String summary(Model model) {
        ResearchStaff researchStaff = currentResearchStaff();
        Integer code = researchStaff.getRegionCode();
        String status = researchStaffServiceClient.getRemoteReference().validMarketJobTaskRunSuccessOrFaild();
        long time;
        if ("SUCCESS".equals(status)) {
            time = System.currentTimeMillis() - (3600 * 24 * 1000);
        } else {
            time = System.currentTimeMillis() - (3600 * 24 * 1000) * 2;
        }
        String date = com.voxlearning.alps.calendar.DateUtils.dateToString(new Date(time), com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE) + " 23:59:59";
        ResearchInfo resinfo;
        if (researchStaff.getResearchStaffType() != ResearchStaffType.STREET) {
            resinfo = researchStaffServiceClient.getRemoteReference().getSchoolStatisticData(researchStaff.getSubject(), date, null, code, researchStaff.getRegionType().getType(), null);
        } else {
            resinfo = researchStaffServiceClient.getRemoteReference().getSchoolStatisticDataBySchoolIds(researchStaff.getSubject(), date, null,
                    code, researchStaff.getRegionType().getType(), researchStaff.getManagedRegion().getSchoolIds());
        }
        logger.debug("UserInfo: {}", resinfo);
        // 初始化下拉框数据
        ResearchStaffType researchStaffType = researchStaff.getResearchStaffType();
        if (researchStaffType == ResearchStaffType.CITY) {
            Map<String, String> regionNameMap = getRegionNameMap(researchStaff.getManagedRegion().getCityCodes());
            model.addAttribute("nameMap", regionNameMap);
        } else if (researchStaffType == ResearchStaffType.COUNTY) {
            Map<String, String> regionNameMap = getRegionNameMap(researchStaff.getManagedRegion().getAreaCodes());
            model.addAttribute("nameMap", regionNameMap);
        } else if (researchStaffType == ResearchStaffType.STREET) {
            Map<String, String> schoolNameMap = getSchoolNameMap(researchStaff.getManagedRegion().getSchoolIds());
            model.addAttribute("nameMap", schoolNameMap);
        }
        model.addAttribute("role", researchStaffType.toString());
        model.addAttribute("authenticateNum", resinfo);
        model.addAttribute("code", code);
        model.addAttribute("dataTime", date);
        model.addAttribute("region", researchStaff.getRegion() == null ? "" : researchStaff.getRegion().getName());

        return "rstaffv3/report/integralstat/summary";
    }


    /**
     * 金币 -- 用户汇总 -- 分时查询 -- 查询按钮
     */
    @RequestMapping(value = "integralstat/detailchip.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public Map indexShowResult(@RequestParam("areaCode") int areaCode,
                               @RequestParam("schoolId") Long schoolId,
                               @RequestParam("beginTime") String beginTime,
                               @RequestParam("endTime") String endTime) {
        ResearchStaff researchStaff = currentResearchStaff();

        ExRegion region = raikouSystem.loadRegion(areaCode);
        // 这里仅仅是判断用户查该区域是否有权限，故直接通过areaCode反查
        boolean containsRegion = false;
        ResearchStaff.ManagedRegion managedRegion = researchStaff.getManagedRegion();
        if (areaCode != 0) {
            if (region != null) {
                if (region.fetchRegionType() == RegionType.PROVINCE) {
                    containsRegion = managedRegion.getProvinceCodes().contains(Long.valueOf(region.getProvinceCode()));
                } else if (region.fetchRegionType() == RegionType.CITY) {
                    containsRegion = managedRegion.getProvinceCodes().contains(Long.valueOf(region.getProvinceCode()))
                            || managedRegion.getCityCodes().contains(Long.valueOf(region.getCityCode()));
                } else if (region.fetchRegionType() == RegionType.COUNTY) {
                    containsRegion = managedRegion.getProvinceCodes().contains(Long.valueOf(region.getProvinceCode()))
                            || managedRegion.getCityCodes().contains(Long.valueOf(region.getCityCode()))
                            || managedRegion.getAreaCodes().contains(Long.valueOf(region.getCountyCode()));
                }
            }
        } else {
            // 街道教研员情况，此时areaCode为0
            // 临时方案，积分这块需要重新思考下逻辑，进而重构
            School school = schoolLoaderClient.getSchoolLoader()
                    .loadSchool(schoolId)
                    .getUninterruptibly();
            if (school != null) {
                areaCode = school.getRegionCode();
            }
            containsRegion = managedRegion.getSchoolIds().contains(schoolId);
        }

        // for province : only itself and all its counties
        // for city: itself and its counties
        // for county: itself
        List<ExRegion> regionNewList = new LinkedList<>();
        if (region != null) {
            regionNewList.add(region);
            if (region.fetchRegionType() != RegionType.PROVINCE) {
                regionNewList.addAll(ExRegion.fetchAllChildren(region));
            } else {
                List<ExRegion> children = ExRegion.fetchAllChildren(region);
                children = children.stream()
                        .filter(source -> source.fetchRegionType() == RegionType.COUNTY)
                        .collect(Collectors.toList());

                regionNewList.addAll(children);
            }
            Collections.sort(regionNewList);
        }
        List<Integer> areaList = regionNewList.stream().map(Region::getCode).collect(Collectors.toList());
        ResearchInfo userInfo = null;
        if (containsRegion) {
            if (schoolId == null || schoolId == 0) {
                userInfo = researchStaffServiceClient.getRemoteReference().getSchoolStatisticDataByDateAndRegionListAndSchool(researchStaff.getSubject(), beginTime, endTime, areaCode, researchStaff.getRegionType().getType(), areaList, 0L);
            } else {
                userInfo = researchStaffServiceClient.getRemoteReference().getSchoolStatisticDataByDateAndRegionListAndSchool(researchStaff.getSubject(), beginTime, endTime, areaCode, researchStaff.getRegionType().getType(), areaList, schoolId);
            }
        } else if (schoolId == 0 && areaCode == 0) {
            userInfo = researchStaffServiceClient.getRemoteReference().getSchoolStatisticDataByDateAndRegionListAndSchoolList(researchStaff.getSubject(), beginTime, endTime, areaCode, researchStaff.getRegionType().getType(), areaList, managedRegion.getSchoolIds());
        } else {
            logger.warn("查询用户汇总下分时查询,用户无权访问该区域(区域编码：" + areaCode + ")");
        }

        return MapMessage.successMessage().set("value", userInfo);
    }

    /**
     * NEW 教研员
     * 大数据报告 -->积分统计（即原来用户汇总） -- 分时查询
     */
    @RequestMapping(value = "integralstat/detail.vpage", method = RequestMethod.GET)
    public String detail(Model model) {
        ResearchStaff researchStaff = currentResearchStaff();
        Integer code = researchStaff.getRegionCode();
        String status = researchStaffServiceClient.getRemoteReference().validMarketJobTaskRunSuccessOrFaild();
        long time;
        if ("SUCCESS".equals(status)) {
            time = System.currentTimeMillis() - (3600 * 24 * 1000);
        } else {
            time = System.currentTimeMillis() - (3600 * 24 * 1000) * 2;
        }
        String date = com.voxlearning.alps.calendar.DateUtils.dateToString(new Date(time), com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE) + " 23:59:59";
        ResearchInfo resinfo;
        if (researchStaff.getResearchStaffType() != ResearchStaffType.STREET) {
            resinfo = researchStaffServiceClient.getRemoteReference().getSchoolStatisticData(researchStaff.getSubject(), date, null, code, researchStaff.getRegionType().getType(), null);
        } else {
            resinfo = researchStaffServiceClient.getRemoteReference().getSchoolStatisticDataBySchoolIds(researchStaff.getSubject(), date, null,
                    code, researchStaff.getRegionType().getType(), researchStaff.getManagedRegion().getSchoolIds());
        }
        // 初始化下拉框数据
        ResearchStaffType researchStaffType = researchStaff.getResearchStaffType();
        if (researchStaffType == ResearchStaffType.CITY) {
            Map<String, String> regionNameMap = getRegionNameMap(researchStaff.getManagedRegion().getCityCodes());
            model.addAttribute("nameMap", regionNameMap);
        } else if (researchStaffType == ResearchStaffType.COUNTY) {
            Map<String, String> regionNameMap = getRegionNameMap(researchStaff.getManagedRegion().getAreaCodes());
            model.addAttribute("nameMap", regionNameMap);
        } else if (researchStaffType == ResearchStaffType.STREET) {
            Map<String, String> schoolNameMap = getSchoolNameMap(researchStaff.getManagedRegion().getSchoolIds());
            model.addAttribute("nameMap", schoolNameMap);
        }
        model.addAttribute("role", researchStaffType.toString());
        model.addAttribute("code", code);
        model.addAttribute("region", researchStaff.getRegion().getName());
        model.addAttribute("authenticateNum", resinfo);
        return "rstaffv3/report/integralstat/detail";
    }


    /**
     * NEW 教研员
     * 大数据报告 --> 积分统计(即原来用户汇总) --->学校数据汇总
     * 不返回数据前端显示暂无相关数据
     */
    @RequestMapping(value = "integralstat/schoolsummarychip.vpage", method = RequestMethod.GET)
    public String getSchoolSummaryChip(Model model) {
//        ResearchStaff researchStaff = currentResearchStaff();
//        Integer code = researchStaff.getRegionCode();
//        String status = researchStaffServiceClient.getRemoteReference().validMarketJobTaskRunSuccessOrFaild();
//        long time;
//        if ("SUCCESS".equals(status)) {
//            time = System.currentTimeMillis() - (3600 * 24 * 1000);
//        } else {
//            time = System.currentTimeMillis() - (3600 * 24 * 1000) * 2;
//        }
//        String date = com.voxlearning.alps.calendar.DateUtils.dateToString(new Date(time), com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE) + " 23:59:59";
//        ResearchInfo resinfo = researchStaffServiceClient.getRemoteReference().getSchoolStatisticData(researchStaff.getSubject(), date, null, code, researchStaff.getRegionType().getType(), null);
//        if (resinfo != null) {   //.2test May be null
//            if (resinfo.getDetails() == null) {
//                resinfo.setDetails(new ArrayList<>());
//            }
//            List<BizMarketingSchoolData> bss = researchStaffServiceClient.getRemoteReference().getGroupbyTeacherIdFinderToSchoolStatistic(resinfo.getDetails());
//            if (bss == null) {
//                bss = new ArrayList<>();
//            }
//            GroupByFinder<BizMarketingSchoolData> scfinder = GroupByFinder.newInstance(BizMarketingSchoolData.class, bss);
//            scfinder.groupBy("province", "provinceCode", "city", "cityCode", "area", "areaCode", "schoolId", "schoolName");
//            scfinder.sum("classSize");
//            scfinder.sum("restaffAuthTotal");
//            Map<GroupByFinder.GroupByKey, Map<String, Object>> scgr = scfinder.find();
//            List<RstaffSchoolInfo> schoolInfos = new ArrayList<>();
//            for (GroupByFinder.GroupByKey key : scgr.keySet()) {
//                key.getCount();//教师数量
//                String province = (String) key.getValue("province");
//                String city = (String) key.getValue("city");
//                String area = (String) key.getValue("area");
//                String schoolName = (String) key.getValue("schoolName");
//                Integer stucount = (Integer) scgr.get(key).get("sum_classSize");
//                Integer rstaffCount = (Integer) scgr.get(key).get("sum_restaffAuthTotal");
//                RstaffSchoolInfo rsi = new RstaffSchoolInfo();
//                rsi.setProvince(province);
//                rsi.setCity(city);
//                rsi.setArea(area);
//                rsi.setSchoolName(schoolName);
//                rsi.setTeacherCount(key.getCount());
//                rsi.setStudentCount(stucount);
//                rsi.setRstaffCount(rstaffCount);
//                schoolInfos.add(rsi);
//            }
//            if (CollectionUtils.isNotEmpty(schoolInfos)) {
//                Collections.sort(schoolInfos, (arg0, arg1) -> arg1.getTeacherCount().compareTo(arg0.getTeacherCount()));
//            }
//            model.addAttribute("schoolResult", schoolInfos);
//        }
        return "rstaffv3/report/integralstat/schoolsummarychip";
    }


    /**
     * NEW 教研员
     * 大数据报告 --> 积分统计(即原来用户汇总) -->查询按钮
     */
    @RequestMapping(value = "integralstat/summarychip.vpage", method = RequestMethod.GET)
    public String summaryChip(Model model) {
        ResearchStaff researchStaff = currentResearchStaff();
        Integer code = researchStaff.getRegionCode();
        String status = researchStaffServiceClient.getRemoteReference().validMarketJobTaskRunSuccessOrFaild();
        long time;
        if ("SUCCESS".equals(status)) {
            time = System.currentTimeMillis() - (3600 * 24 * 1000);
        } else {
            time = System.currentTimeMillis() - (3600 * 24 * 1000) * 2;
        }
        String date = com.voxlearning.alps.calendar.DateUtils.dateToString(new Date(time), com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE) + " 23:59:59";
        ResearchInfo resinfo = researchStaffServiceClient.getRemoteReference().getSchoolStatisticData(researchStaff.getSubject(), date, null, code, researchStaff.getRegionType().getType(), null);

        if (resinfo != null) {   //.2test May be null
            for (BizMarketingSchoolData ri : resinfo.getDetails()) {
                try {
                    List<Long> clazzIds = new ArrayList<>();
                    clazzIds.add(ConversionUtils.toLong(ri.getClassId()));
                    List<BizStudentVoice> list = businessStudentServiceClient.loadClazzStudentVoices(clazzIds);
                    if (list.size() > 0 && !list.isEmpty()) {
                        ri.setIfVoice(list.size());
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (resinfo.getDetails() == null) {
                resinfo.setDetails(new ArrayList<>());
            }
            List<BizMarketingSchoolData> bss = researchStaffServiceClient.getRemoteReference().getGroupbyTeacherIdFinderToSchoolStatistic(resinfo.getDetails());
            resinfo.setDetails(bss);
        }
        logger.debug("UserInfo: {}", resinfo);
        model.addAttribute("authenticateNum", resinfo);
        return "rstaffv3/report/integralstat/summarychip";
    }


    /**
     * NEW 教研员
     * 大数据报告 --> 积分统计(即原来用户汇总) -->查询按钮
     * 根据区域编码查询区域下的学校班级详情
     */
    @RequestMapping(value = "integralstat/summarychip/search.vpage", method = {RequestMethod.GET})
    public String index_showtotal(@RequestParam("areaCode") int areaCode,
                                  @RequestParam("schoolId") Long schoolId,
                                  Model model) {
        ResearchStaff researchStaff = currentResearchStaff();

        // 这里仅仅是判断用户查该区域是否有权限，故直接通过areaCode反查
        boolean containsRegion = false;
        ResearchStaff.ManagedRegion managedRegion = researchStaff.getManagedRegion();
        ExRegion exRegion;
        if (areaCode != 0) {
            exRegion = raikouSystem.loadRegion(areaCode);
            if (exRegion != null) {
                if (exRegion.fetchRegionType() == RegionType.PROVINCE) {
                    containsRegion = managedRegion.getProvinceCodes().contains(Long.valueOf(exRegion.getProvinceCode()));
                } else if (exRegion.fetchRegionType() == RegionType.CITY) {
                    containsRegion = managedRegion.getProvinceCodes().contains(Long.valueOf(exRegion.getProvinceCode()))
                            || managedRegion.getCityCodes().contains(Long.valueOf(exRegion.getCityCode()));
                } else if (exRegion.fetchRegionType() == RegionType.COUNTY) {
                    containsRegion = managedRegion.getProvinceCodes().contains(Long.valueOf(exRegion.getProvinceCode()))
                            || managedRegion.getCityCodes().contains(Long.valueOf(exRegion.getCityCode()))
                            || managedRegion.getAreaCodes().contains(Long.valueOf(exRegion.getCountyCode()));
                }
            }
        } else {
            if (schoolId != 0) {
                // 街道教研员情况，此时areaCode为0
                // 临时方案，积分这块需要重新思考下逻辑，进而重构
                School school = schoolLoaderClient.getSchoolLoader()
                        .loadSchool(schoolId)
                        .getUninterruptibly();
                if (school != null) {
                    areaCode = school.getRegionCode();
                }
                containsRegion = managedRegion.getSchoolIds().contains(schoolId);
            } else {
                // 街道教研员 查询“全部”情况
                containsRegion = true;
            }
        }

        if (containsRegion) {
            String status = researchStaffServiceClient.getRemoteReference().validMarketJobTaskRunSuccessOrFaild();
            long time;
            if ("SUCCESS".equals(status)) {
                time = System.currentTimeMillis() - (3600 * 24 * 1000);
            } else {
                time = System.currentTimeMillis() - (3600 * 24 * 1000) * 2;
            }
            String date = com.voxlearning.alps.calendar.DateUtils.dateToString(new Date(time), com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE) + " 23:59:59";
            ResearchInfo resinfo;
            if (schoolId == 0) {
                if (areaCode != 0) {
                    resinfo = researchStaffServiceClient.getRemoteReference().getSchoolStatisticData(researchStaff.getSubject(), date, null, areaCode, researchStaff.getRegionType().getType(), null);
                } else {
                    resinfo = researchStaffServiceClient.getRemoteReference().getSchoolStatisticDataBySchoolIds(researchStaff.getSubject(), date, null, areaCode, researchStaff.getRegionType().getType(), managedRegion.getSchoolIds());
                }
            } else {
                resinfo = researchStaffServiceClient.getRemoteReference().getSchoolStatisticData(researchStaff.getSubject(), date, null, areaCode, researchStaff.getRegionType().getType(), schoolId);
            }

            if (resinfo != null) {   //.2test May be null
                for (BizMarketingSchoolData ri : resinfo.getDetails()) {
                    try {
                        List<Long> clazzIds = new ArrayList<>();
                        clazzIds.add(ConversionUtils.toLong(ri.getClassId()));
                        List<BizStudentVoice> list = businessStudentServiceClient.loadClazzStudentVoices(clazzIds);
                        if (list.size() > 0 && !list.isEmpty()) {
                            ri.setIfVoice(list.size());
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                if (resinfo.getDetails() == null) {
                    resinfo.setDetails(new ArrayList<>());
                }
                List<BizMarketingSchoolData> bss = researchStaffServiceClient.getRemoteReference().getGroupbyTeacherIdFinderToSchoolStatistic(resinfo.getDetails());
                resinfo.setDetails(bss);
            }
            model.addAttribute("authenticateNum", resinfo);
        } else {
            logger.warn("用户无权访问该区域(区域编码：" + areaCode + ")");
        }

        return "rstaffv3/report/integralstat/summarychip";
    }


    /**
     * NEW 教研员
     * 大数据报告 --> 积分统计(即原来用户汇总)
     * 根据教师Id获取录音列表
     */
    @RequestMapping(value = "voicelist.vpage", method = {RequestMethod.GET})
    public String showClazzVoice(@RequestParam("teacherId") Long teacherId, Model model) {
        List<Clazz> teacherClazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherId);

        Collection<Long> clazzIds = teacherClazzs.stream()
                .filter(t -> t != null)
                .filter(t -> !t.isTerminalClazz())
                .map(Clazz::getId)
                .filter(t -> t != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        try {
            List<BizStudentVoice> list = businessStudentServiceClient.loadClazzStudentVoices(clazzIds);
            model.addAttribute("classResult", list);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "rstaffv3/report/integralstat/voicelist";
    }


    /**
     * NEW 教研员
     * 大数据报告--> 积分统计(即原来用户汇总) --->下载
     * 下载文件
     */
    @RequestMapping(value = "integralstat/downloadsummarychip.vpage", method = RequestMethod.GET)
    public void downloadsummarychip(@RequestParam("areaCode") int areaCode,
                                    @RequestParam("schoolId") Long schoolId,
                                    HttpServletResponse response) {
        ResearchStaff researchStaff = currentResearchStaff();
        List<Region> regionList = new LinkedList<>();
        ExRegion region = researchStaff.getRegion();
        if (region != null) {
            regionList.add(region);
            regionList.addAll(ExRegion.fetchAllChildren(region));
            Collections.sort(regionList);
        }
        boolean containsRegion = false;
        //执行Excel处理
        // 设置excel标题行
        List<Object[]> title = new ArrayList<>();
        Object[] titleDetail = new Object[]{"省份", "地级市", "区域", "学校名称", "老师编号", "老师姓名", "班级数量", "注册学生人数", "认证使用学生数量"};
        title.add(titleDetail);
        //设置Excel内容
        List<List<Object[]>> objectList = new ArrayList<>();
        List<Object[]> objects = new ArrayList<>();

        for (Region reg : regionList) {
            if (reg.getCode() == areaCode) {
                containsRegion = true;
                break;
            }
        }
        if (containsRegion) {
            String status = researchStaffServiceClient.getRemoteReference().validMarketJobTaskRunSuccessOrFaild();
            long time;
            if ("SUCCESS".equals(status)) {
                time = System.currentTimeMillis() - (3600 * 24 * 1000);
            } else {
                time = System.currentTimeMillis() - (3600 * 24 * 1000) * 2;
            }
            String date = com.voxlearning.alps.calendar.DateUtils.dateToString(new Date(time), com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE) + " 23:59:59";
            String DateFormat = com.voxlearning.alps.calendar.DateUtils.dateToString(new Date(time), com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE);
            ResearchInfo resinfo;
            if (schoolId == 0) {
                resinfo = researchStaffServiceClient.getRemoteReference().getSchoolStatisticData(researchStaff.getSubject(), date, null, researchStaff.getRegionCode(), researchStaff.getRegionType().getType(), null);
            } else {
                resinfo = researchStaffServiceClient.getRemoteReference().getSchoolStatisticData(researchStaff.getSubject(), date, null, researchStaff.getRegionCode(), researchStaff.getRegionType().getType(), schoolId);
            }

            if (resinfo != null) {   //.2test May be null
                if (resinfo.getDetails() == null) {
                    resinfo.setDetails(new ArrayList<>());
                }
                List<BizMarketingSchoolData> bss = researchStaffServiceClient.getRemoteReference().getGroupbyTeacherIdFinderToSchoolStatistic(resinfo.getDetails());
                if (CollectionUtils.isNotEmpty(bss)) {
                    for (BizMarketingSchoolData bschool : bss) {
                        objects.add(new Object[]{
                                bschool.getProvince(),
                                bschool.getCity(),
                                bschool.getArea(),
                                bschool.getSchoolName(),
                                bschool.getTeacherId(),
                                bschool.getTeacherName(),
                                bschool.getClassNumber(),
                                bschool.getClassSize(),
                                bschool.getRestaffAuthTotal()
                        });
                    }
                }
                objectList.add(objects);
//                downLoadExcelSheets("教研员用户汇总" + DateFormat, new String[]{"以老师为单位汇总"}, title, objectList, EXCEL_VERSION_07);
                List<POIExcel.TitleGenerator> titleGenerators = new ArrayList<>();
                titleGenerators.add(new POIExcel.DefaultTitleGenerator(null, title.get(0)));

                List<POIExcel.ContentGenerator> contentGenerators = new ArrayList<>();
                contentGenerators.add(new POIExcel.DefaultContentGenerator(objectList.get(0)));

                POIExcel.downLoadExcelSheets(getRequest(),
                        response,
                        POIExcel.EXCEL_VERSION_07,
                        "教研员用户汇总" + DateFormat,
                        new String[]{"以老师为单位汇总"},
                        titleGenerators,
                        contentGenerators,
                        null);
            } else {

                objectList.add(objects);

//                downLoadExcelSheets("教研员用户汇总" + DateFormat, new String[]{"以老师为单位汇总"}, title, objectList, EXCEL_VERSION_07);

                List<POIExcel.TitleGenerator> titleGenerators = new ArrayList<>();
                titleGenerators.add(new POIExcel.DefaultTitleGenerator(null, title.get(0)));

                List<POIExcel.ContentGenerator> contentGenerators = new ArrayList<>();
                contentGenerators.add(new POIExcel.DefaultContentGenerator(objectList.get(0)));

                POIExcel.downLoadExcelSheets(getRequest(),
                        response,
                        POIExcel.EXCEL_VERSION_07,
                        "教研员用户汇总" + DateFormat,
                        new String[]{"以老师为单位汇总"},
                        titleGenerators,
                        contentGenerators,
                        null);
            }

        } else {
            logger.warn("用户无权访问该区域(区域编码：" + areaCode + ")");
        }
    }


    /**
     * NEW 教研员
     * 大数据报告 - 积分统计 -->金币记录
     */
    @RequestMapping(value = "integralstat/goldrecord.vpage", method = RequestMethod.GET)
    public String goldRecord() {
        return "rstaffv3/report/integralstat/goldrecord";
    }

    /**
     * NEW 教研员
     * 大数据报告 - 积分统计 -->金币记录 --详情分页
     */
    @RequestMapping(value = "integralstat/goldrecordchip.vpage", method = RequestMethod.GET)
    public String integralHistory(Model model) {
        int pageNumber = getRequestInt("pageNumber");
        // 获取金币前六个月的历史数据
        UserIntegralHistoryPagination pagination = userLoaderClient
                .loadUserIntegralHistories(currentUser(), 6, pageNumber - 1, 20);
        model.addAttribute("pagination", pagination);
        return "rstaffv3/report/integralstat/goldrecordchip";
    }

    @RequestMapping(value = "exportInformation.vpage", method = RequestMethod.GET)
    public void exportInformation(HttpServletResponse response) {

        Integer type = this.getRequestInt("type");
        ResearchStaff researchStaff = currentResearchStaff();
        SpecifiedTerm specifiedTerm = getSpecifiedTermFromRequest();
        ResearchStaffSkillMonthlyMapper researchStaffSkillMonthlyMapper = washingtonCacheSystem.CBS.flushable
                .wrapCache(researchStaffServiceClient.getRemoteReference())
                .keys(researchStaff.getId(), specifiedTerm.year, specifiedTerm.term)
                .expiration(CachedObjectExpirationPolicy.today)
                .proxy()
                .getSkillMonthlyData(researchStaff.getManagedRegion().getProvinceCodes(),
                        researchStaff.getManagedRegion().getCityCodes(),
                        researchStaff.getManagedRegion().getAreaCodes(),
                        researchStaff.getManagedRegion().getSchoolIds(),
                        specifiedTerm.year, specifiedTerm.term);
        SchoolYear schoolYear;
        DateRange dateRange;
        Date endDate;
        if (specifiedTerm.year == null || specifiedTerm.term == null) {
            schoolYear = SchoolYear.newInstance(new Date());
            dateRange = schoolYear.currentTermDateRange();
            endDate = MonthRange.current().next().getStartDate();
        } else {
            schoolYear = SchoolYear.newInstance(specifiedTerm.year);
            dateRange = schoolYear.getDateRangeByTerm(specifiedTerm.term);
            endDate = MonthRange.newInstance(dateRange.getEndTime()).next().getStartDate();
        }
        Date monthDate = dateRange.getStartDate();
        List<String> dateList = new LinkedList<>();
        while (monthDate.before(endDate)) {
            dateList.add(com.voxlearning.alps.calendar.DateUtils.dateToString(monthDate, "yyyy-MM"));
            monthDate = MonthRange.newInstance(monthDate.getTime()).next().getStartDate();
        }
        String fileName;
        ResearchStaffSkillMonthlyUnitMapper researchStaffSkillMonthlyUnitMapper;
        String[] titles = {"月份", "学校名称", "总做题量", "做题正确率"};
        if (type == 1) {
            fileName = "听报告.xls";
            researchStaffSkillMonthlyUnitMapper = researchStaffSkillMonthlyMapper.getListening();
        } else if (type == 2) {
            fileName = "说报告.xls";
            researchStaffSkillMonthlyUnitMapper = researchStaffSkillMonthlyMapper.getSpeaking();
        } else if (type == 3) {
            fileName = "读报告.xls";
            researchStaffSkillMonthlyUnitMapper = researchStaffSkillMonthlyMapper.getReading();
        } else {
            fileName = "写报告.xls";
            researchStaffSkillMonthlyUnitMapper = researchStaffSkillMonthlyMapper.getWritten();
        }
        if (researchStaffSkillMonthlyUnitMapper != null) {
            exportExcel(fileName, titles, researchStaffSkillMonthlyUnitMapper, response, dateList);
        }
    }

    private void exportExcel(String fileName, String[] titles, ResearchStaffSkillMonthlyUnitMapper researchStaffSkillMonthlyUnitMapper, HttpServletResponse response, List<String> dateList) {

        WritableWorkbook workbook = null;
        try {
            OutputStream os = response.getOutputStream();// 取得输出流
            response.reset();// 清空输出流
            response.setHeader("Content-disposition", "attachment; filename=" + new String(fileName.getBytes("GB2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");// 定义输出类型
            workbook = Workbook.createWorkbook(os);
            WritableSheet sheet = workbook.createSheet("Sheet1", 0);
            jxl.SheetSettings sheetSettings = sheet.getSettings();
            sheetSettings.setProtected(false);

            WritableFont NormalFont = new WritableFont(WritableFont.ARIAL, 10);
            WritableFont BoldFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);

            // 用于标题居中
            WritableCellFormat wcf_center = new WritableCellFormat(BoldFont);
            wcf_center.setBorder(Border.ALL, BorderLineStyle.THIN); // 线条
            wcf_center.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐
            wcf_center.setAlignment(Alignment.CENTRE); // 文字水平对齐
            wcf_center.setWrap(false); // 文字是否换行

            // 用于正文居左
            WritableCellFormat wcf_left = new WritableCellFormat(NormalFont);
            wcf_left.setBorder(Border.NONE, BorderLineStyle.THIN); // 线条
            wcf_left.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐
            wcf_left.setAlignment(Alignment.LEFT); // 文字水平对齐
            wcf_left.setWrap(false); // 文字是否换行

            for (int i = 0; i < titles.length; i++) {
                sheet.addCell(new Label(i, 0, titles[i], wcf_center));
            }

            if (researchStaffSkillMonthlyUnitMapper.getNames() != null) {
                List<String> names = researchStaffSkillMonthlyUnitMapper.getNames();
                int i = 0;
                int k = 0;
                Map<String, List<Double>> monthlyRate = researchStaffSkillMonthlyUnitMapper.getMonthlyRate();
                Map<String, List<Integer>> monthlySum = researchStaffSkillMonthlyUnitMapper.getMonthlySum();
                for (String name : names) {
                    for (int j = 0; j < dateList.size(); j++) {
                        List<Double> monthlyRateList = monthlyRate.get(dateList.get(j));
                        List<Integer> monthlySumList = monthlySum.get(dateList.get(j));
                        sheet.addCell(new Label(0, i + 1, dateList.get(j), wcf_left));
                        sheet.addCell(new Label(1, i + 1, name, wcf_left));
                        sheet.addCell(new Label(2, i + 1, monthlySumList.get(k).toString(), wcf_left));
                        sheet.addCell(new Label(3, i + 1, monthlyRateList.get(k).toString(), wcf_left));
                        i++;
                    }
                    k++;
                }
            }
            workbook.write();
        } catch (Exception e) {
            logger.error("教研员数据Excel文件导出失败", e);
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }


    /**
     * NEW 教研员
     * 大数据报告 - 知识数据 -->总表
     */
    @RequestMapping(value = "skillchart.vpage", method = RequestMethod.GET)
    public String getSkillChart(Model model) {
        ResearchStaff researchStaff = currentResearchStaff();
        if (researchStaff.getResearchStaffType() == ResearchStaffType.UNKNOWN) {
            logger.warn("researchStaff no permission,researchId:{},regionType:{}", researchStaff.getId(), researchStaff.getRegionType());
            return "";
        }

        SpecifiedTerm specifiedTerm = getSpecifiedTermFromRequest();

        ResearchStaffSkillMonthlyMapper researchStaffSkillMonthlyMapper = washingtonCacheSystem.CBS.flushable
                .wrapCache(researchStaffServiceClient.getRemoteReference())
                .keys(researchStaff.getId(), specifiedTerm.year, specifiedTerm.term)
                .expiration(CachedObjectExpirationPolicy.today)
                .proxy()
                .getSkillMonthlyData(researchStaff.getManagedRegion().getProvinceCodes(),
                        researchStaff.getManagedRegion().getCityCodes(),
                        researchStaff.getManagedRegion().getAreaCodes(),
                        researchStaff.getManagedRegion().getSchoolIds(),
                        specifiedTerm.year, specifiedTerm.term);
        model.addAttribute("skillMonthMapper", researchStaffSkillMonthlyMapper);
        SchoolYear schoolYear;
        DateRange dateRange;
        Date endDate;
        if (specifiedTerm.year == null || specifiedTerm.term == null) {
            schoolYear = SchoolYear.newInstance(new Date());
            dateRange = schoolYear.currentTermDateRange();
            endDate = MonthRange.current().next().getStartDate();
        } else {
            schoolYear = SchoolYear.newInstance(specifiedTerm.year);
            dateRange = schoolYear.getDateRangeByTerm(specifiedTerm.term);
            endDate = MonthRange.newInstance(dateRange.getEndTime()).next().getStartDate();
        }
        Date monthDate = dateRange.getStartDate();
        Set<String> dateSet = new LinkedHashSet<>();
        while (monthDate.before(endDate)) {
            dateSet.add(com.voxlearning.alps.calendar.DateUtils.dateToString(monthDate, "yyyy-MM"));
            monthDate = MonthRange.newInstance(monthDate.getTime()).next().getStartDate();
        }
        model.addAttribute("dateSet", dateSet);
        if (researchStaff.getRegionType() == RegionType.COUNTY) {
            return "rstaffv3/report/htmlchip/skillchart";
        } else {
            return "rstaffv3/report/htmlchip/areaskillchart";
        }

    }

    /**
     * NEW 教研员
     * 大数据报告 - 知识数据 -->题型数据
     */
    @RequestMapping(value = "patterndata.vpage", method = RequestMethod.GET)
    public String getPatternData(Model model) {
        SpecifiedTerm specifiedTerm = getSpecifiedTermFromRequest();

        ResearchStaff researchStaff = currentResearchStaff();
        ResearchStaffPatternMapper researchStaffPatternMapper = washingtonCacheSystem.CBS.flushable
                .wrapCache(researchStaffServiceClient.getRemoteReference())
                .keys(researchStaff.getId(), specifiedTerm.year, specifiedTerm.term)
                .expiration(com.voxlearning.alps.calendar.DateUtils.getCurrentToDayEndSecond())
                .proxy()
                .getPatternData(researchStaff.getManagedRegion().getProvinceCodes(),
                        researchStaff.getManagedRegion().getCityCodes(),
                        researchStaff.getManagedRegion().getAreaCodes(),
                        researchStaff.getManagedRegion().getSchoolIds(),
                        specifiedTerm.year, specifiedTerm.term);
        model.addAttribute("patternData", researchStaffPatternMapper);
        return "rstaffv3/report/htmlchip/patternchip";
    }

    /**
     * NEW 教研员
     * 大数据报告 - 知识数据 -->语言技能
     */
    @RequestMapping(value = "skilldata.vpage", method = RequestMethod.GET)
    public String getSkillData(Model model) {
        SpecifiedTerm specifiedTerm = getSpecifiedTermFromRequest();

        ResearchStaff researchStaff = currentResearchStaff();
        ResearchStaffSkillMapper researchStaffSkillMapper = washingtonCacheSystem.CBS.flushable
                .wrapCache(researchStaffServiceClient.getRemoteReference())
                .keys(researchStaff.getId(), specifiedTerm.year, specifiedTerm.term)
                .expiration(CachedObjectExpirationPolicy.today)
                .proxy()
                .getSkillData(researchStaff.getManagedRegion().getProvinceCodes(),
                        researchStaff.getManagedRegion().getCityCodes(),
                        researchStaff.getManagedRegion().getAreaCodes(),
                        researchStaff.getManagedRegion().getSchoolIds(),
                        specifiedTerm.year, specifiedTerm.term);
        model.addAttribute("skillMapper", researchStaffSkillMapper);
        return "rstaffv3/report/htmlchip/skilldata";
    }

    /**
     * NEW 教研员
     * 大数据报告 - 知识数据 -->语言知识
     */
    @RequestMapping(value = "languagedata.vpage", method = RequestMethod.GET)
    public String getKnowledgeData(Model model) {
        SpecifiedTerm specifiedTerm = getSpecifiedTermFromRequest();

        ResearchStaff researchStaff = currentResearchStaff();
        ResearchStaffKnowledgeMapper researchStaffKnowledgeMapper = washingtonCacheSystem.CBS.flushable
                .wrapCache(researchStaffServiceClient.getRemoteReference())
                .keys(researchStaff.getId(), specifiedTerm.year, specifiedTerm.term)
                .expiration(CachedObjectExpirationPolicy.today)
                .proxy()
                .getKnowledgeData(researchStaff.getManagedRegion().getProvinceCodes(),
                        researchStaff.getManagedRegion().getCityCodes(),
                        researchStaff.getManagedRegion().getAreaCodes(),
                        researchStaff.getManagedRegion().getSchoolIds(),
                        specifiedTerm.year, specifiedTerm.term);
        model.addAttribute("languageMapper", researchStaffKnowledgeMapper);
        return "rstaffv3/report/htmlchip/languagedata";
    }

    /**
     * NEW 教研员
     * 大数据报告 - 知识数据 -->各区/校薄弱
     */
    @RequestMapping(value = "weakknowledge.vpage", method = RequestMethod.GET)
    public String getWeakPoint(Model model) {
        SpecifiedTerm specifiedTerm = getSpecifiedTermFromRequest();

        ResearchStaff researchStaff = currentResearchStaff();
        List<ResearchStaffWeakPointUnitMapper> researchStaffWeakPointUnitMappers = washingtonCacheSystem.CBS.flushable
                .wrapCache(researchStaffServiceClient.getRemoteReference())
                .keys(researchStaff.getId(), specifiedTerm.year, specifiedTerm.term)
                .expiration(CachedObjectExpirationPolicy.today)
                .proxy()
                .getWeakPointData(researchStaff.getManagedRegion().getProvinceCodes(),
                        researchStaff.getManagedRegion().getCityCodes(),
                        researchStaff.getManagedRegion().getAreaCodes(),
                        researchStaff.getManagedRegion().getSchoolIds(),
                        specifiedTerm.year, specifiedTerm.term);
        model.addAttribute("weekPointMapperList", researchStaffWeakPointUnitMappers);
        return "rstaffv3/report/htmlchip/weakknowledge";
    }

    /**
     * NEW 教研员
     * 大数据报告 - 知识数据 -->单元薄弱
     */
    @RequestMapping(value = "unitknowledgeweak.vpage", method = RequestMethod.GET)
    public String getUnitknowledgeweak(Model model) {
        SpecifiedTerm specifiedTerm = getSpecifiedTermFromRequest();

        ResearchStaff researchStaff = currentResearchStaff();
        ResearchStaffUnitWeakPointMapper researchStaffUnitWeakPointMapper = washingtonCacheSystem.CBS.flushable
                .wrapCache(researchStaffServiceClient.getRemoteReference())
                .keys(researchStaff.getId(), specifiedTerm.year, specifiedTerm.term)
                .expiration(CachedObjectExpirationPolicy.today)
                .proxy()
                .getUnitWeakPointData(researchStaff.getManagedRegion().getCityCodes(),
                        researchStaff.getManagedRegion().getAreaCodes(),
                        researchStaff.getManagedRegion().getSchoolIds(),
                        specifiedTerm.year, specifiedTerm.term);
        model.addAttribute("unitWeakPointMapper", researchStaffUnitWeakPointMapper);
        return "rstaffv3/report/htmlchip/unitknowledgeweak";
    }

    /**
     * NEW 教研员
     * 大数据报告 -- 行为数据
     */
    @RequestMapping(value = "behaviordata.vpage", method = RequestMethod.GET)
    public String getBehaviordata(Model model) {
        DateRange dateRange = SchoolYear.newInstance().currentTermDateRange();
        Term term = SchoolYear.newInstance().currentTerm();
        String termText;
        int endYear = DayRange.newInstance(dateRange.getEndTime()).getYear();
        if (term == Term.下学期) {
            termText = (endYear - 1) + "-" + endYear + "学年第二学期";
        } else {
            termText = (endYear - 1) + "-" + endYear + "学年第一学期";
        }
        model.addAttribute("termText", termText);
        //数据更新日期
        model.addAttribute("updateDate", com.voxlearning.alps.calendar.DateUtils.dateToString(DayRange.current().getStartDate(), com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE));

        //历史学期
        List<String> historyYearStr = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, 9, 1);   // 起始可查询年份
        while (calendar.getTime().before(dateRange.getEndDate())) {
            int year = calendar.get(Calendar.YEAR);
            String str = year + "-" + (year + 1);
            historyYearStr.add(str);

            calendar.add(Calendar.YEAR, 1);
        }
        model.addAttribute("historyYears", historyYearStr);

        //根据输入确定所在学期
        String year = getRequest().getParameter("year");
        if (year != null) {
            model.addAttribute("year", year);
            model.addAttribute("term", getRequest().getParameter("term"));
        } else {
            int startYear = SchoolYear.newInstance().year();
            model.addAttribute("year", String.valueOf(startYear) + "-" + String.valueOf(startYear + 1));
            model.addAttribute("term", String.valueOf(SchoolYear.newInstance().currentTerm().getKey()));
        }
        return "rstaffv3/report/behaviordata";
    }

    /**
     * NEW 教研员
     * 大数据报告 -- 行为数据数据表
     */
    @RequestMapping(value = "behaviordatachip.vpage", method = RequestMethod.GET)
    public String getBehaviordatachip(Model model) {
        // 获取行为数据
        SpecifiedTerm specifiedTerm = getSpecifiedTermFromRequest();
        if (specifiedTerm.year == null || specifiedTerm.term == null) {
            SchoolYear schoolYear = SchoolYear.newInstance();
            specifiedTerm.year = schoolYear.year();
            specifiedTerm.term = schoolYear.currentTerm();
        }
        ResearchStaff researchStaff = currentResearchStaff();
        List<ResearchStaffBehaviorDataMapper> researchStaffBehaviorDataMappers = washingtonCacheSystem.CBS.flushable
                .wrapCache(researchStaffServiceClient.getRemoteReference())
                .keys(researchStaff.getId(), specifiedTerm.year, specifiedTerm.term.getKey())
                .expiration(CachedObjectExpirationPolicy.today)
                .proxy()
                .getBehaviorData(researchStaff.getId(), researchStaff.getSubject(),
                        researchStaff.getManagedRegion().getProvinceCodes(),
                        researchStaff.getManagedRegion().getCityCodes(),
                        researchStaff.getManagedRegion().getAreaCodes(),
                        researchStaff.getManagedRegion().getSchoolIds(), specifiedTerm.year, specifiedTerm.term);
        // 统计汇总信息
        ResearchStaffBehaviorDataMapper summaryMapper = calBehaviorSummaryData(researchStaffBehaviorDataMappers, researchStaff);
        model.addAttribute("summaryMapper", summaryMapper);

        model.addAttribute("behaviorList", researchStaffBehaviorDataMappers);
        return "rstaffv3/report/htmlchip/behaviordatachip";
    }


    private static String[] sheetNamesForProvinceRS = {"语言技能", "语言知识", "题型数据"};
    private static String[] sheetNamesForCityRS = {"语言技能", "语言知识", "各区薄弱", "教材薄弱", "题型数据"};
    private static String[] sheetNamesForCountyRS = {"语言技能", "语言知识", "各校薄弱", "教材薄弱", "题型数据"};

    private static Object[] rsWeakPointTitlesForRegion = {"区域名称", "词汇", "语法", "话题"};
    private static Object[] rsWeakPointTitlesForSchool = {"学校名称", "词汇", "语法", "话题"};
    private static Object[] rsUnitWeakPointTitlesForRegion = {"区域名称", "年级", "教材版本", "薄弱知识点"};
    private static Object[] rsUnitWeakPointTitlesForSchool = {"学校名称", "年级", "教材版本", "薄弱知识点"};

    @RequestMapping(value = "downloadknowledgedata.vpage", method = RequestMethod.GET)
    public void downloadKnowledgeData(HttpServletResponse response) {
        SpecifiedTerm specifiedTerm = getSpecifiedTermFromRequest();

        ResearchStaff researchStaff = currentResearchStaff();

        // 语言技能数据
        ResearchStaffSkillMapper researchStaffSkillMapper = washingtonCacheSystem.CBS.flushable
                .wrapCache(researchStaffServiceClient.getRemoteReference())
                .keys(researchStaff.getId(), specifiedTerm.year, specifiedTerm.term)
                .expiration(CachedObjectExpirationPolicy.today)
                .proxy()
                .getSkillData(researchStaff.getManagedRegion().getProvinceCodes(),
                        researchStaff.getManagedRegion().getCityCodes(),
                        researchStaff.getManagedRegion().getAreaCodes(),
                        researchStaff.getManagedRegion().getSchoolIds(),
                        specifiedTerm.year, specifiedTerm.term);

        // 语言知识数据
        ResearchStaffKnowledgeMapper researchStaffKnowledgeMapper = washingtonCacheSystem.CBS.flushable
                .wrapCache(researchStaffServiceClient.getRemoteReference())
                .keys(researchStaff.getId(), specifiedTerm.year, specifiedTerm.term)
                .expiration(CachedObjectExpirationPolicy.today)
                .proxy()
                .getKnowledgeData(researchStaff.getManagedRegion().getProvinceCodes(),
                        researchStaff.getManagedRegion().getCityCodes(),
                        researchStaff.getManagedRegion().getAreaCodes(),
                        researchStaff.getManagedRegion().getSchoolIds(),
                        specifiedTerm.year, specifiedTerm.term);

        // 各区/校薄弱
        List<ResearchStaffWeakPointUnitMapper> researchStaffWeakPointUnitMappers = washingtonCacheSystem.CBS.flushable
                .wrapCache(researchStaffServiceClient.getRemoteReference())
                .keys(researchStaff.getId(), specifiedTerm.year, specifiedTerm.term)
                .expiration(CachedObjectExpirationPolicy.today)
                .proxy()
                .getWeakPointData(researchStaff.getManagedRegion().getProvinceCodes(),
                        researchStaff.getManagedRegion().getCityCodes(),
                        researchStaff.getManagedRegion().getAreaCodes(),
                        researchStaff.getManagedRegion().getSchoolIds(),
                        specifiedTerm.year, specifiedTerm.term);

        // 单元薄弱
        ResearchStaffUnitWeakPointMapper researchStaffUnitWeakPointMapper = washingtonCacheSystem.CBS.flushable
                .wrapCache(researchStaffServiceClient.getRemoteReference())
                .keys(researchStaff.getId(), specifiedTerm.year, specifiedTerm.term == null ? -1 : specifiedTerm.term.getKey())
                .expiration(CachedObjectExpirationPolicy.today)
                .proxy()
                .getUnitWeakPointData(researchStaff.getManagedRegion().getCityCodes(),
                        researchStaff.getManagedRegion().getAreaCodes(),
                        researchStaff.getManagedRegion().getSchoolIds(),
                        specifiedTerm.year, specifiedTerm.term);

        // 题型数据
        ResearchStaffPatternMapper researchStaffPatternMapper = washingtonCacheSystem.CBS.flushable
                .wrapCache(researchStaffServiceClient.getRemoteReference())
                .keys(researchStaff.getId(), specifiedTerm.year, specifiedTerm.term)
                .expiration(CachedObjectExpirationPolicy.today)
                .proxy()
                .getPatternData(researchStaff.getManagedRegion().getProvinceCodes(),
                        researchStaff.getManagedRegion().getCityCodes(),
                        researchStaff.getManagedRegion().getAreaCodes(),
                        researchStaff.getManagedRegion().getSchoolIds(),
                        specifiedTerm.year, specifiedTerm.term);

        // TODO need add more info in the file name
        String fileName = researchStaff.formatManagedRegionStr() + "小学" + researchStaff.getSubject().getValue() + "数据分析报告";
        String[] sheetNames;
        if (researchStaff.getResearchStaffType() == ResearchStaffType.PROVINCE) {
            sheetNames = sheetNamesForProvinceRS;
        } else if (researchStaff.getResearchStaffType() == ResearchStaffType.CITY) {
            sheetNames = sheetNamesForCityRS;
        } else {
            sheetNames = sheetNamesForCountyRS;
        }

        // 表头
        List<POIExcel.TitleGenerator> titleGenerators = new ArrayList<>();
        titleGenerators.add(new RSSKillDataTiltleGenerator(researchStaff.getResearchStaffType()));
        titleGenerators.add(new RSKnowledgeDataTitleGenerator(researchStaff.getResearchStaffType()));
        if (researchStaff.getResearchStaffType() != ResearchStaffType.PROVINCE) {
            if (researchStaff.getResearchStaffType() == ResearchStaffType.CITY) {
                titleGenerators.add(new POIExcel.DefaultTitleGenerator(null, rsWeakPointTitlesForRegion));
                titleGenerators.add(new POIExcel.DefaultTitleGenerator(null, rsUnitWeakPointTitlesForRegion));
            } else {
                titleGenerators.add(new POIExcel.DefaultTitleGenerator(null, rsWeakPointTitlesForSchool));
                titleGenerators.add(new POIExcel.DefaultTitleGenerator(null, rsUnitWeakPointTitlesForSchool));
            }
        }
        if (researchStaffPatternMapper != null && researchStaffPatternMapper.getPatternRank() != null) {
            titleGenerators.add(new RSPatternDataTitleGenerator(researchStaffPatternMapper.getPatternRank().keySet(), researchStaff.getResearchStaffType()));
        }

        // 报告内容
        List<POIExcel.ContentGenerator> contentGenerators = new ArrayList<>();
        contentGenerators.add(new POIExcel.DefaultContentGenerator(generateRSSkillDataContentForExcel(researchStaffSkillMapper)));
        contentGenerators.add(new POIExcel.DefaultContentGenerator(generateRSKnowledgeDataContentForExcel(researchStaffKnowledgeMapper)));
        if (researchStaff.getResearchStaffType() != ResearchStaffType.PROVINCE) {
            contentGenerators.add(new POIExcel.DefaultContentGenerator(generateRSWeakPointDataContentForExcel(researchStaffWeakPointUnitMappers)));
            contentGenerators.add(new POIExcel.DefaultContentGenerator(generateRSUnitWeakPointDataContentForExcel(researchStaffUnitWeakPointMapper), true));
        }
        contentGenerators.add(new POIExcel.DefaultContentGenerator(generateRSPatternDataContentForExcel(researchStaffPatternMapper)));

        // 下载报告
        POIExcel.downLoadExcelSheets(getRequest(),
                response,
                POIExcel.EXCEL_VERSION_03,
                fileName,
                sheetNames,
                titleGenerators,
                contentGenerators,
                null);
    }

    @RequestMapping(value = "downloadbehaviordata.vpage", method = RequestMethod.GET)
    public void downloadBehaviorData(HttpServletResponse response) {
        SpecifiedTerm specifiedTerm = getSpecifiedTermFromRequest();
        if (specifiedTerm.year == null || specifiedTerm.term == null) {
            SchoolYear schoolYear = SchoolYear.newInstance();
            specifiedTerm.year = schoolYear.year();
            specifiedTerm.term = schoolYear.currentTerm();
        }
        // 获取行为数据
        ResearchStaff researchStaff = currentResearchStaff();
        List<ResearchStaffBehaviorDataMapper> researchStaffBehaviorDataMappers = washingtonCacheSystem.CBS.flushable
                .wrapCache(researchStaffServiceClient.getRemoteReference())
                .keys(researchStaff.getId(), specifiedTerm.year, specifiedTerm.term.getKey())
                .expiration(CachedObjectExpirationPolicy.today)
                .proxy()
                .getBehaviorData(researchStaff.getId(), researchStaff.getSubject(),
                        researchStaff.getManagedRegion().getProvinceCodes(),
                        researchStaff.getManagedRegion().getCityCodes(),
                        researchStaff.getManagedRegion().getAreaCodes(),
                        researchStaff.getManagedRegion().getSchoolIds(), specifiedTerm.year, specifiedTerm.term);
        // 统计汇总信息
        ResearchStaffBehaviorDataMapper summaryMapper = calBehaviorSummaryData(researchStaffBehaviorDataMappers, researchStaff);

        List<POIExcel.TitleGenerator> titleGenerators = new ArrayList<>();
        titleGenerators.add(new RSBehaviorDataTitleGenerator(researchStaff.getSubject(), researchStaff.getResearchStaffType()));
        List<POIExcel.ContentGenerator> contentGenerators = new ArrayList<>();

        List<Object[]> content = generateRSBehaviorContentDataForExcel(researchStaff, researchStaffBehaviorDataMappers, summaryMapper);
        contentGenerators.add(new POIExcel.DefaultContentGenerator(content));

        String fileName = researchStaff.formatManagedRegionStr() + "小学" + researchStaff.getSubject().getValue() + "作业行为统计报告";
        POIExcel.downLoadExcelSheets(getRequest(),
                response,
                POIExcel.EXCEL_VERSION_03,
                fileName,
                new String[]{"行为报告"},
                titleGenerators,
                contentGenerators,
                null);
    }


    /////////////////////////////////////////////////私有方法////////////////////////////////////////////////////

    private ResearchStaffBehaviorDataMapper calBehaviorSummaryData(List<ResearchStaffBehaviorDataMapper> researchStaffBehaviorDataMappers, ResearchStaff researchStaff) {
        ResearchStaffBehaviorDataMapper summaryMapper = new ResearchStaffBehaviorDataMapper();
        for (ResearchStaffBehaviorDataMapper mapper : researchStaffBehaviorDataMappers) {
            summaryMapper.setSchoolNum(summaryMapper.getSchoolNum() + mapper.getSchoolNum());

            summaryMapper.setHomeworkTeacherNum(summaryMapper.getHomeworkTeacherNum() + mapper.getHomeworkTeacherNum());
            summaryMapper.setHomeworkTeacherTime(summaryMapper.getHomeworkTeacherTime() + mapper.getHomeworkTeacherTime());
//            summaryMapper.setQuizTeacherNum(summaryMapper.getQuizTeacherNum() + mapper.getQuizTeacherNum());
//            summaryMapper.setQuizTeacherTime(summaryMapper.getQuizTeacherTime() + mapper.getQuizTeacherTime());

            summaryMapper.setHomeworkStuNum(summaryMapper.getHomeworkStuNum() + mapper.getHomeworkStuNum());
            summaryMapper.setHomeworkStuTime(summaryMapper.getHomeworkStuTime() + mapper.getHomeworkStuTime());
//            summaryMapper.setQuizStuNum(summaryMapper.getQuizStuNum() + mapper.getQuizStuNum());
//            summaryMapper.setQuizStuTime(summaryMapper.getQuizStuTime() + mapper.getQuizStuTime());
        }
        if (researchStaff.getResearchStaffType() == ResearchStaffType.PROVINCE) {
            summaryMapper.setName(researchStaff.getRegion().getProvinceName());
        } else {
            summaryMapper.setName(researchStaff.getRegion().getCityName());
        }
        return summaryMapper;
    }

    private static class SpecifiedTerm {
        public Integer year;
        public Term term;
    }

    private SpecifiedTerm getSpecifiedTermFromRequest() {
        String yearStr = getRequest().getParameter("year");
        String termStr = getRequest().getParameter("term");

        SpecifiedTerm specifiedTerm = new SpecifiedTerm();
        if (StringUtils.isNotEmpty(yearStr)) {
            specifiedTerm.year = Integer.valueOf(yearStr.substring(0, 4));
        }
        if (specifiedTerm.year != null || StringUtils.isNotEmpty(termStr)) {
            specifiedTerm.term = (termStr == null || termStr.equals("1")) ? Term.上学期 : Term.下学期;
        }
        DateRange specifiedDateRange = SchoolYear.newInstance(specifiedTerm.year).getDateRangeByTerm(specifiedTerm.term);
        Date currentDate = new Date();
        if (specifiedTerm.year != null && specifiedDateRange.getEndDate().getTime() > currentDate.getTime()) {
            // 指定的学期参数如果结束日期大于当前日期，说明要么是当前学期，要么是未来学期，直接设为null
            specifiedTerm.year = null;
            specifiedTerm.term = null;
        }
        return specifiedTerm;
    }

    private Map<String, String> getSchoolNameMap(Set<Long> schoolIds) {
        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolIds)
                .getUninterruptibly();
        Map<String, String> schoolNameMap = new HashMap<>();
        for (Map.Entry<Long, School> entry : schoolMap.entrySet()) {
            schoolNameMap.put(entry.getKey().toString(), entry.getValue().getCname());
        }
        return schoolNameMap;
    }

    private Map<String, String> getRegionNameMap(Set<Long> regionCodes) {
        List<Integer> intRegionCodes = new ArrayList<>();
        for (Long regionCode : regionCodes) {
            intRegionCodes.add(regionCode.intValue());
        }
        Map<Integer, ExRegion> regionMap = raikouSystem.getRegionBuffer().loadRegions(intRegionCodes);
        Map<String, String> regionNameMap = new HashMap<>();
        for (Map.Entry<Integer, ExRegion> entry : regionMap.entrySet()) {
            regionNameMap.put(entry.getKey().toString(), entry.getValue().getName());
        }
        return regionNameMap;
    }

    /**
     * 语言技能下载表的表头生成类
     *
     * @author changyuan.liu
     */
    private static class RSSKillDataTiltleGenerator implements POIExcel.TitleGenerator {

        private static String[] typeTitles = new String[]{"听", "说", "读", "写"};
        private static String[] subTitles = new String[]{"总题量", "人均做题", "正确率"};

        private ResearchStaffType researchStaffType;

        private RSSKillDataTiltleGenerator(ResearchStaffType researchStaffType) {
            this.researchStaffType = researchStaffType;
        }

        @Override
        public int generateTitle(Sheet sheet, CellStyle cellStyle, CellStyle headTitleStyle) {
            Row row = sheet.createRow(0);
            int cellInd = 0;
            Cell cell = row.createCell(cellInd++);
            cell.setCellValue("语言技能");
            cell.setCellStyle(cellStyle);

            for (String typeTitle : typeTitles) {
                cell = row.createCell(cellInd);
                cell.setCellValue(typeTitle);
                cell.setCellStyle(cellStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, cellInd, cellInd + 2));
                cellInd += 3;
            }
            cell = row.createCell(cellInd);
            cell.setCellValue("学生数量");
            cell.setCellStyle(cellStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 1, cellInd, cellInd));

            row = sheet.createRow(1);
            cellInd = 0;
            cell = row.createCell(cellInd++);
            if (researchStaffType == ResearchStaffType.PROVINCE || researchStaffType == ResearchStaffType.CITY) {
                cell.setCellValue("区域名称");
            } else {
                cell.setCellValue("学校名称");
            }
            cell.setCellStyle(cellStyle);

            for (int i = 0; i < typeTitles.length; i++) {
                for (String subTitle : subTitles) {
                    cell = row.createCell(cellInd++);
                    cell.setCellValue(subTitle);
                    cell.setCellStyle(cellStyle);
                }
            }

            return 2;
        }
    }

    /**
     * 语言知识下载表的表头生成类
     *
     * @author changyuan.liu
     */
    private static class RSKnowledgeDataTitleGenerator implements POIExcel.TitleGenerator {

        private static String[] typeTitles = new String[]{"词汇", "语法", "话题"};
        private static String[] subTitles = new String[]{"总题量", "人均做题", "正确率"};

        private ResearchStaffType researchStaffType;

        private RSKnowledgeDataTitleGenerator(ResearchStaffType researchStaffType) {
            this.researchStaffType = researchStaffType;
        }

        @Override
        public int generateTitle(Sheet sheet, CellStyle cellStyle, CellStyle headTitleStyle) {
            Row row = sheet.createRow(0);
            int cellInd = 0;
            Cell cell = row.createCell(cellInd++);
            cell.setCellValue("语言知识");
            cell.setCellStyle(cellStyle);

            for (String typeTitle : typeTitles) {
                cell = row.createCell(cellInd);
                cell.setCellValue(typeTitle);
                cell.setCellStyle(cellStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, cellInd, cellInd + 2));
                cellInd += 3;
            }
            cell = row.createCell(cellInd);
            cell.setCellValue("学生数量");
            cell.setCellStyle(cellStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 1, cellInd, cellInd));

            row = sheet.createRow(1);
            cellInd = 0;
            cell = row.createCell(cellInd++);
            if (researchStaffType == ResearchStaffType.PROVINCE || researchStaffType == ResearchStaffType.CITY) {
                cell.setCellValue("区域名称");
            } else {
                cell.setCellValue("学校名称");
            }
            cell.setCellStyle(cellStyle);

            for (int i = 0; i < typeTitles.length; i++) {
                for (String subTitle : subTitles) {
                    cell = row.createCell(cellInd++);
                    cell.setCellValue(subTitle);
                    cell.setCellStyle(cellStyle);
                }
            }

            return 2;
        }
    }

    /**
     * 题型数据下载表的表头生成类
     *
     * @author changyuan.liu
     */
    private static class RSPatternDataTitleGenerator implements POIExcel.TitleGenerator {

        String[] subTitles = new String[]{"做题总量", "正确率"};
        Collection<String> patterns;
        ResearchStaffType researchStaffType;

        public RSPatternDataTitleGenerator(Collection<String> patterns, ResearchStaffType researchStaffType) {
            this.patterns = patterns;
            this.researchStaffType = researchStaffType;
        }

        @Override
        public int generateTitle(Sheet sheet, CellStyle cellStyle, CellStyle headTitleStyle) {
            if (patterns == null) {
                return 0;
            }

            Row row = sheet.createRow(0);
            int cellInd = 0;
            Cell cell = row.createCell(cellInd);
            if (researchStaffType == ResearchStaffType.PROVINCE || researchStaffType == ResearchStaffType.CITY) {
                cell.setCellValue("区域名称");
            } else {
                cell.setCellValue("学校名称");
            }
            cell.setCellStyle(cellStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 1, cellInd, cellInd));
            cellInd++;

            for (String pattern : patterns) {
                cell = row.createCell(cellInd);
                cell.setCellValue(pattern);
                cell.setCellStyle(cellStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, cellInd, cellInd + 1));
                cellInd += 2;
            }

            row = sheet.createRow(1);
            cellInd = 1;
            for (int i = 0; i < patterns.size(); i++) {
                for (String subTitle : subTitles) {
                    cell = row.createCell(cellInd++);
                    cell.setCellValue(subTitle);
                    cell.setCellStyle(cellStyle);
                }
            }

            return 2;
        }
    }

    /**
     * 行为数据下载表的表头生成类
     *
     * @author changyuan.liu
     */
    private static class RSBehaviorDataTitleGenerator implements POIExcel.TitleGenerator {

        private Subject subject;
        private ResearchStaffType researchStaffType;

        RSBehaviorDataTitleGenerator(Subject subject, ResearchStaffType researchStaffType) {
            this.subject = subject;
            this.researchStaffType = researchStaffType;
        }

        @Override
        public int generateTitle(Sheet sheet, CellStyle cellStyle, CellStyle headTitleStyle) {
            if (subject == Subject.ENGLISH || subject == Subject.MATH) {
                Row row = sheet.createRow(0);
                int cellInd = 0;
                Cell cell = row.createCell(cellInd);
                if (researchStaffType == ResearchStaffType.PROVINCE) {
                    cell.setCellValue("市");
                } else {
                    cell.setCellValue("区");
                }
                cell.setCellStyle(cellStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 2, cellInd, cellInd));

                cell = row.createCell(++cellInd);
                if (researchStaffType != ResearchStaffType.PROVINCE) {
                    cell.setCellValue("学校");
                } else {
                    cell.setCellValue("区");
                }
                cell.setCellStyle(cellStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 2, cellInd, cellInd));

                cell = row.createCell(++cellInd);
                cell.setCellValue("认证老师");
                cell.setCellStyle(cellStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, cellInd, cellInd + 3));
                cellInd += 4;

                cell = row.createCell(cellInd);
                cell.setCellValue("认证学生");
                cell.setCellStyle(cellStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, cellInd, cellInd + 3));

                row = sheet.createRow(1);
                cellInd = 2;
                for (int i = 0; i < 2; i++) {
                    cell = row.createCell(cellInd);
                    cell.setCellValue("作业");
                    cell.setCellStyle(cellStyle);
                    sheet.addMergedRegion(new CellRangeAddress(1, 1, cellInd, cellInd + 1));
                    cellInd += 2;

                    cell = row.createCell(cellInd);
                    cell.setCellValue("统考");
                    cell.setCellStyle(cellStyle);
                    sheet.addMergedRegion(new CellRangeAddress(1, 1, cellInd, cellInd + 1));
                    cellInd += 2;
                }

                row = sheet.createRow(2);
                cellInd = 2;
                for (int i = 0; i < 4; i++, cellInd++) {
                    cell = row.createCell(cellInd);
                    cell.setCellValue("人数");
                    cell.setCellStyle(cellStyle);
                    cell = row.createCell(++cellInd);
                    cell.setCellValue("人次");
                    cell.setCellStyle(cellStyle);
                }

                return 3;
            } else {
                Row row = sheet.createRow(0);
                int cellInd = 0;
                Cell cell = row.createCell(cellInd);
                if (researchStaffType == ResearchStaffType.PROVINCE) {
                    cell.setCellValue("市");
                } else {
                    cell.setCellValue("区");
                }
                cell.setCellStyle(cellStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 2, cellInd, cellInd));

                cell = row.createCell(++cellInd);
                cell.setCellValue("学校");
                cell.setCellStyle(cellStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 2, cellInd, cellInd));

                cell = row.createCell(++cellInd);
                cell.setCellValue("认证老师");
                cell.setCellStyle(cellStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, cellInd, cellInd + 1));
                cellInd += 2;

                cell = row.createCell(cellInd);
                cell.setCellValue("认证学生");
                cell.setCellStyle(cellStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, cellInd, cellInd + 1));

                row = sheet.createRow(1);
                cellInd = 2;
                cell = row.createCell(cellInd);
                cell.setCellValue("作业");
                cell.setCellStyle(cellStyle);
                sheet.addMergedRegion(new CellRangeAddress(1, 1, cellInd, cellInd + 1));
                cellInd += 2;

                cell = row.createCell(cellInd);
                cell.setCellValue("作业");
                cell.setCellStyle(cellStyle);
                sheet.addMergedRegion(new CellRangeAddress(1, 1, cellInd, cellInd + 1));

                row = sheet.createRow(2);
                cellInd = 2;
                for (int i = 0; i < 2; i++, cellInd++) {
                    cell = row.createCell(cellInd);
                    cell.setCellValue("人数");
                    cell.setCellStyle(cellStyle);
                    cell = row.createCell(++cellInd);
                    cell.setCellValue("人次");
                    cell.setCellStyle(cellStyle);
                }

                return 3;
            }
        }
    }

    /**
     * 生成语言技能报告下载表的内容部分
     */
    private List<Object[]> generateRSSkillDataContentForExcel(ResearchStaffSkillMapper researchStaffSkillMapper) {
        List<Object[]> content = new LinkedList<>();
        if (researchStaffSkillMapper == null || researchStaffSkillMapper.getSkillUnitList() == null) {
            return content;
        }
        for (ResearchStaffSkillUnitMapper mapper : researchStaffSkillMapper.getSkillUnitList()) {
            Object[] row = new Object[14];
            int ind = 0;
            row[ind++] = mapper.getName();
            if (mapper.getListening() != null) {
                row[ind++] = mapper.getListening().getFinishCount();
                row[ind++] = mapper.getListening().getCountPerStudent();
                row[ind++] = formatRate(mapper.getListening().getCorrectRate());
            } else {
                ind += 3;
            }
            if (mapper.getSpeaking() != null) {
                row[ind++] = mapper.getSpeaking().getFinishCount();
                row[ind++] = mapper.getSpeaking().getCountPerStudent();
                row[ind++] = formatRate(mapper.getSpeaking().getCorrectRate());
            } else {
                ind += 3;
            }
            if (mapper.getReading() != null) {
                row[ind++] = mapper.getReading().getFinishCount();
                row[ind++] = mapper.getReading().getCountPerStudent();
                row[ind++] = formatRate(mapper.getReading().getCorrectRate());
            } else {
                ind += 3;
            }
            if (mapper.getWritten() != null) {
                row[ind++] = mapper.getWritten().getFinishCount();
                row[ind++] = mapper.getWritten().getCountPerStudent();
                row[ind++] = formatRate(mapper.getWritten().getCorrectRate());
            } else {
                ind += 3;
            }
            row[ind++] = mapper.getStudentCount();
            content.add(row);
        }
        return content;
    }

    /**
     * 生成语言知识报告下载表的内容部分
     */
    private List<Object[]> generateRSKnowledgeDataContentForExcel(ResearchStaffKnowledgeMapper researchStaffKnowledgeMapper) {
        List<Object[]> content = new LinkedList<>();
        if (researchStaffKnowledgeMapper == null || researchStaffKnowledgeMapper.getKnowledgeUnitList() == null) {
            return content;
        }
        for (ResearchStaffKnowledgeUnitMapper mapper : researchStaffKnowledgeMapper.getKnowledgeUnitList()) {
            Object[] row = new Object[11];
            row[0] = mapper.getName();
            if (mapper.getWordDetail() != null) {
                row[1] = mapper.getWordDetail().getFinishCount();
                row[2] = mapper.getWordDetail().getCountPerStudent();
                row[3] = formatRate(mapper.getWordDetail().getCorrectRate());
            }
            if (mapper.getGramDetail() != null) {
                row[4] = mapper.getGramDetail().getFinishCount();
                row[5] = mapper.getGramDetail().getCountPerStudent();
                row[6] = formatRate(mapper.getGramDetail().getCorrectRate());
            }
            if (mapper.getTopicDetail() != null) {
                row[7] = mapper.getTopicDetail().getFinishCount();
                row[8] = mapper.getTopicDetail().getCountPerStudent();
                row[9] = formatRate(mapper.getTopicDetail().getCorrectRate());
            }
            row[10] = mapper.getStudentCount();
            content.add(row);
        }
        return content;
    }

    /**
     * 生成区/校薄弱报告下载表的内容部分
     */
    private List<Object[]> generateRSWeakPointDataContentForExcel(List<ResearchStaffWeakPointUnitMapper> researchStaffWeakPointUnitMappers) {
        List<Object[]> content = new LinkedList<>();
        if (researchStaffWeakPointUnitMappers == null) {
            return content;
        }
        for (ResearchStaffWeakPointUnitMapper mapper : researchStaffWeakPointUnitMappers) {
            Object[] row = new Object[4];
            row[0] = mapper.getName();
            row[1] = mapper.getWord();
            row[2] = mapper.getGrammar();
            row[3] = mapper.getTopic();
            content.add(row);
        }
        return content;
    }

    /**
     * 生成教材薄弱报告下载表的内容部分
     */
    private List<Object[]> generateRSUnitWeakPointDataContentForExcel(ResearchStaffUnitWeakPointMapper researchStaffUnitWeakPointMapper) {
        List<Object[]> content = new LinkedList<>();
        if (researchStaffUnitWeakPointMapper == null || researchStaffUnitWeakPointMapper.getRegionWeakPointMap() == null) {
            return content;
        }
        boolean setName;
        boolean setLevel;
        for (Map.Entry<String, List<ResearchStaffUnitWeakPointUnitMapper>> entry : researchStaffUnitWeakPointMapper.getRegionWeakPointMap().entrySet()) {
            setName = true;
            List<ResearchStaffUnitWeakPointUnitMapper> unitMappers = entry.getValue();
            if (unitMappers != null) {
                for (ResearchStaffUnitWeakPointUnitMapper unitMapper : unitMappers) {
                    setLevel = true;
                    if (unitMapper.getBookList() != null) {
                        for (ResearchStaffUnitWeakPointBookMapper bookMapper : unitMapper.getBookList()) {
                            Object[] row = new Object[4];
                            if (setName) {
                                row[0] = entry.getKey();
                                setName = false;
                            }
                            if (setLevel) {
                                row[1] = unitMapper.getClazzLevel();
                                setLevel = false;
                            }
                            row[2] = bookMapper.getPress();
                            row[3] = bookMapper.getWeakPoints();
                            content.add(row);
                        }
                    }
                }
            }
        }
        return content;
    }

    /**
     * 生成题型数据报告下载表的内容部分
     */
    private List<Object[]> generateRSPatternDataContentForExcel(ResearchStaffPatternMapper researchStaffPatternMapper) {
        List<Object[]> content = new LinkedList<>();
        if (researchStaffPatternMapper == null || researchStaffPatternMapper.getPatternUnitList() == null) {
            return content;
        }
        for (ResearchStaffPatternUnitMapper unitMapper : researchStaffPatternMapper.getPatternUnitList()) {
            Object[] row = new Object[1 + 2 * researchStaffPatternMapper.getPatternRank().keySet().size()];
            Map<String, ResearchStaffPatternDetailMapper> patternMap = unitMapper.getPatternMap();
            int ind = 0;
            row[ind++] = unitMapper.getName();
            for (String key : researchStaffPatternMapper.getPatternRank().keySet()) {
                if (patternMap.containsKey(key)) {
                    row[ind++] = patternMap.get(key).getFinishCount();
                    row[ind++] = formatRate(patternMap.get(key).getCorrectRate());
                } else {
                    ind += 2;
                }
            }
            content.add(row);
        }
        return content;
    }

    /**
     * 生成行为数据报告下载表的内容部分
     */
    private List<Object[]> generateRSBehaviorContentDataForExcel(ResearchStaff researchStaff, List<ResearchStaffBehaviorDataMapper> researchStaffBehaviorDataMappers, ResearchStaffBehaviorDataMapper summaryMapper) {
        Subject subject = researchStaff.getSubject();
        List<Object[]> content = new LinkedList<>();
        if (subject == Subject.ENGLISH || subject == Subject.MATH) {
            Object[] row = new Object[10];
            int ind = 0;
            row[ind++] = summaryMapper.getName();
            row[ind++] = summaryMapper.getSchoolNum();
            setRowForRSBehaviorDataForEnglish(row, ind, summaryMapper);
            content.add(row);
            for (ResearchStaffBehaviorDataMapper mapper : researchStaffBehaviorDataMappers) {
                row = new Object[10];
                ind = 0;
                row[ind++] = mapper.getName();
                row[ind++] = mapper.getSchoolNum();
                setRowForRSBehaviorDataForEnglish(row, ind, mapper);
                content.add(row);
                for (ResearchStaffBehaviorDataMapper childMapper : mapper.getChildBehaviorData()) {
                    row = new Object[10];
                    ind = 1;
                    row[ind++] = childMapper.getName();
                    setRowForRSBehaviorDataForEnglish(row, ind, childMapper);
                    content.add(row);
                }
            }
        } else {

            if (researchStaff.getResearchStaffType() != ResearchStaffType.COUNTY) {
                Object[] row = {summaryMapper.getName(), summaryMapper.getSchoolNum(), summaryMapper.getHomeworkTeacherNum(), summaryMapper.getHomeworkTeacherTime(), summaryMapper.getHomeworkStuNum(), summaryMapper.getHomeworkStuTime()};
                content.add(row);
            }
            for (ResearchStaffBehaviorDataMapper mapper : researchStaffBehaviorDataMappers) {
                Object[] row = {mapper.getName(), mapper.getSchoolNum(), mapper.getHomeworkTeacherNum(), mapper.getHomeworkTeacherTime(), mapper.getHomeworkStuNum(), mapper.getHomeworkStuTime()};
                content.add(row);
            }
        }
        return content;
    }

    private void setRowForRSBehaviorDataForEnglish(Object[] row, int ind, ResearchStaffBehaviorDataMapper mapper) {
        row[ind++] = mapper.getHomeworkTeacherNum();
        row[ind++] = mapper.getHomeworkTeacherTime();
//        row[ind++] = mapper.getQuizTeacherNum();
//        row[ind++] = mapper.getQuizTeacherTime();
        row[ind++] = mapper.getHomeworkStuNum();
        row[ind++] = mapper.getHomeworkStuTime();
//        row[ind++] = mapper.getQuizStuNum();
//        row[ind] = mapper.getQuizStuTime();
    }

    private static DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private String formatRate(double number) {
        return decimalFormat.format(number) + "%";
    }
}
