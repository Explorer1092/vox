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

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.service.crm.CrmReviewResultService;
import com.voxlearning.utopia.admin.service.crm.CrmSchoolClueService;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.entity.crm.CrmReviewResult;
import com.voxlearning.utopia.entity.crm.CrmSchoolClue;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CrmSchoolReviewController
 *
 * @author song.wang
 * @date 2016/7/5
 */
@Controller
@RequestMapping("/crm/school_review")
public class CrmSchoolReviewController extends CrmAbstractController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    CrmSchoolClueService crmSchoolClueService;
    @Inject
    CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject
    CrmReviewResultService crmReviewResultService;

    @Inject private SchoolExtServiceClient schoolExtServiceClient;

    @RequestMapping(value = "review_manage.vpage")
    public String setReviewSchools(Model model) {
        String schoolIdStr = getRequestString("schoolIds"); // 学校名
        List<Long> schoolIdList = StringUtils.toLongList(schoolIdStr);
        HashSet<Long> schoolIdSet = new HashSet<>(schoolIdList);
        if (CollectionUtils.isEmpty(schoolIdSet)) {
            return "crm/school_review/review_manage";
        }

        List<Map<String, Object>> schoolList = new ArrayList<>();
        for (Long schoolId : schoolIdSet) {
            School school = raikouSystem.loadSchool(schoolId);
            if (school == null) {
                continue;
            }

            Map<String, Object> schoolInfo = new HashMap<>();
            schoolInfo.put("schoolId", school.getId());
            schoolInfo.put("schoolName", school.getCname());
            schoolInfo.put("schoolLevel", SchoolLevel.safeParse(school.getLevel()).getDescription());
            ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
            schoolInfo.put("schoolRegion", StringUtils.join(region.getProvinceName(), region.getCityName(), region.getCountyName()));

            SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                    .loadSchoolExtInfo(school.getId())
                    .getUninterruptibly();
            if (schoolExtInfo == null || (MapUtils.isEmpty(schoolExtInfo.getGradeStudentCount()) && schoolExtInfo.getEduSystem() == null && schoolExtInfo.getExternOrBoarder() == null && schoolExtInfo.getEnglishStartGrade() == null)) { //没有线索，或者线索不全
                schoolInfo.put("tip", "信息不全");
            } else {
                // 不是抽审对象，则设置成抽审对象
                if (schoolExtInfo.getIsReviewObj() == null || !schoolExtInfo.getIsReviewObj()) {
                    schoolExtInfo.setIsReviewObj(true);
                    schoolExtInfo.setBeReviewObjTime(new Date());
                    schoolExtInfo.setReviewStatus(1);
                    schoolExtServiceClient.getSchoolExtService()
                            .upsertSchoolExtInfo(schoolExtInfo)
                            .awaitUninterruptibly();
                }
                schoolInfo.put("reviewStatus", schoolExtInfo.getReviewStatus());

                CrmSchoolClue crmSchoolClue = crmSchoolClueService.findLastestAuthedSchoolClue(schoolId);
                if (crmSchoolClue != null) {
                    schoolInfo.put("recorderId", crmSchoolClue.getRecorderId());
                    schoolInfo.put("recorderName", crmSchoolClue.getRecorderName());
                    schoolInfo.put("recorderPhone", crmSchoolClue.getRecorderPhone());
                    schoolInfo.put("authedTime", formatDate(crmSchoolClue.getUpdateTime(), DATE_FORMAT));
                }
            }
            schoolList.add(schoolInfo);
        }
        model.addAttribute("schoolList", schoolList);
        return "crm/school_review/review_manage";
    }

    @RequestMapping(value = "review_list.vpage")
    public String reviewList(Model model) {

        AuthCurrentAdminUser user = getCurrentAdminUser();

        List<Integer> reviewStatusList = new ArrayList<>();
        reviewStatusList.add(SchoolExtInfo.REVIEW_STATUS_TODO);
        reviewStatusList.add(SchoolExtInfo.REVIEW_STATUS_DOING);
        Pageable pageable = buildSortPageRequest(50, "beReviewObjTime");
        Page<SchoolExtInfo> dataPage = schoolExtServiceClient.getSchoolExtService()
                .findSchoolExtInfoListByReviewStatusList(reviewStatusList, pageable)
                .getUninterruptibly();
        if (dataPage == null || CollectionUtils.isEmpty(dataPage.getContent())) {
            model.addAttribute("pager", dataPage);
            model.addAttribute("schoolList", new ArrayList<Map<String, Object>>());
            return "crm/school_review/review_list";
        }

        model.addAttribute("pager", dataPage);
        List<Map<String, Object>> schoolList = new ArrayList<>();
        for (SchoolExtInfo extInfo : dataPage.getContent()) {
            Map<String, Object> schoolInfo = new HashMap<>();
            School school = raikouSystem.loadSchool(extInfo.getId());
            if (school == null) {
                continue;
            }
            schoolInfo.put("schoolId", school.getId());
            schoolInfo.put("schoolName", school.getCname());
            schoolInfo.put("schoolLevel", SchoolLevel.safeParse(school.getLevel()).getDescription());
            ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
            schoolInfo.put("schoolRegion", StringUtils.join(region.getProvinceName(), region.getCityName(), region.getCountyName()));

            schoolInfo.put("reviewStatus", extInfo.getReviewStatus());

            CrmSchoolClue crmSchoolClue = crmSchoolClueService.findLastestAuthedSchoolClue(school.getId());
            if (crmSchoolClue != null) {
                schoolInfo.put("recorderId", crmSchoolClue.getRecorderId());
                schoolInfo.put("recorderName", crmSchoolClue.getRecorderName());
                schoolInfo.put("recorderPhone", crmSchoolClue.getRecorderPhone());
                schoolInfo.put("authedTime", formatDate(crmSchoolClue.getUpdateTime(), DATE_FORMAT));
            }

            schoolInfo.put("reviewUser", extInfo.getReviewUser());
            schoolInfo.put("canReview", true);
            if (Objects.equals(extInfo.getReviewStatus(), SchoolExtInfo.REVIEW_STATUS_DOING)) {
                if (user.getAdminUserName().equals(extInfo.getReviewUser())) {
                    schoolList.add(0, schoolInfo);
                } else {
                    schoolInfo.put("canReview", false);
                    schoolList.add(schoolInfo);
                }
            } else {
                schoolList.add(schoolInfo);
            }
        }
        model.addAttribute("schoolList", schoolList);
        return "crm/school_review/review_list";
    }

    @RequestMapping(value = "review_detail.vpage")
    public String reviewDetail(Model model) {

        AuthCurrentAdminUser user = getCurrentAdminUser();
        Long schoolId = getRequestLong("schoolId"); // 学校名

        School school = raikouSystem.loadSchool(schoolId);
        model.addAttribute("school", school);

        if (school != null) {
            ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
            model.addAttribute("schoolRegion", region);
            SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                    .loadSchoolExtInfo(school.getId())
                    .getUninterruptibly();
            if (schoolExtInfo != null) {
                if (Objects.equals(schoolExtInfo.getReviewStatus(), SchoolExtInfo.REVIEW_STATUS_TODO)) {
                    schoolExtInfo.setReviewUser(user.getAdminUserName());
                    schoolExtInfo.setReviewStatus(SchoolExtInfo.REVIEW_STATUS_DOING);
                    schoolExtServiceClient.getSchoolExtService()
                            .upsertSchoolExtInfo(schoolExtInfo)
                            .awaitUninterruptibly();
                    model.addAttribute("canReview", true);
                } else if (Objects.equals(schoolExtInfo.getReviewStatus(), SchoolExtInfo.REVIEW_STATUS_DONE)) {
                    model.addAttribute("canReview", false);
                } else if (Objects.equals(schoolExtInfo.getReviewStatus(), SchoolExtInfo.REVIEW_STATUS_DOING)) {
                    if (!Objects.equals(schoolExtInfo.getReviewUser(), user.getAdminUserName())) {
                        model.addAttribute("canReview", false);
                    } else {
                        model.addAttribute("canReview", true);
                    }
                }
                if (StringUtils.isNotBlank(schoolExtInfo.getPhotoUrl())) {
                    Map<String, String> photoMeta = crmSchoolClueService.photoMeta(schoolExtInfo.getPhotoUrl());
                    model.addAttribute("photoMeta", photoMeta);
                }
            }
            model.addAttribute("schoolExtInfo", schoolExtInfo);
        }

        List<CrmTeacherSummary> teacherSummaryList = crmSummaryLoaderClient.loadSchoolTeachers(schoolId);
        teacherSummaryList = teacherSummaryList.stream().filter(p -> p.getAuthStatus() != null && AuthenticationState.valueOf(p.getAuthStatus()) == AuthenticationState.SUCCESS && !p.getFakeTeacher() && StringUtils.isNotBlank(p.getSensitiveMobile())).collect(Collectors.toList());

        Collections.sort(teacherSummaryList, (o1, o2) -> {
            if (o1.getSubject().equals(o2.getSubject())) {
                return 0;
            } else if (Subject.ENGLISH.name().equals(o1.getSubject())) {
                return -1;
            } else if (Subject.MATH.name().equals(o1.getSubject())) {
                if (Subject.ENGLISH.name().equals(o2.getSubject())) {
                    return 1;
                }
                return -1;
            }
            return 1;
        });
        if (CollectionUtils.isNotEmpty(teacherSummaryList) && teacherSummaryList.size() > 5) {
            teacherSummaryList = teacherSummaryList.subList(0, 5);
        }
        if (CollectionUtils.isNotEmpty(teacherSummaryList)) {
            teacherSummaryList.forEach(p -> {
                String phone = sensitiveUserDataServiceClient.showUserMobile(p.getTeacherId(), "crm", SafeConverter.toString(p.getTeacherId()));
                p.setSensitiveMobile(phone);
            });
        }

        model.addAttribute("teacherList", teacherSummaryList);

        return "crm/school_review/review_detail";
    }


    @RequestMapping(value = "cancalReview.vpage")
    @ResponseBody
    public MapMessage cancalReview() {
        AuthCurrentAdminUser user = getCurrentAdminUser();
        Long schoolId = getRequestLong("schoolId"); // 学校名
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();
        if (schoolExtInfo != null) {
            if (Objects.equals(schoolExtInfo.getReviewUser(), user.getAdminUserName())) {
                schoolExtInfo.setReviewUser("");
                schoolExtInfo.setReviewStatus(SchoolExtInfo.REVIEW_STATUS_TODO);
                schoolExtServiceClient.getSchoolExtService()
                        .upsertSchoolExtInfo(schoolExtInfo)
                        .awaitUninterruptibly();
            }
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "doReview.vpage")
    @ResponseBody
    public MapMessage doReview(Model model) {

        AuthCurrentAdminUser user = getCurrentAdminUser();

        Long schoolId = getRequestLong("schoolId"); // 学校名
        School school = raikouSystem.loadSchool(schoolId);
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();
        if (school == null || schoolExtInfo == null) {
            return MapMessage.errorMessage("该学校不存在");
        }
        boolean notDial = getRequestBool("notDial"); // 学校名
        if (notDial) {
            schoolExtInfo.setReviewStatus(SchoolExtInfo.REVIEW_STATUS_DONE);
            schoolExtInfo.setReviewResult(-1);
            schoolExtServiceClient.getSchoolExtService()
                    .upsertSchoolExtInfo(schoolExtInfo)
                    .awaitUninterruptibly();
            return MapMessage.successMessage();
        }


        CrmReviewResult crmReviewResult = new CrmReviewResult();
        crmReviewResult.setSchoolId(school.getId());
        crmReviewResult.setSchoolName(school.getCname());
        crmReviewResult.setReviewUser(user.getAdminUserName());
        crmReviewResult.setReviewUserName(user.getRealName());

        crmReviewResult.setGradeDistributionFlag(getRequestBool("gradeDistributionFlag"));
        crmReviewResult.setSchoolingLengthFlag(getRequestBool("schoolingLengthFlag"));
        crmReviewResult.setExternOrBoarderFlag(getRequestBool("externOrBoarderFlag"));
        crmReviewResult.setEnglishStartGradeFlag(getRequestBool("englishStartGradeFlag"));
        crmReviewResult.setSchoolSizeFlag(getRequestBool("schoolSizeFlag"));
        crmReviewResult.setBranchSchoolIdsFlag(getRequestBool("branchSchoolIdsFlag"));


        CrmReviewResult.CrmReviewResultDetail crmReviewResultDetail1 = new CrmReviewResult.CrmReviewResultDetail();
        CrmReviewResult.CrmReviewResultDetail crmReviewResultDetail2 = new CrmReviewResult.CrmReviewResultDetail();

        boolean useDetail1 = false;
        boolean useDetail2 = false;
        if (!crmReviewResult.getGradeDistributionFlag()) {
            String gradeDistribution1 = getRequestString("gradeDistribution1");
            String gradeDistribution2 = getRequestString("gradeDistribution2");
            crmReviewResultDetail1.setGradeDistribution(gradeDistribution1);
            crmReviewResultDetail2.setGradeDistribution(gradeDistribution2);
            if (StringUtils.isNotBlank(gradeDistribution1)) {
                useDetail1 = true;
            }
            if (StringUtils.isNotBlank(gradeDistribution2)) {
                useDetail2 = true;
            }

          /*  if (!gradeDistribution1.equals(schoolExtInfo.getGradeDistribution()) && gradeDistribution1.equals(gradeDistribution2)) {
                schoolExtInfo.setGradeDistribution(gradeDistribution1);
            }*/
        }
        if (!crmReviewResult.getSchoolingLengthFlag()) {
            Integer schoolLength1 = requestInteger("schoolingLength1");
            Integer schoolLength2 = requestInteger("schoolingLength2");
            crmReviewResultDetail1.setSchoolingLength(schoolLength1);
            crmReviewResultDetail2.setSchoolingLength(schoolLength2);
            if (schoolLength1 != null) {
                useDetail1 = true;
            }
            if (schoolLength2 != null) {
                useDetail2 = true;
            }

            EduSystemType schoolEduSystemType1 = getEduSystemBySchoolLength(schoolLength1, SchoolLevel.safeParse(school.getLevel()));
            if (Objects.equals(schoolLength1, schoolLength2) && schoolEduSystemType1 != null && !Objects.equals(schoolEduSystemType1.name(), schoolExtInfo.getEduSystem())) {
                schoolExtInfo.setEduSystem(schoolEduSystemType1.name());
            }
        }

        if (!crmReviewResult.getEnglishStartGradeFlag()) {
            Integer englishStartGrade1 = requestInteger("englishStartGrade1");
            Integer englishStartGrade2 = requestInteger("englishStartGrade2");
            crmReviewResultDetail1.setEnglishStartGrade(englishStartGrade1);
            crmReviewResultDetail2.setEnglishStartGrade(englishStartGrade2);
            if (englishStartGrade1 != null) {
                useDetail1 = true;
            }
            if (englishStartGrade2 != null) {
                useDetail2 = true;
            }

            if (Objects.equals(englishStartGrade1, englishStartGrade2) && !Objects.equals(englishStartGrade1, schoolExtInfo.getEnglishStartGrade())) {
                schoolExtInfo.setEnglishStartGrade(englishStartGrade1);
            }
        }

        if (!crmReviewResult.getExternOrBoarderFlag()) {
            Integer externOrBoarder1 = requestInteger("externOrBoarder1");
            Integer externOrBoarder2 = requestInteger("externOrBoarder2");
            crmReviewResultDetail1.setExternOrBoarder(externOrBoarder1);
            crmReviewResultDetail2.setExternOrBoarder(externOrBoarder2);
            if (externOrBoarder1 != null) {
                useDetail1 = true;
            }
            if (externOrBoarder2 != null) {
                useDetail2 = true;
            }

            if (Objects.equals(externOrBoarder1, externOrBoarder2) && !Objects.equals(externOrBoarder1, schoolExtInfo.getExternOrBoarder())) {
                schoolExtInfo.setExternOrBoarder(externOrBoarder1);
            }
        }

        if (!crmReviewResult.getSchoolSizeFlag()) {
            Integer schoolSize1 = requestInteger("schoolSize1");
            Integer schoolSize2 = requestInteger("schoolSize2");
            crmReviewResultDetail1.setSchoolSize(schoolSize1);
            crmReviewResultDetail2.setSchoolSize(schoolSize2);

            Integer gradeClassCount1 = requestInteger("gradeClassCount1");
            Integer gradeClassCount2 = requestInteger("gradeClassCount2");
            crmReviewResultDetail1.setGradeClassCount(gradeClassCount1);
            crmReviewResultDetail2.setGradeClassCount(gradeClassCount2);

            Integer classStudentCount1 = requestInteger("classStudentCount1");
            Integer classStudentCount2 = requestInteger("classStudentCount2");

            crmReviewResultDetail1.setClassStudentCount(classStudentCount1);
            crmReviewResultDetail2.setClassStudentCount(classStudentCount2);

            if (schoolSize1 != null || gradeClassCount1 != null || classStudentCount1 != null) {
                useDetail1 = true;
            }
            if (schoolSize2 != null || gradeClassCount2 != null || classStudentCount2 != null) {
                useDetail2 = true;
            }

            if (schoolSize1 != null && schoolSize2 != null && Objects.equals(schoolSize1, schoolSize2) && schoolSize1 > 100) {
                schoolExtInfo.setSchoolSize(schoolSize1);
            }
        }

        if (!crmReviewResult.getBranchSchoolIdsFlag()) {
            Set<Long> branchSchoolIds1 = requestLongSet("branchSchoolIds1");
            Set<Long> branchSchoolIds2 = requestLongSet("branchSchoolIds2");
            crmReviewResultDetail1.setBranchSchoolIds(branchSchoolIds1);
            crmReviewResultDetail2.setBranchSchoolIds(branchSchoolIds2);
            if (CollectionUtils.isNotEmpty(branchSchoolIds1)) {
                useDetail1 = true;
            }
            if (CollectionUtils.isNotEmpty(branchSchoolIds2)) {
                useDetail2 = true;
            }
        }


        if (useDetail1 || useDetail2) {
            List<CrmReviewResult.CrmReviewResultDetail> resultDetailList = new ArrayList<>();
            if (useDetail1) {
                resultDetailList.add(crmReviewResultDetail1);
            }
            if (useDetail2) {
                resultDetailList.add(crmReviewResultDetail2);
            }
            crmReviewResult.setResultDetailList(resultDetailList);
        }

        crmReviewResultService.saveReviewResult(crmReviewResult);

        schoolExtInfo.setReviewStatus(SchoolExtInfo.REVIEW_STATUS_DONE);
        schoolExtServiceClient.getSchoolExtService()
                .upsertSchoolExtInfo(schoolExtInfo)
                .awaitUninterruptibly();


        return MapMessage.successMessage();
    }

    private EduSystemType getEduSystemBySchoolLength(Integer schoollength, SchoolLevel schoolLevel) {
        if (schoollength == 1) {
            return EduSystemType.P5;
        }
        if (schoollength == 2) {
            return EduSystemType.P6;
        }
        if (schoollength == 3 && schoolLevel == SchoolLevel.HIGH) {
            return EduSystemType.S3;
        } else if (schoollength == 3 && schoolLevel == SchoolLevel.MIDDLE) {
            return EduSystemType.J3;
        }
        if (schoollength == 4 && schoolLevel == SchoolLevel.HIGH) {
            return EduSystemType.S4;
        } else if (schoollength == 4 && schoolLevel == SchoolLevel.MIDDLE) {
            return EduSystemType.J4;
        }
        return null;
    }


    @RequestMapping(value = "review_result.vpage")
    public String viewResult(Model model) {

        AuthCurrentAdminUser user = getCurrentAdminUser();
        Long schoolId = getRequestLong("schoolId"); // 学校名

        School school = raikouSystem.loadSchool(schoolId);
        model.addAttribute("school", school);
        if (school != null) {
            ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
            model.addAttribute("schoolRegion", region);
            SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                    .loadSchoolExtInfo(school.getId())
                    .getUninterruptibly();
            if (schoolExtInfo != null && StringUtils.isNotBlank(schoolExtInfo.getPhotoUrl())) {
                Map<String, String> photoMeta = crmSchoolClueService.photoMeta(schoolExtInfo.getPhotoUrl());
                model.addAttribute("photoMeta", photoMeta);
            }
            model.addAttribute("schoolExtInfo", schoolExtInfo);
        }
        CrmReviewResult reviewResult = crmReviewResultService.getReviewResult(schoolId);

        model.addAttribute("reviewResult", reviewResult);

        return "crm/school_review/review_result";
    }

    /**
     * 导出评审结果
     */
    @RequestMapping(value = "export_review_result.vpage", method = RequestMethod.POST)
    public void exportReviewResult() {
        String schoolIdStr = getRequestString("schoolIds"); // 学校名
        List<Long> schoolIdList = StringUtils.toLongList(schoolIdStr);
        try {
            XSSFWorkbook workbook = crmReviewResultService.exportReviewResult(schoolIdList);
            if (workbook == null) {
                logger.error("export review result - Null workbook for schoolIds = {}", schoolIdStr);
                return;
            }
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            out.flush();
            String fileName = "评审结果" + DateUtils.dateToString(new Date()) + ".xlsx";
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception ex) {
            logger.error("export review result  - Excp : {}; schoolIds = {}", ex, schoolIdStr);
        }
    }
}
