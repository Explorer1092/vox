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

package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.mapper.QuestionMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("/teacher/homework")
public class TeacherHomeworkController extends AbstractTeacherController {

    private static final List<String> termEndLessonList = Arrays.asList("词汇练习", "语感练习", "重点词汇");
    private static final List<String> termEndNeedFilterPracticeLessonList = Arrays.asList("词汇练习", "重点词汇");

    /*
     * NEW -- 作业 -- 作业列表
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String homeworklist() {
        return "redirect:/teacher/new/homework/report/list.vpage";
    }

    /**
     * NEW -- 作业 -- 作业列表 -- 详情
     */
    @RequestMapping(value = "homeworkdetail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage homeworkDetail() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 调整作业
     */
    @RequestMapping(value = "adjusthomework.vpage", method = RequestMethod.GET)
    public String adjustHomework(@RequestParam("homeworkId") String homeworkId, Model model, HttpServletRequest request) {
        return "redirect:/teacher/new/homework/report/list.vpage";
    }

    private List<Map<String, Object>> processAdjustHomeworkPaperJson(List<String> questionIds) {

        Map<String, QuestionMapper> questionMapperMap = questionLoaderClient.loadQuestionMapperByQids(questionIds, false, false, true);
        List<Map<String, Object>> qlist = new ArrayList<>();
        for (String qid : questionMapperMap.keySet()) {
            long time = 0l;
            for (NewQuestion nq : questionMapperMap.get(qid).getQuestions()) {
                time += nq.getSeconds();
            }
            String pointId = "";
            if (CollectionUtils.isNotEmpty(questionMapperMap.get(qid).getQuestions().get(0).mainKnowledgePointList())) {
                pointId = questionMapperMap.get(qid).getQuestions().get(0).mainKnowledgePointList().get(0);
            }
            qlist.add(MiscUtils.m("id", qid,
                    "time", time,
                    "pointId", pointId));
        }
        return qlist;
    }

    // 根据班级Ids获取可用学豆最大值
    @RequestMapping(value = "maxic.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage calculateMaxIntegralCount() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * NEW -- 提交布置/调整作业表单
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveHomework(@RequestBody Map<String, Object> map) {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * NEW -- 批量布置作业
     */
    @RequestMapping(value = "batchassignhomework.vpage", method = RequestMethod.GET)
    public String clickBatchAssignHomework(Model model, HttpServletRequest request) {
        return "redirect:/teacher/new/homework/batchassignhomework.vpage";
    }

    /**
     * 布置作业--更换课本
     */
    @RequestMapping(value = "changebook.vpage", method = RequestMethod.GET)
    public String changeBook(Model model) {
        return "redirect:/teacher/new/homework/batchassignhomework.vpage";
    }

    /**
     * 搜索阅读理解
     */
    @RequestMapping(value = "reading/search.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage readingSearch(@RequestBody Map<String, Object> jsonMap) {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 根据单元ID推送应试题目
     */
    @RequestMapping(value = "book/exam.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getExam(@RequestParam("unitId") Long unitId) {
        return MapMessage.errorMessage("功能已下线");
    }


    /**
     * 英语---布置期末作业
     * meng.chen 2016.05.16
     */
    @RequestMapping(value = "batchassigntermend.vpage", method = RequestMethod.GET)
    public String batchAssignTermend() {
        return "redirect:/teacher/new/homework/batchassignhomework.vpage";
    }
}
