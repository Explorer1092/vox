/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkQuestionAnswerRequest;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/student/exam")
public class StudentExamController extends AbstractController {

    /***************** 假期作业 ********** 分割线 ***************** 假期作业 *****************/
    /*****************
     * 假期作业 ********** 分割线 ***************** 假期作业
     *****************/

    // 英语假期作业应试
    @RequestMapping(value = "vh/{homeWorkId}/{packageId}.vpage", method = RequestMethod.GET)
    public String vacationHomework(@PathVariable("homeWorkId") String homeWorkId,
                                   @PathVariable("packageId") Long packageId,
                                   Model model) {
        /*User student = currentStudent();
        if (student != null) {
            model.addAttribute("homeworkId", homeWorkId);
            model.addAttribute("packageId", packageId);
            model.addAttribute("learningType", StudyType.vacationHomework);
            model.addAttribute("subject", Subject.ENGLISH);
            model.addAttribute("questionUrl", "student/exam/vacation/" + Subject.ENGLISH.name() + "/" + packageId + Constants.AntiHijackExt);
            model.addAttribute("completedUrl", "student/exam/vacation/answer/" + homeWorkId + "/" + packageId + Constants.AntiHijackExt);
            model.addAttribute("examResultUrl", "/exam/flash/process/vh/result" + Constants.AntiHijackExt);
            model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
            return "studentv3/exam/homework";
        }*/
        return "redirect:/student/index.vpage";
    }

    // 数学假期作业应试
    @RequestMapping(value = "math/vh/{homeWorkId}/{packageId}.vpage", method = RequestMethod.GET)
    public String mathVacationHomework(@PathVariable("homeWorkId") String homeWorkId,
                                       @PathVariable("packageId") Long packageId,
                                       Model model) {
        /*User student = currentStudent();
        if (student != null) {
            model.addAttribute("homeworkId", homeWorkId);
            model.addAttribute("packageId", packageId);
            model.addAttribute("learningType", StudyType.vacationHomework);
            model.addAttribute("subject", Subject.MATH);
            model.addAttribute("questionUrl", "student/exam/vacation/" + Subject.MATH.name() + "/" + packageId + Constants.AntiHijackExt);
            model.addAttribute("completedUrl", "student/exam/vacation/math/answer/" + homeWorkId + "/" + packageId + Constants.AntiHijackExt);
            model.addAttribute("examResultUrl", "/exam/flash/process/vh/result" + Constants.AntiHijackExt);
            model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
            return "studentv3/exam/homework";
        }*/
        return "redirect:/student/index.vpage";
    }

    // 英语假期作业做过的应试题
    @RequestMapping(value = "vacation/answer/{homeworkId}/{packageId}.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map answer(@PathVariable("homeworkId") Long homeworkId, @PathVariable("packageId") Long packageId) {
        return MapMessage.errorMessage("功能已下线");
    }

    // 数学假期作业做过的应试题
    @RequestMapping(value = "vacation/math/answer/{homeWorkId}/{packageId}.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map mathAnswer(@PathVariable("homeWorkId") Long homeWorkId, @PathVariable("packageId") Long packageId) {
        return MapMessage.errorMessage("功能已下线");
    }

    // 获取作业应试试题信息，返回应试题目、题量、时间、单元、课本信息
    @RequestMapping(value = "vacation/{subject}/{packageId}.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map vacationExams(@PathVariable("subject") String subject, @PathVariable("packageId") Long packageId) {
        return MapMessage.errorMessage("功能已下线");
    }

    private Map<String, Object> loadVacationHomeworkExam(String packageJson) {
        Map<String, Object> jsonMap = JsonUtils.fromJson(packageJson);
        if (jsonMap.isEmpty()) {
            return null;
        }
        List<String> eids = new ArrayList<>();
        if (jsonMap.containsKey("examCart")) {
            // noinspection unchecked
            eids = (List<String>) jsonMap.get("examCart");
        }
        Map<String, Long> examUnitMap = new LinkedHashMap<>();
        for (String eid : eids) {
            examUnitMap.put(eid, SafeConverter.toLong(jsonMap.get("unitId")));
        }
        List<Map<String, Object>> units = new ArrayList<>();
        Map<String, Object> unit = new HashMap<>();
        unit.put("unitId", jsonMap.get("unitId"));
        unit.put("examTime", 0);
        unit.put("examNum", eids.size());
        units.add(unit);

        Map<String, Object> paperMap = new HashMap<>();
        paperMap.put("bookId", jsonMap.get("bookId"));
        paperMap.put("bookName", jsonMap.get("bookName"));
        paperMap.put("examUnitMap", examUnitMap);
        paperMap.put("units", units);
        paperMap.put("normalTime", 0);
        paperMap.put("questionNum", eids.size());
        paperMap.put("eids", eids);
        return paperMap;
    }

