package com.voxlearning.utopia.service.parent.homework.impl.template.submit.batch;

import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkLoader;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkResultLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.*;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

/**
 * 初始化上下文
 *
 * @author Wenlong Meng
 * @since Jan 16, 2019
 */
@Named
public class HomeworkResultBatchBuildProcessor implements HomeworkProcessor {

    //Local variables
    @Inject private HomeworkLoader homeworkLoader;
    @Inject private HomeworkResultLoader homeworkResultLoader;

    //Logic
    /**
     * 初始化上下文
     *
     * @param hc args
     * @return result
     */
    public void process(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();//参数
        String homeworkId = param.getHomeworkId();//作业id
        Long studentId = param.getStudentId();//学生id
        String objectiveConfigType = param.getObjectiveConfigType();
        HomeworkResult hr = hc.getHomeworkResult();//作业结果
        Homework h = hc.getHomework();//作业
        HomeworkPractice hp = hc.getHomeworkPractice();//查询题目信息

        //构建作业结果
        //查询是否已有作业结果
        HomeworkResult homeworkResult = homeworkResultLoader.loadHomeworkResult(homeworkId,studentId);
        if(homeworkResult == null){//初始化作业结果
            homeworkResult = hr;
            homeworkResult.setId(HomeworkUtil.generatorID(param.getHomeworkId(), param.getStudentId()));
            homeworkResult.setSubject(h.getSubject());
            homeworkResult.setAdditions(h.getAdditions());
            homeworkResult.setQuestionCount(h.getQuestionCount());
            homeworkResult.setActionId(h.getActionId());
            homeworkResult.setGrade(h.getGrade());
            homeworkResult.setStartTime(new Date());
            homeworkResult.setUserId(param.getStudentId());
            homeworkResult.setScore(h.getScore());
            homeworkResult.setSource(h.getSource());
            homeworkResult.setDuration(h.getDuration());
            homeworkResult.setBizType(h.getBizType());
            homeworkResult.setTimeLimit(ObjectUtils.get(()->hp.getPractices().get(0).getTimeLimit(),0));
        }else {
            hc.setHomeworkResult(homeworkResult);//更新作业结果
        }

        //完成状态及时间设置
        homeworkResult.setEndTime(new Date());
        homeworkResult.setFinished(Boolean.TRUE);

        for(HomeworkProcessResult hpr : hc.getHomeworkProcessResults()){
            //构建作业结果详情
            hpr.setObjectiveConfigType(objectiveConfigType);
            hpr.setGrade(h.getGrade());
            hpr.setHomeworkTag(h.getHomeworkTag());
            hpr.setSubject(h.getSubject());
            hpr.setType(h.getType());
            hpr.setHomeworkResultId(homeworkResult.getId());
            hpr.setUserId(studentId);
            //查询题目信息
            Questions questions = hp.getPractices().stream().flatMap(e->e.getQuestions().stream()).filter(e->e.getQuestionId().equals(hpr.getQuestionId())).findFirst().get();
            hpr.setQuestionDocId(questions.getDocId());
            hpr.setQuestionVersion(questions.getQuestionVersion());
            hpr.setHomeworkId(param.getHomeworkId());
        }

    }

}
