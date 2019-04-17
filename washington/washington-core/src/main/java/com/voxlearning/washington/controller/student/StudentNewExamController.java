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

package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.question.api.constant.NewExamType;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by tanguohong on 2016/3/8.
 */
@Controller
@RequestMapping("/student/newexam")
public class StudentNewExamController extends AbstractController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject protected GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject protected DeprecatedGroupLoaderClient groupLoaderClient;

    // 地区(模考)考试列表
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage list() {
        StudentDetail studentDetail = currentStudentDetail();
        String from = getRequestString("from");
        boolean fromPc = "pc".equalsIgnoreCase(from);
        boolean filterOld = getRequestBool("filterOld");
        boolean independent = getRequestBool("independent");
        if (studentDetail != null && studentDetail.getClazz() != null) {
            Clazz clazz = studentDetail.getClazz();
            School school = schoolLoaderClient.getSchoolLoader()
                    .loadSchool(clazz.getSchoolId())
                    .getUninterruptibly();
            if (school != null && school.getRegionCode() != null) {
                ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());
                MapMessage mapMessage = newExamServiceClient.loadAllExams(studentDetail, school, exRegion);
                if (!mapMessage.isSuccess()) {
                    return mapMessage;
                }
                //來自PC
                if (fromPc) {
                    return mapMessage;
                } else {
                    //是否过滤老模块数据
                    if (mapMessage.containsKey("newExamList")) {
                        List<Map<String, Object>> exams = (List<Map<String, Object>>) mapMessage.get("newExamList");
                        if (filterOld) {
                            exams = exams.stream()
                                    .filter(o -> !SafeConverter.toBoolean(o.get("oldNewExam")))
                                    .filter(o -> {
                                        //是不是自主考试
                                        boolean d = Objects.equals(SafeConverter.toString(o.get("examType")), NewExamType.independent.name());
                                        if (independent) {
                                            return d;
                                        } else {
                                            return !d;
                                        }
                                    })
                                    .collect(Collectors.toList());
                            mapMessage.put("newExamList", exams);
                            return mapMessage;
                        } else {
                            exams = exams.stream()
                                    .filter(o -> SafeConverter.toBoolean(o.get("oldNewExam")))
                                    .collect(Collectors.toList());
                            mapMessage.put("newExamList", exams);
                            return mapMessage;
                        }
                    } else {
                        return MapMessage.errorMessage("系统异常，请稍候重试");
                    }
                }
            }
        } else {
            return MapMessage.successMessage().add("newExamList", Collections.emptyList());
        }
        return MapMessage.errorMessage("系统异常，请稍候重试");
    }

    /**
     * 作业记录
     * 单元检测列表
     */
    @RequestMapping(value = "unit/test/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage unitTestList() {
        StudentDetail studentDetail = currentStudentDetail();
        return newExamServiceClient.loadStudentUnitTestHistoryList(studentDetail);
    }

    /**
     * 首页卡片点击进去
     * 单元检测列表
     */
    @RequestMapping(value = "unit/test/index/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage unitTestIndexList() {
        StudentDetail studentDetail = currentStudentDetail();
        return newExamServiceClient.loadStudentIndexUnitTestList(studentDetail);
    }

    /**
     * 学生报名页面
     */
    @RequestMapping(value = "apply.vpage", method = RequestMethod.GET)
    public String goApply(@RequestParam String id, Model model) {
        NewExam newExam = newExamLoaderClient.load(id);
        if (newExam == null) {
            logger.error("Invalid newExam id,id:{}", id);
            return "redirect:/student/index.vpage";
        } else {
            model.addAttribute("id", id);
            model.addAttribute("name", newExam.getName());
            model.addAttribute("remainTime", (newExam.getApplyStopAt().getTime() - System.currentTimeMillis() - 1000) / 1000);
        }
        return "studentv3/newexam/apply";
    }

    @RequestMapping(value = "begin.vpage", method = RequestMethod.GET)
    public String beginNewExam(@RequestParam String id, Model model) {
        NewExam newExam = newExamLoaderClient.load(id);
        if (newExam == null) {
            logger.error("Invalid newExam id,id:{}", id);
            return "redirect:/student/index.vpage";
        }
        model.addAttribute("id", id);
        if (StringUtils.isNoneBlank(newExam.getPaperId())) {
            return "studentv3/newexam/begin";
        } else {
            Subject subject = newExam.getSubject();
            if (newExam.getSchoolLevel() == SchoolLevel.JUNIOR && isNewModelExam(newExam)) {
                //跳到新模考V3.0的跳题
                return "studentv3/newexamv3/begin";
            } else {
                //跳到新模考V2.0的跳题
                return "studentv3/newexamv2/begin";
            }
        }
    }

    /**
     * 移动端查看考试详情
     */
    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage newExamDetail(@RequestParam String id) {
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("考试id不能为空");
        }
        return newExamServiceClient.loadNewExamDetail(id, currentStudentDetail());
    }

    /**
     * 查看考试结果
     */
    @RequestMapping(value = "result.vpage", method = RequestMethod.GET)
    public String result(@RequestParam String id, Model model) {
        NewExam newExam = newExamLoaderClient.load(id);
        if (newExam == null) {
            logger.error("Invalid newExam id,id:{}", id);
            return "redirect:/student/index.vpage";
        }
        model.addAttribute("id", id);
        if (StringUtils.isNoneBlank(newExam.getPaperId())) {
            return "studentv3/newexam/result";
        } else {
            //跳到新模考V2.0的学生详情页
            String url = UrlUtils.buildUrlQuery("/newexamv2/viewstudent.vpage", MapUtils.m("examId", id, "userId", currentUserId(), "from", "student_history"));
            return "redirect:" + url;
        }

    }

    /**
     *  单元测 --- 试卷考试列表
     * @param model
     * @return
     */
    @RequestMapping(value = "paperlist.vpage", method = RequestMethod.GET)
    public String paperlist(Model model) {
        return "studentv3/newexamv3/paperlist";
    }
}
