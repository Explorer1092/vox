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

package com.voxlearning.washington.controller.teacher;

import com.google.common.collect.Maps;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.newexam.api.context.CorrectNewExamContext;
import com.voxlearning.utopia.service.newexam.consumer.client.GroupExamRegistrationServiceClient;
import com.voxlearning.utopia.service.question.api.constant.NewExamRegionLevel;
import com.voxlearning.utopia.service.question.api.constant.NewExamType;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by tanguohong on 2016/3/16.
 */
@Controller
@RequestMapping("/teacher/newexam")
public class TeacherNewExamController extends AbstractTeacherController {

    @Inject private GroupExamRegistrationServiceClient groupExamRegistrationServiceClient;
    @Inject private EmailServiceClient emailServiceClient;

    @RequestMapping(value = "correct.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage correct(@RequestBody Map<String, Object> correctMap) {
        try {
            CorrectNewExamContext correctNewExamContext = new CorrectNewExamContext();
            correctNewExamContext.setQuestionId(SafeConverter.toString(correctMap.get("questionId")));
            correctNewExamContext.setNewExamId(SafeConverter.toString(correctMap.get("newExamId")));
            correctNewExamContext.setSubId(SafeConverter.toInt(correctMap.get("subId")));
            Map<Long, Double> userScoreMap = JsonUtils.fromJsonToMap(JsonUtils.toJson(correctMap.get("userScoreMap")), Long.class, Double.class);
            if (MapUtils.isEmpty(userScoreMap)) {
                return MapMessage.errorMessage("修改失败，分数不能为空");
            }
            correctNewExamContext.setUserScoreMap(userScoreMap);
            return newExamServiceClient.correctNewExam(correctNewExamContext);
        } catch (Exception e) {
            logger.error(getClass().getName() + e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }


    @RequestMapping(value = "resetscore.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetScore(@RequestBody Map<String, Object> pramMap) {
        try {
            return newExamServiceClient.resetScore(pramMap);
        } catch (Exception e) {
            logger.error(getClass().getName() + e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(value = "registration/result.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchExamRegistrationResult(@RequestParam String newExamId) {
        try {
            TeacherDetail teacherDetail = currentTeacherDetail();
            if (teacherDetail == null) {
                return MapMessage.errorMessage("请重新登录");
            }
            NewExam newExam = newExamLoaderClient.load(newExamId);
            if (newExam == null) {
                logger.error("TeacherNewExamController.fetchExamRegistrationResult--fetch NewExam failed : newExamId {} ,teacherId {}", newExamId, teacherDetail.getId());
                return MapMessage.errorMessage("考试不存在");
            }
            if (!NewExamType.apply.equals(newExam.getExamType())) {
                return MapMessage.errorMessage("当前考试不是报名考试");
            }

            boolean matchRegion = (newExam.getRegionLevel() == NewExamRegionLevel.school && CollectionUtils.isNotEmpty(newExam.getSchoolIds()) && newExam.getSchoolIds().contains(teacherDetail.getTeacherSchoolId()))
                    || newExam.getRegionLevel() == NewExamRegionLevel.nation
                    || (newExam.getRegionLevel() == NewExamRegionLevel.province && CollectionUtils.isNotEmpty(newExam.getRegions()) && newExam.getRegions().stream().anyMatch(region -> Objects.equals(region.getProvinceId(), teacherDetail.getRootRegionCode())))
                    || (newExam.getRegionLevel() == NewExamRegionLevel.city && CollectionUtils.isNotEmpty(newExam.getRegions()) && newExam.getRegions().stream().anyMatch(region -> Objects.equals(region.getCityId(), teacherDetail.getCityCode())))
                    || (newExam.getRegionLevel() == NewExamRegionLevel.country && CollectionUtils.isNotEmpty(newExam.getRegions()) && newExam.getRegions().stream().anyMatch(region -> Objects.equals(region.getRegionId(), teacherDetail.getRegionCode())));

            if (!matchRegion) {
                // 发送邮件通知相关人
                sendEmail(newExamId, teacherDetail);
                return MapMessage.successMessage().add("result", MapUtils.m(
                        "examName", newExam.getName(),
                        "matchRegion", Boolean.FALSE,
                        "canRegister", Boolean.FALSE));
            }
            Long relTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(teacherDetail.getId(), newExam.getSubject());
            return groupExamRegistrationServiceClient.fetchExamRegistrationResult(newExam, relTeacherId);
        } catch (Exception e) {
            logger.error(getClass().getName() + e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    private void sendEmail(@RequestParam String newExamId, TeacherDetail teacherDetail) {
        Map<String, Object> content = Maps.newHashMap();
        content.put("info", StringUtils.join(RuntimeMode.getCurrentStage(), "环境",
                "<br />测评id: ", newExamId,
                "<br />教师id: ", teacherDetail.getId(),
                "<br />教师所处地区: ", teacherDetail.getRootRegionName(), teacherDetail.getCityName(), teacherDetail.getCountyName(), teacherDetail.getTeacherSchoolName(),
                "<br />"));
        emailServiceClient.createTemplateEmail(EmailTemplate.office)
                .to("aiming.wang@17zuoye.com; wei.dai@17zuoye.com; xuyi.tian@17zuoye.com; yu.zhang@17zuoye.com; yanfei.wang@17zuoye.com; yiran.gao@17zuoye.com")
                .subject("报名考试不包含教师所属地区")
                .content(content)
                .send();
    }

    @RequestMapping(value = "register.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage register(@RequestParam String newExamId, @RequestParam String groupIds) {
        try {
            TeacherDetail teacherDetail = currentTeacherDetail();
            if (teacherDetail == null) {
                return MapMessage.errorMessage("请重新登录");
            }
            List<Long> clazzGroupIds = StringUtils.toLongList(groupIds);
            if (CollectionUtils.isEmpty(clazzGroupIds)) {
                return MapMessage.errorMessage("groupIds不能为空");
            }
            NewExam newExam = newExamLoaderClient.load(newExamId);
            if (newExam == null) {
                logger.error("TeacherNewExamController.register--fetch NewExam failed : newExamId {} ,teacherId {}", newExamId, teacherDetail.getId());
                return MapMessage.errorMessage("考试不存在");
            }
            if (!NewExamType.apply.equals(newExam.getExamType())) {
                return MapMessage.errorMessage("当前考试不是报名考试");
            }
            boolean matchRegion = (newExam.getRegionLevel() == NewExamRegionLevel.school && CollectionUtils.isNotEmpty(newExam.getSchoolIds()) && newExam.getSchoolIds().contains(teacherDetail.getTeacherSchoolId()))
                    || newExam.getRegionLevel() == NewExamRegionLevel.nation
                    || (newExam.getRegionLevel() == NewExamRegionLevel.province && CollectionUtils.isNotEmpty(newExam.getRegions()) && newExam.getRegions().stream().anyMatch(region -> Objects.equals(region.getProvinceId(), teacherDetail.getRootRegionCode())))
                    || (newExam.getRegionLevel() == NewExamRegionLevel.city && CollectionUtils.isNotEmpty(newExam.getRegions()) && newExam.getRegions().stream().anyMatch(region -> Objects.equals(region.getCityId(), teacherDetail.getCityCode())))
                    || (newExam.getRegionLevel() == NewExamRegionLevel.country && CollectionUtils.isNotEmpty(newExam.getRegions()) && newExam.getRegions().stream().anyMatch(region -> Objects.equals(region.getRegionId(), teacherDetail.getRegionCode())));

            if (!matchRegion) {
                return MapMessage.errorMessage("很抱歉，本次活动未包含您所在的城市. 请关注下次活动");
            }
            Date currentDate = new Date();
            if (newExam.getApplyStopAt().before(currentDate)) {
                return MapMessage.errorMessage("很遗憾, 活动报名已结束. 请关注下次活动").setErrorCode("E100001");
            }
            return groupExamRegistrationServiceClient.register(teacherDetail, newExam, clazzGroupIds);
        } catch (Exception e) {
            logger.error(getClass().getName() + e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(value = "share.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage shareReport() {
        String newExamId = getRequestString("newExamId");
        Long groupId = getRequestLong("groupId");
        if(StringUtils.isBlank(newExamId) || groupId == null){
            return MapMessage.errorMessage("参数错误").setErrorCode(ErrorCodeConstants.ERROR_CODE_PARAMETER);
        }
        try {
            TeacherDetail teacherDetail = currentTeacherDetail();
            if (teacherDetail == null) {
                return MapMessage.errorMessage("请重新登录");
            }
            return groupExamRegistrationServiceClient.shareReport(teacherDetail, newExamId, groupId);
        } catch (Exception e) {
            logger.error(getClass().getName() + e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(value = "assign.vpage", method = RequestMethod.GET)
    public String assign(Model model){
        model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        Long nowDateTime = new Date().getTime();
        Date tomorrowEndDate = DayRange.newInstance(nowDateTime).next().getEndDate();
        model.addAttribute("currentDate", DateUtils.dateToString(new Date(nowDateTime)));
        //当前时间推后五分钟
        model.addAttribute("startDateTime",DateUtils.dateToString(new Date(nowDateTime + 5 * 60 * 1000)));
        model.addAttribute("endDateTime", DateUtils.dateToString(tomorrowEndDate));
        return "teacherv3/newexamv3/assign";
    }

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String list(Model model){
        model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        return "teacherv3/newexamv3/list";
    }
}
