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
import com.voxlearning.utopia.service.parent.homework.api.HomeworkResultLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
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
import java.util.stream.Collectors;

/**
 * 作业结果报告api
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-08
 */
@Controller
@RequestMapping(value = "/parent/homework/report")
@Slf4j
public class HomeworkReportController extends AbstractStudentApiController {

    //local variables
    @ImportService(interfaceClass=HomeworkResultLoader.class)
    private HomeworkResultLoader homeworkResultLoader;

    //Logic
    /**
     * 查询作业结果报告详情
     *
     * @param homeworkId 作业id
     * @param studentId  学生id
     * @return
     */
    @RequestMapping(value = "detail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage index(String homeworkId, Long studentId) {
        //check 参数
        if(ObjectUtils.anyBlank(homeworkId, studentId)){
            return MapMessage.errorMessage("作业id和学生id不能为空")
                    .setErrorCode(ErrorCodeConstants.ERROR_CODE_PARAMETER);
        }
        //check是否登录
        User user = currentUser();
        if (user == null || UserType.PARENT != user.fetchUserType()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        //作业结果
        HomeworkResult homeworkResult = homeworkResultLoader.loadHomeworkResult(homeworkId, studentId);
        if(!ObjectUtils.get(()->homeworkResult.getFinished(), Boolean.FALSE)){
            return MapMessage.errorMessage("作业不存在或未完成")
                    .setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }

        //封装结果
        Map<String, Object> data = new HashMap<>();
        data.put("homeworkId",homeworkResult.getHomeworkId());
        data.put("name", ObjectiveConfigType.EXAM.getValue() + homeworkResult.getQuestionCount()+"题");
        data.put("subject",MapUtils.map("name", homeworkResult.getSubject(),"value", Subject.valueOf(homeworkResult.getSubject()).getValue()));
        data.put("unitName",homeworkResult.getAdditions().get("unitName"));//单元名称
        data.put("questionCount", homeworkResult.getQuestionCount());
        data.put("errorQuestionCount", homeworkResult.getErrorQuestionCount());
        data.put("score", Math.round(homeworkResult.getScore()));
        data.put("userScore", Math.round(homeworkResult.getUserScore()));
        data.put("scoreLevel", HomeworkUtil.score2Level(homeworkResult.getUserScore()).getLevel());
        data.put("duration", (ObjectUtils.get(()->homeworkResult.getDuration(), 300L)+59)/60);
        data.put("userDuration", (homeworkResult.getUserDuration()+59000)/60000);
        data.put("userDurationSecond", (homeworkResult.getUserDuration())/1000);//学生耗时：秒
        data.put("endTime", DateUtils.dateToString(homeworkResult.getEndTime(), "yyyy-MM-dd HH:mm"));
        data.put("level", homeworkResult.getAdditions().get("level"));
        data.put("bizType", homeworkResult.getBizType());
        //作业结果详情
        List<HomeworkProcessResult> hprs = homeworkResultLoader.loadHomeworkProcessResults(homeworkResult.getId());
        //订正状态
        this.correctInfo(data, homeworkResult,hprs);
        //结果详情
        detail(hprs, data);

        return MapMessage.successMessage().add("data", data);
    }

    /**
     * 结果详情
     *
     * @param hprs
     * @param data
     */
    private void detail(List<HomeworkProcessResult> hprs, Map<String, Object> data){
        //得分详情
        List scoreList = hprs.stream().sorted(Comparator.comparing(HomeworkProcessResult::getCreateTime)).collect(
                Collectors.groupingBy(HomeworkProcessResult::getObjectiveConfigType,  Collectors.summingDouble(HomeworkProcessResult::getUserScore))
        ).entrySet().stream().map(e->MapUtils.map("type", ObjectiveConfigType.valueOf(e.getKey()).getValue(), "score", Math.round(e.getValue()))).collect(Collectors.toList());
        data.put("scoreList", scoreList);
    }

    /**
     * 查询作业结果报告列表
     *
     * @param date 日期，格式yyyy-MM-dd
     * @param size 分页大小
     * @param studentId  学生id
     * @return
     */
    @RequestMapping(value = "list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage list(Long studentId, @RequestParam(defaultValue = "0") Integer start, @RequestParam(defaultValue = "5") Integer size, @RequestParam(required = false) String date) {
        //check是否登录
        User user = currentUser();
        if (user == null || UserType.PARENT != user.fetchUserType()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        //check 参数
        if(ObjectUtils.anyBlank(studentId)){
            return MapMessage.errorMessage("学生id不能为空")
                    .setErrorCode(ErrorCodeConstants.ERROR_CODE_PARAMETER);
        }
        //非法size重置
        if(size < 1 || size > 20){
            logger.warn("size={}， reset size = 5", size);
            size = 5;
        }
        //非法start重置
        if(start < 0 || start > 300){
            logger.warn("start={}， reset start = 0", start);
            start = 0;
        }
        //日期：默认当天
        Date startTime;
        if(ObjectUtils.anyBlank(date)){
            startTime = new Date();
        }else{
            try{
                startTime = DateUtils.stringToDate(date, DateUtils.FORMAT_SQL_DATETIME);
            }catch (Exception e){
                log.error("list({},{},{},{})", studentId, start, size, date, e);
                return MapMessage.errorMessage("日期格式不正确")
                        .setErrorCode(ErrorCodeConstants.ERROR_CODE_PARAMETER);
            }

        }

        //作业结果
        List<HomeworkResult> homeworkResults = homeworkResultLoader.loadHomeworkResultDown(studentId, start , size + 1, startTime);

        //更多标识，并移除多余记录
        boolean hasNext = homeworkResults.size() > size;
        if(hasNext){
            homeworkResults = homeworkResults.subList(0, homeworkResults.size() - 1);
        }

        //封装结果
        Map<String, List<Map<String, Object>>> data = new LinkedHashMap<>();
        for(HomeworkResult homeworkResult : homeworkResults){
            String createDate = DateUtils.dateToString(homeworkResult.getCreateTime(), DateUtils.FORMAT_SQL_DATE);
            List<Map<String, Object>> ds =  data.get(createDate);
            if(ds == null){
                ds = new ArrayList<>();
                data.put(createDate, ds);
            }
            Map<String, Object> d = new HashMap<>();
            d.put("homeworkId",homeworkResult.getHomeworkId());
            d.put("subject",MapUtils.map("name", homeworkResult.getSubject(),"value", Subject.valueOf(homeworkResult.getSubject()).getValue()));
            d.put("name", ObjectiveConfigType.EXAM.getValue());
            d.put("unitName",homeworkResult.getAdditions().get("unitName"));//单元名称
            d.put("questionCount", homeworkResult.getQuestionCount());
            Boolean finished = ObjectUtils.get(()->homeworkResult.getFinished(), Boolean.FALSE);
            d.put("status", homeworkResult == null ? -1 : (finished ? 1 : 0));
            if(finished){
                d.put("userDuration", (homeworkResult.getUserDuration() + 59000)/60000);//学生耗时：分钟
                d.put("userDurationSecond", (homeworkResult.getUserDuration())/1000);//学生耗时：秒
                d.put("userScore", Math.round(homeworkResult.getUserScore()));//学生得分
                d.put("scoreLevel", HomeworkUtil.score2Level(homeworkResult.getUserScore()).getLevel());
            }
            d.put("errorQuestionCount", homeworkResult.getErrorQuestionCount());
            d.put("score", Math.round(homeworkResult.getScore()));
            d.put("duration", (homeworkResult.getDuration()+59)/60);
            d.put("endTime", DateUtils.dateToString(homeworkResult.getEndTime(), "yyyy-MM-dd HH:mm"));
            String level = ObjectUtils.get(()->(String)homeworkResult.getAdditions().get("level"));
            if(StringUtils.isNotBlank(level)){
                d.put("level", MapUtils.m(level, HomeworkUtil.levelCName(level)));
            }
            d.put("bizType", homeworkResult.getBizType());
            correctInfo(d, homeworkResult, null);
            ds.add(d);
        }
        List d = data.entrySet().stream().map(e->MapUtils.m("date", e.getKey(), "list",e.getValue())).collect(Collectors.toList());

        //返回参数date
        if(ObjectUtils.anyBlank(date)){
            date = DateUtils.dateToString(startTime);
        }
        return MapMessage.successMessage().add("hasNext", hasNext).add("date", date).add("data", d);
    }

    /**
     * 订正信息
     *
     * @param d
     * @param homeworkResult
     */
    private void correctInfo(Map<String, Object> d, HomeworkResult homeworkResult, List<HomeworkProcessResult> hprs){
        int correctStatus = -1;
        if(homeworkResult.getErrorQuestionCount() > 0){
            HomeworkResult correctHR = homeworkResultLoader.loadHomeworkResult(HomeworkUtil.generatorID(homeworkResult.getHomeworkId(), homeworkResult.getUserId(), DoType.CORRECT));
            if(correctHR == null){
                if(hprs == null){
                    hprs = homeworkResultLoader.loadHomeworkProcessResults(homeworkResult.getId());
                }
                if(hprs.stream().anyMatch(p->!p.getRight())){
                    correctStatus = 0;
                }else{
                    LoggerUtils.info("errorHomeworkResult", homeworkResult.getId());
                }
            }else {
                correctStatus = correctHR.getFinished()==Boolean.TRUE ? 1:0;
            }
        }
        d.put("correctStatus", correctStatus);
    }

}
