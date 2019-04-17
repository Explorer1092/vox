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

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.AbstractAdminController;
import com.voxlearning.utopia.admin.service.crm.CrmRecSchoolService;
import com.voxlearning.utopia.admin.service.crm.CrmUgcStudentResultService;
import com.voxlearning.utopia.entity.crm.CrmRecSchool;
import com.voxlearning.utopia.entity.crm.CrmUgcStudentResult;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by jiang wei on 2016/7/26.
 */

@Controller
@RequestMapping("/crm/summer_reporter")
public class CrmSummerReporterController extends AbstractAdminController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private CrmRecSchoolService crmRecSchoolService;
    @Inject private CrmUgcStudentResultService crmUgcStudentResultService;

    @RequestMapping(value = "school_list.vpage")
    public String SchoolList(Model model) {
        String provinceId = requestString("provinceName");
        if (StringUtils.isBlank(provinceId)) {
            return "crm/reporter_review/school_list";
        }
        String cityId = requestString("cityName");
        String countyId = "";
//        if (StringUtils.equals(provinceId, "110000") || StringUtils.equals(provinceId, "120000") || StringUtils.equals(provinceId, "310000") || StringUtils.equals(provinceId, "500000")) {
//            Integer provinceIdTemp = Integer.valueOf(provinceId);
//            countyId = cityId;
//            cityId = String.valueOf(provinceIdTemp + 100);
//
//
//        }
        if (StringUtils.isBlank(cityId)) {
            model.addAttribute("cityError", "请选择城市！");
            return "crm/reporter_review/school_list";
        }
        String schoolName = requestString("schoolName");
        String status = requestString("status");
        if (StringUtils.isBlank(schoolName)) {
            if (StringUtils.isBlank(countyId)) {
                if (StringUtils.equals(provinceId, "000000") && StringUtils.equals(cityId, "000000")) {
                    List<CrmRecSchool> crmStatusRecSchoolList = crmRecSchoolService.loadSchoolsByStatus(status);
                    List<CrmRecSchool> crmStatusRecSchoolListResult = generateCrmRecSchoolList(crmStatusRecSchoolList);
                    model.addAttribute("crmRecSchoolList", crmStatusRecSchoolListResult);
                    return "crm/reporter_review/school_list";
                }
                List<CrmRecSchool> crmCityAndProvinceRecSchoolList = crmRecSchoolService.loadSchoolsByCityAndProvince(Integer.valueOf(provinceId), Integer.valueOf(cityId), status);
                List<CrmRecSchool> crmCityAndProvinceRecSchoolListResult = generateCrmRecSchoolList(crmCityAndProvinceRecSchoolList);
                model.addAttribute("crmRecSchoolList", crmCityAndProvinceRecSchoolListResult);
                return "crm/reporter_review/school_list";
            }
            List<CrmRecSchool> crmRegionRecSchoolList = crmRecSchoolService.loadSchoolsByRegion(Integer.valueOf(provinceId), Integer.valueOf(cityId), Integer.valueOf(countyId), status);
            List<CrmRecSchool> crmRegionRecSchoolListResult = generateCrmRecSchoolList(crmRegionRecSchoolList);
            model.addAttribute("crmRecSchoolList", crmRegionRecSchoolListResult);
            return "crm/reporter_review/school_list";

        }


        if (StringUtils.isBlank(countyId)) {
            List<CrmRecSchool> crmCityAndNameRecSchoolList = crmRecSchoolService.loadSchoolsByCityAndName(Integer.valueOf(provinceId), Integer.valueOf(cityId), schoolName, status);
            List<CrmRecSchool> crmCityAndNameRecSchoolListResult = generateCrmRecSchoolList(crmCityAndNameRecSchoolList);
            model.addAttribute("crmRecSchoolList", crmCityAndNameRecSchoolListResult);
            return "crm/reporter_review/school_list";
        }
        List<CrmRecSchool> crmRecSchoolList = crmRecSchoolService.loadSchoolsByType(Integer.valueOf(provinceId), Integer.valueOf(cityId), Integer.valueOf(countyId), schoolName, status);
        // List<CrmRecSchool> crmRecSchoolList = crmRecSchoolService.loadSchoolsByTypeTest(provinceId, cityId, schoolName);
        if (CollectionUtils.isEmpty(crmRecSchoolList)) {
            return "crm/reporter_review/school_list";
        }
        List<CrmRecSchool> crmRecSchoolListResult = generateCrmRecSchoolList(crmRecSchoolList);
        model.addAttribute("crmRecSchoolList", crmRecSchoolListResult);
        return "crm/reporter_review/school_list";
    }

    @RequestMapping(value = "review_detail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage ReporterDetail() {
        //AuthCurrentAdminUser user = getCurrentAdminUser();
        Integer id = getRequestInt("schoolId");
        //CrmRecSchool crmRecSchool = crmRecSchoolService.loadSchoolById(id);
        List<CrmUgcStudentResult> crmUgcStudentResultList = crmUgcStudentResultService.loadUgcStudentResultByReleId(id);
        return MapMessage.successMessage().add("crmUgcStudentResultList", crmUgcStudentResultList);
    }

    @RequestMapping(value = "doReview.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage doReview() {
        AuthCurrentAdminUser user = getCurrentAdminUser();
        String passIds = getRequestString("passIds");
        String rejectIds = getRequestString("rejectIds");
        Integer schoolId = getRequestInt("schoolId");
        Integer recSchoolId = getRequestInt("recSchoolId");
        String schoolAddr = getRequestString("schooladdr");
        String schoolName = getRequestString("schoolName");
        Double schoolblat = getRequestDouble("schoolblat", 0);
        Double schoolblon = getRequestDouble("schoolblon", 0);
        Date updateTime = new Date();
        String[] rejectIdsArray = rejectIds.split(",");
        List<String> rejectIdsList = Arrays.asList(rejectIdsArray);
        if (StringUtils.isBlank(passIds)) {
            crmRecSchoolService.passRecSchoolInfo(schoolId, schoolName, schoolAddr, schoolblat, schoolblon, user.getAdminUserName(), updateTime);
            if (StringUtils.isNotBlank(rejectIds)) {
                crmUgcStudentResultService.rejectUgcStudentResult(rejectIdsList);
            }
//                if (updateResult == null){
//                    return MapMessage.errorMessage("审核学校失败，未找到id为:{schoolId}的学校",schoolId);
//                }
            return MapMessage.successMessage("审核学校成功");
        } else {
            String[] passIdsArray = passIds.split(",");
            List<String> passIdsList = Arrays.asList(passIdsArray);
            crmRecSchoolService.passRecSchoolInfo(schoolId, schoolName, schoolAddr, schoolblat, schoolblon, user.getAdminUserName(), updateTime);
            crmUgcStudentResultService.passUgcStudentResult(passIdsList, schoolId);
            if (StringUtils.isNotBlank(rejectIds)) {
                crmUgcStudentResultService.rejectUgcStudentResult(rejectIdsList);
            }
            return MapMessage.successMessage("审核学校成功");
        }
    }

    @RequestMapping(value = "doReject.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage doReject() {
        AuthCurrentAdminUser user = getCurrentAdminUser();
        Integer schoolId = getRequestInt("schoolId");
        String rejectIds = getRequestString("rejectIds");
        Date updateTime = new Date();
        String[] rejectIdsArray = rejectIds.split(",");
        List<String> rejectIdsList = Arrays.asList(rejectIdsArray);
        if (StringUtils.isBlank(rejectIds)) {
            crmRecSchoolService.rejectRecSchoolInfo(schoolId, user.getAdminUserName(), updateTime);
        } else {
            crmRecSchoolService.rejectRecSchoolInfo(schoolId, user.getAdminUserName(), updateTime);
            crmUgcStudentResultService.rejectUgcStudentResult(rejectIdsList);
        }

        return MapMessage.successMessage("驳回学校成功");
    }


    private List<CrmRecSchool> generateCrmRecSchoolList(List<CrmRecSchool> list) {
        for (CrmRecSchool crmRecSchool : list) {
            ExRegion province = raikouSystem.loadRegion(crmRecSchool.getProvinceId());
            if (province != null) {
                crmRecSchool.setProvinceName(province.getProvinceName());
                crmRecSchool.setCityName(province.getCityName());
                crmRecSchool.setCountyName(province.getCountyName());
            } else {
                ExRegion city = raikouSystem.loadRegion(crmRecSchool.getCityId());
                if (city != null) {
                    crmRecSchool.setProvinceName(city.getProvinceName());
                    crmRecSchool.setCityName(city.getCityName());
                    crmRecSchool.setCountyName(city.getCountyName());
                } else {
                    ExRegion county = raikouSystem.loadRegion(crmRecSchool.getCountyId());
                    crmRecSchool.setProvinceName(county.getProvinceName());
                    crmRecSchool.setCityName(county.getCityName());
                    crmRecSchool.setCountyName(county.getCountyName());
                }
            }
        }
        return list;
    }

}