    // 获取作业应试试题信息
    @RequestMapping(value = "newhomework/questions.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map newexams(@RequestParam("objectiveConfigType") String objectiveConfigTypeStr, @RequestParam("homeworkId") String homeworkId) {
        WordTeachModuleType wordTeachModuleType = WordTeachModuleType.of( getRequestString("wordTeachModuleType"));
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeStr);

        HomeworkQuestionAnswerRequest request = new HomeworkQuestionAnswerRequest();
        request.setHomeworkId(homeworkId);
        request.setObjectiveConfigType(objectiveConfigType);
        request.setCategoryId(getRequestInt("categoryId", 0));
        request.setLessonId(getRequestString("lessonId"));
        request.setVideoId(getRequestString("videoId"));
        request.setQuestionBoxId(getRequestString("questionBoxId"));
        request.setStoneDataId(getRequestString("stoneDataId"));
        request.setWordTeachModuleType(wordTeachModuleType);
        return MapMessage.successMessage().add("result", newHomeworkLoaderClient.loadHomeworkQuestions(request));
    }

    // 获取作业应试试题信息
    @RequestMapping(value = "newhomework/questions/answer.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map questionsAnswer(@RequestParam("objectiveConfigType") String objectiveConfigTypeStr, @RequestParam("homeworkId") String homeworkId) {
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeStr);
        User student = getHomeworkUser();
        WordTeachModuleType wordTeachModuleType = WordTeachModuleType.of(getRequestString("wordTeachModuleType"));

        HomeworkQuestionAnswerRequest request = new HomeworkQuestionAnswerRequest();
        request.setHomeworkId(homeworkId);
        request.setObjectiveConfigType(objectiveConfigType);
        request.setStudentId(student.getId());
        request.setCategoryId(getRequestInt("categoryId", 0));
        request.setLessonId(getRequestString("lessonId"));
        request.setVideoId(getRequestString("videoId"));
        request.setQuestionBoxId(getRequestString("questionBoxId"));
        request.setStoneDataId(getRequestString("stoneDataId"));
        request.setWordTeachModuleType(wordTeachModuleType);
        if (student != null) {
            return MapMessage.successMessage().add("result", newHomeworkLoaderClient.loadQuestionAnswer(request));
        } else {
            return MapMessage.errorMessage("请登录");
        }
    }

    /**
     * 获取字词讲练作业应试试题信息
     * 字词讲练-字词训练模块PC报告专用
     */
    @RequestMapping(value = "word/teach/questions/answer.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage wordTeachQuestionsAnswer() {
        String homeworkId = getRequestString("homeworkId");
        String type = getRequestString("type");
        if (StringUtils.isAnyBlank(homeworkId, type)) {
            return MapMessage.errorMessage("作业不存在");
        }
        User student = currentStudent();
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(type);
        HomeworkQuestionAnswerRequest request = new HomeworkQuestionAnswerRequest();
        request.setHomeworkId(homeworkId);
        request.setObjectiveConfigType(objectiveConfigType);
        request.setStudentId(student.getId());
        return MapMessage.successMessage().add("result", newHomeworkLoaderClient.loadWordTeachQuestionsAnswer(request));
    }

    @RequestMapping(value = "newhomework/correct/questions.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map correctQuestions(@RequestParam("objectiveConfigType") String objectiveConfigTypeStr, @RequestParam("homeworkId") String homeworkId) {
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeStr);
        User student = getHomeworkUser();
        return MapMessage.successMessage().add("result", newHomeworkLoaderClient.loadHomeworkCorrectQuestions(homeworkId, objectiveConfigType, student.getId()));
    }

    @RequestMapping(value = "newhomework/correct/questions/answer.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map correctQuestionsAnswer(@RequestParam("objectiveConfigType") String objectiveConfigTypeStr, @RequestParam("homeworkId") String homeworkId) {
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeStr);
        User student = getHomeworkUser();
        if (student != null) {
            return MapMessage.successMessage().add("result", newHomeworkLoaderClient.loadCorrectQuestionAnswer(homeworkId, objectiveConfigType, student.getId()));
        } else {
            return MapMessage.errorMessage("请登录");
        }
    }
}
