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
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.service.content.api.entity.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * math book controller
 * User: maofeng.lu
 * Version : 0.1
 * Date: 13-7-9
 * Time: 下午4:30
 */
@Controller
@RequestMapping("math/book")
public class MathBookController extends AbstractTeacherController {
    /**
     * 布置作业-->随堂练习
     * 通过教材id获取单元
     *
     * @param bookId 教材id
     * @param model  model
     * @return 单元message
     */
    @RequestMapping(value = "follower/unit.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage followerUnit(@RequestParam("bookId") Long bookId, Model model) {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 布置作业-->点击课本-->点击单元
     * 数学教材下某单元的课时查询
     *
     * unitId
     * @param model
     * @return
     */
    @RequestMapping(value = "follower/lesson.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage learningFollowerMathLesson(Model model) {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 布置作业-->点击课本-->点击单元
     * 数学教材下某单元的课程的课时查询
     *
     * @param unitId
     * @param model
     * @return
     */
    @RequestMapping(value = "follower/section.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage learningFollowerMathSection(@RequestParam("unitId") Long unitId, @RequestParam("homeworkForm") String homeworkForm, Model model) {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "base.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendStarReward(@RequestBody Map<String, Object> jsonMap) {

        String pointInfo = JsonUtils.toJson(jsonMap.get("pointInfo"));
        int density = SafeConverter.toInt(jsonMap.get("density"));
        List<Map> pointObjs = JsonUtils.fromJsonToList(pointInfo, Map.class);
        try {
            String cdnUrl = getCdnBaseUrlStaticSharedWithSep();
            PracticeType practiceType = practiceLoaderClient.loadPractice(216);
            Collection<Map<String, Object>> pointBases = mathContentLoaderClient.getMathPointBases(pointObjs, cdnUrl, density);
            if(CollectionUtils.isEmpty( pointBases)){
                return MapMessage.errorMessage("没有可用题");
            }else {
                return MapMessage.successMessage()
                        .add("practice", MiscUtils.m("id", practiceType.getId(), "name", practiceType.getPracticeName(),"type",practiceType.getId()))
                        .add("pointBases", mathContentLoaderClient.getMathPointBases(pointObjs, cdnUrl, density));
            }

        } catch (Exception ex) {
            logger.error(getClass().getName() + ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    /**
     * 布置作业-->点击课本-->点击单元
     * 数学教材下某单元的课时查询
     *
     * unitId
     * @param model
     * @return
     */
    @RequestMapping(value = "mentalarithmetic/lesson.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage learningFollowerMathLessonQuestion(Model model) {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * NEW -- 快速布置数学作业（10分钟或者20分钟）
     */
    @RequestMapping(value = "follower/lesson/qSelect.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage qSelectMathFollowerLesson() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 根据知识点的基本时间和最大时间计算出题的最大数量，避免在快速出题时由于
     * 时间超过阀值而无法推题。
     *
     * @param basePlayTime
     * @param maxPlayTime
     * @return
     */
    private static int getMaxQuestionCount(int basePlayTime, int maxPlayTime) {
        int maxCount = maxPlayTime / 10 / basePlayTime;
        if (maxCount > 5) {
            maxCount = 5;
        } else if (maxCount == 0) {
            maxCount = 1;
        }

        return maxCount;
    }

    /**
     * NEW -- 快速布置数学作业（10分钟或者20分钟）
     */
    @RequestMapping(value = "lesson/qSelect.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage qSelectMathLesson() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 查出应试题
     */
    @RequestMapping(value = "exam/{unitId}.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getExam(@PathVariable("unitId") Long unitId) {

        return MapMessage.errorMessage("功能已下线");
    }




//    @RequestMapping(value = "preview.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage preview(@RequestBody Map<String, Object> commend) {
//        // 二次确认页面预览应试题是生成临时试卷模板
//        MapMessage mapMessage = new MapMessage();
//        try {
//            String paperJson = JsonUtils.toJson(commend.get("paperJson"));
//            User user = currentUser();
//            Set<RoleType> roleTypeSet = userLoaderClient.loadUserRoles(user);
//            if (!roleTypeSet.contains(RoleType.ROLE_TEACHER)) {
//                return MapMessage.errorMessage("无权限预览");
//            }
//            String paperId = deprecatedExamServiceClient.getRemoteReference().createTemp(user.getId(), conversionService.convert(commend.get("clazzId"), Long.class), paperJson);
//            if (StringUtils.isNotBlank(paperId)) {
//                mapMessage.setSuccess(true);
//                mapMessage.add("paperId", paperId);
//            }
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            mapMessage.setSuccess(false);
//        }
//        return mapMessage;
//    }

}