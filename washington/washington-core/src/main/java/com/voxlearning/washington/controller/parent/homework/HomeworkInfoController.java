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

package com.voxlearning.washington.controller.parent.homework;

import com.google.common.collect.Sets;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkLoader;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkResultLoader;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkRewardService;
import com.voxlearning.utopia.service.parent.homework.api.entity.Homework;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkPractice;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.api.model.DoType;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.open.AbstractStudentApiController;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.controller.parent.homework.util.HomeworkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 作业列表api
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-08
 */
@Controller
@RequestMapping(value = "/parent/homework/info")
@Slf4j
public class HomeworkInfoController extends AbstractStudentApiController {

    //local variables
    /**
     * 作业结果Loader
     */
    @ImportService(interfaceClass=HomeworkResultLoader.class)
    private HomeworkResultLoader homeworkResultLoader;
    /**
     * 作业Loader
     */
    @ImportService(interfaceClass=HomeworkLoader.class)
    private HomeworkLoader homeworkLoader;
    @ImportService(interfaceClass=HomeworkRewardService.class)
    private HomeworkRewardService hrs;
    private static Set<String> bizTypes = Sets.newHashSet("EXAM", "MENTAL_ARITHMETIC");

    //Logic
    /**
     * 查询作业列表
     *
     * @param studentId  学生id
     * @return
     */
    @RequestMapping(value = "list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage index(Long studentId, @RequestParam(required = false, defaultValue = "EXAM") String bizType) {
        //check 参数
        if(ObjectUtils.anyBlank(studentId)){
            return MapMessage.errorMessage("学生id不能为空")
                    .setErrorCode(ErrorCodeConstants.ERROR_CODE_PARAMETER);
        }
        //check是否登录
        User user = currentUser();
        //未登录、非父母、非本人
        if (user == null || UserType.PARENT != user.fetchUserType()){
            return MapMessage.errorMessage("账号异常").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        //作业
        List<Homework> homeworks = homeworkLoader.loadHomeworkByUserId(studentId).stream().filter(h->!match(h, ObjectiveConfigType.OCR_MENTAL_ARITHMETIC.name())).collect(Collectors.toList());
        if(ObjectUtils.anyBlank(homeworks)){
            return MapMessage.successMessage();
        }
        Map<String, HomeworkPractice> homeworkPracticeMap = homeworkLoader.loadHomeworkPractices(homeworks.stream().map(Homework::getId).collect(Collectors.toList()));
        //作业结果
        List<HomeworkResult> homeworkResults = homeworkResultLoader.loadHomeworkResultByUserId(studentId);
        Map<String, HomeworkResult> homeworkResultMap = homeworkResults.stream().collect(Collectors.toMap(HomeworkResult::getHomeworkId, Function.identity()));

        //封装结果
        List<Map> data = homeworks.stream().filter(u->contains(u)).sorted(Comparator.comparing(Homework::getCreateTime).reversed()).map(e->{
            HomeworkResult homeworkResult = homeworkResultMap.get(e.getId());
            Map<String, Object> d = new HashMap<>();
            d.put("homeworkId",e.getId());
            d.put("subject",MapUtils.map("name", e.getSubject(),"value", Subject.valueOf(e.getSubject()).getValue()));
            d.put("unitName",e.getAdditions().get("unitName"));//单元名称
            d.put("questionCount", e.getQuestionCount());
            Boolean finished = ObjectUtils.get(()->homeworkResult.getFinished(), Boolean.FALSE);
            d.put("status", homeworkResult == null ? -1 : (finished ? 1 : 0));
            if(finished){
                d.put("userDuration", (homeworkResult.getUserDuration() + 59000)/60000);//学生耗时：分钟
                d.put("userDurationSecond", homeworkResult.getUserDuration()/1000);//学生耗时：秒
                d.put("userScore", Math.round(homeworkResult.getUserScore()));//学生得分
                d.put("scoreLevel", HomeworkUtil.score2Level(homeworkResult.getUserScore()).getLevel());
            }
            d.put("duration", (e.getDuration() + 59)/60);//预计耗时：分钟
            d.put("score", Math.round(e.getScore()));//总分
            d.put("endTime", DateUtils.dateToString(e.getEndTime(), "M月dd日 HH:mm"));
            String level = ObjectUtils.get(()->(String)e.getAdditions().get("level"));
            if(StringUtils.isNotBlank(level)){
                d.put("level", MapUtils.m(level, HomeworkUtil.levelCName(level)));
            }
            d.put("bizType", e.getBizType());
            String objectiveConfigType = homeworkPracticeMap.get(e.getId()).getPractices().get(0).getType();
            d.put("objectiveConfigType", objectiveConfigType);
            d.put("name", ObjectUtils.get(()->ObjectiveConfigType.of(objectiveConfigType).getValue(), "同步习题"));
            this.correctInfo(d, homeworkResult);//订正状态
            return d;
        }).collect(Collectors.toList());
        List<Map<String, Object>> rewards = hrs.loadByUserId(studentId);
        return MapMessage.successMessage().set("data", data).set("rewards", rewards);
    }

    /**
     * 订正信息
     *
     * @param d
     * @param homeworkResult
     */
    private void correctInfo(Map<String, Object> d, HomeworkResult homeworkResult){
        int correctStatus = -1;
        if(ObjectUtils.get(()->homeworkResult.getFinished(), Boolean.FALSE) && homeworkResult.getErrorQuestionCount() > 0){
            boolean result = this.homeworkResultLoader.loadHomeworkProcessResults(homeworkResult.getId()).stream().anyMatch(p->!p.getRight());
            if(result){
                HomeworkResult correctHR = homeworkResultLoader.loadHomeworkResult(HomeworkUtil.generatorID(homeworkResult.getHomeworkId(), homeworkResult.getUserId(), DoType.CORRECT));
                correctStatus =  correctHR != null && correctHR.getFinished()==Boolean.TRUE ? 1:0;
            }else{
                LoggerUtils.info("errorHomeworkResult", homeworkResult.getId());
            }
        }
        d.put("correctStatus", correctStatus);
    }

    /**
     * 检查作业是否属于该业务类型
     *
     * @param homework
     * @param bizType
     * @return
     */
    private boolean match(Homework homework, String bizType){
        return (StringUtils.equals(homework.getBizType(), bizType)||(bizType.equals("EXAM") && StringUtils.isEmpty(homework.getBizType())));
    }

    private boolean contains(Homework h){
        return StringUtils.isEmpty(h.getBizType()) || bizTypes.contains(h.getBizType());
    }

}
