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

package com.voxlearning.washington.controller.mobile.student;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.helper.GEOUtils;
import com.voxlearning.utopia.service.business.api.SummerReporterService;
import com.voxlearning.utopia.service.business.api.entity.summerreport.StudentCityInterviewCount;
import com.voxlearning.utopia.service.business.api.entity.summerreport.StudentCollectSchool;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by jiangpeng on 16/6/12.
 */

@Controller
@RequestMapping("/studentMobile/summerReporter")
@NoArgsConstructor
@Slf4j
public class MobileStudentSummerReporterController extends AbstractMobileController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = SummerReporterService.class)
    private SummerReporterService summerReporterService;


    @Inject
    protected GrayFunctionManagerClient grayFunctionManagerClient;


    private static Integer cacheExpire = 60 * 60 * 4;

    private static BigDecimal maxDistance = new BigDecimal(0.3);

    private static String cacheKeyPrefix = "SUMMERREPORTER_";

    private static List<String> schoolNameKeyWordList = Arrays.asList("小学,中学,初中,学校,校,中".split(","));

    @RequestMapping(value = "/interview/commit.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage commit() {
        User student = currentUser();
        if (student == null || !student.fetchUserType().equals(UserType.STUDENT))
            return noLoginResult;

        String schoolName = getRequestString("school_name");
        if (StringUtils.isEmpty(schoolName))
            return MapMessage.errorMessage("学校名不能为空");
        if (schoolName.length() > 20)
            return MapMessage.errorMessage("学校名不可超出20个字哦");
        String description = getRequestString("description");
        if (StringUtils.isEmpty(description))
            return MapMessage.errorMessage("学校描述不能为空");
        if (description.length() > 150)
            return MapMessage.errorMessage("学校描述不可超出150个字哦");

        if (badWordCheckerClient.containsConversationBadWord(schoolName)) {
            return MapMessage.errorMessage("提交的内容有敏感词，请重新输入");
        }

        if (!checkSchoolNameKeyWord(schoolName))
            return MapMessage.errorMessage("学校名称异常，请核对后提交哦");

        Double latitude = getRequestDouble("latitude");
        Double longitude = getRequestDouble("longitude");

        Integer provinceId = getRequestInt("provinceId");
        Integer cityId = getRequestInt("cityId");
        Integer countyId = getRequestInt("countyId");

        if (provinceId == 0 || cityId == 0 || countyId == 0)
            return MapMessage.errorMessage("没有省市区信息");

        if (!checkRegion(provinceId, cityId, countyId))
            return MapMessage.errorMessage("区域选择错误,请重新选择区域");

        String pictureUrl = getRequestString("picUrl");
        if (StringUtils.isEmpty(pictureUrl))
            return MapMessage.errorMessage("没有上传图片");

        //通过地理位置判断，已提交>=3个相同位置（一公里之内）的学校，报错"不可以重复采访学校哦 " 需要排除已获取权限且GPS已开但无法取到定位的特殊情况
        //通过学校名称判断，如已提交>=1个完全相同名称的学校，报错"不可以重复采访学校哦"
        //如果已提交数量>=30所学校，报错"采访学校数量已达30上限"
        List<StudentCollectSchool> collectSchoolList = summerReporterService.loadInterviewRecord(student.getId());
        if (!CollectionUtils.isEmpty(collectSchoolList)) {
            if (collectSchoolList.size() >= 30)
                return MapMessage.errorMessage("采访学校数量已达30上限");
            Integer sameLocationSchoolCount = 0;
            for (StudentCollectSchool collectSchool : collectSchoolList) {
                if (schoolName.equals(collectSchool.getSchoolName()))
                    return MapMessage.errorMessage("不可以重复采访学校哦");
                if (collectSchool.getLatitude() != 0 && collectSchool.getLongitude() != 0) {
                    double distance = GEOUtils.getDistance(latitude, longitude, collectSchool.getLatitude(), collectSchool.getLongitude());
                    if (new BigDecimal(distance).compareTo(maxDistance) < 0)
                        sameLocationSchoolCount++;
                }
            }
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(student.getId());
            if (!checkStudentIsInWhiteList(studentDetail) && sameLocationSchoolCount >= 3)
                return MapMessage.errorMessage("不可以重复采访学校哦");
        }

        StudentCollectSchool studentCollectSchool = StudentCollectSchool.newInstance();
        studentCollectSchool.setSchoolName(schoolName);
        studentCollectSchool.setDescription(description);
        studentCollectSchool.setLatitude(latitude);
        studentCollectSchool.setLongitude(longitude);
        studentCollectSchool.setPictureUrl(pictureUrl);
        studentCollectSchool.setStudentId(student.getId());
        studentCollectSchool.setStudentName(student.fetchRealname());
        studentCollectSchool.setProvinceId(provinceId);
        studentCollectSchool.setCityId(cityId);
        studentCollectSchool.setCountyId(countyId);

        try {
            return atomicLockManager.wrapAtomic(summerReporterService)
                    .keyPrefix("COMMIT_INTERVIEW_SCHOOL")
                    .keys(student.getId())
                    .proxy()
                    .uploadInterviewSchool(studentCollectSchool);

        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("正在提交,稍后再试!");
        }
    }

    /**
     * 灰度配置某些学生 不受地理位置限制.
     *
     * @param studentDetail
     * @return
     */
    private boolean checkStudentIsInWhiteList(StudentDetail studentDetail) {
        return grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "summerReporter", "whiteList");
    }

    public static void main(String[] args) {
        System.out.println(new BigDecimal(3.333333).compareTo(new BigDecimal(1.22222)) < 0);
    }

    private boolean checkRegion(Integer provinceId, Integer cityId, Integer countyId) {
        ExRegion province = raikouSystem.loadRegion(provinceId);
        if (province == null || RegionType.PROVINCE != province.fetchRegionType())
            return false;

        ExRegion city = raikouSystem.loadRegion(cityId);
        if (city == null || RegionType.CITY != city.fetchRegionType())
            return false;
        List<ExRegion> children = province.getChildren();
        if (!children.contains(city))
            return false;

        ExRegion county = raikouSystem.loadRegion(countyId);
        if (countyId == null || RegionType.COUNTY != county.fetchRegionType())
            return false;
        List<ExRegion> cityChildren = city.getChildren();
        if (!cityChildren.contains(county))
            return false;

        return true;
    }

    private Boolean checkSchoolNameKeyWord(String schoolName) {
        for (String k : schoolNameKeyWordList) {
            if (schoolName.contains(k))
                return true;
        }
        return false;
    }


    @RequestMapping(value = "/interview/record.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage interviewHistory() {
        User student = currentUser();
        if (student == null || !student.fetchUserType().equals(UserType.STUDENT))
            return noLoginResult;

        List<StudentCollectSchool> collectSchoolList = summerReporterService.loadInterviewRecord(student.getId());

        List<Map<String, Object>> recordMapList = new ArrayList<>();
        if (CollectionUtils.isEmpty(collectSchoolList))
            return MapMessage.successMessage().add("recordList", recordMapList);

        for (StudentCollectSchool collectSchool : collectSchoolList) {
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("schoolName", collectSchool.getSchoolName());
            record.put("pictureUrl", collectSchool.getPictureUrl());
            record.put("description", collectSchool.getDescription());
            recordMapList.add(record);
        }
        return MapMessage.successMessage().add("recordList", recordMapList);

    }

    @RequestMapping(value = "/school/recommend.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage recommendSchool() {
        User student = currentUser();
        if (student == null || !student.fetchUserType().equals(UserType.STUDENT))
            return noLoginResult;
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(student.getId());
        if (studentDetail == null)
            return noLoginResult;
        Integer countyId = studentDetail.getStudentSchoolRegionCode();
        if (countyId == null)
            return MapMessage.errorMessage("学生没有区域信息");
        List<Map<String, Object>> schoolMapList = washingtonCacheSystem.CBS.flushable
                .wrapCache(summerReporterService)
                .expiration(cacheExpire)
                .keyPrefix(cacheKeyPrefix + "recommendSchool")
                .keys(countyId)
                .proxy()
                .loadRecommendSchoolByCounty(countyId);
        return MapMessage.successMessage().add("schoolList", schoolMapList);

    }

    @RequestMapping(value = "/city/ranking.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage cityTopRange() {
        User student = currentUser();
        if (student == null || !student.fetchUserType().equals(UserType.STUDENT))
            return noLoginResult;
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(student.getId());
        if (studentDetail == null)
            return noLoginResult;
        Integer cityId = studentDetail.getCityCode();
        if (cityId == null)
            return MapMessage.errorMessage("学生没有区域信息");
        List<StudentCityInterviewCount> interviewCounts = washingtonCacheSystem.CBS.flushable
                .wrapCache(summerReporterService)
                .expiration(cacheExpire)
                .keyPrefix(cacheKeyPrefix + "cityTopRange")
                .keys(cityId)
                .proxy()
                .loadCityTopRange(cityId);
        ExRegion exRegion = raikouSystem.loadRegion(cityId);
        String cityName = "城市";
        if (exRegion != null) {
            if (exRegion.fetchRegionType() == RegionType.CITY || exRegion.fetchRegionType() == RegionType.COUNTY) {
                if (exRegion.isMunicipalitiy()) {
                    cityName = exRegion.getProvinceName() + "市";
                } else
                    cityName = exRegion.getCityName();
            }
        }

        if (CollectionUtils.isEmpty(interviewCounts))
            return MapMessage.successMessage().add("rankingList", new ArrayList<>()).add("cityName", cityName);
        else
            return MapMessage.successMessage().add("rankingList", interviewCounts).add("cityName", cityName);
    }

    @RequestMapping(value = "/index/data.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage indexData() {
        User student = currentUser();
        if (student == null || !student.fetchUserType().equals(UserType.STUDENT))
            return noLoginResult;

        MapMessage result = MapMessage.successMessage();
        Integer count = summerReporterService.loadInterviewCount(student.getId());
        result.add("interviewCounts", count);


        Integer rank = washingtonCacheSystem.CBS.flushable
                .wrapCache(summerReporterService)
                .expiration(cacheExpire)
                .keyPrefix(cacheKeyPrefix + "studentRankV1")
                .keys(student.getId())
                .proxy()
                .loadStudentCityRank(student.getId());
        if (rank > 0)
            result.add("rank", rank);
        else
            result.add("rank", "暂无");
        return result;

    }

    @RequestMapping(value = "/student/region.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage studentRegion() {
        User student = currentUser();
        if (student == null || !student.fetchUserType().equals(UserType.STUDENT))
            return noLoginResult;
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(student.getId());
        if (studentDetail == null)
            return noLoginResult;
        Integer provinceId = studentDetail.getRootRegionCode();
        Integer cityId = studentDetail.getCityCode();
        Integer countyId = studentDetail.getStudentSchoolRegionCode();

        if (provinceId == null || cityId == null || countyId == null)
            return MapMessage.errorMessage("学生区域不正确");
        return MapMessage.successMessage().add("provinceId", provinceId).add("cityId", cityId).add("countyId", countyId);

    }


}
