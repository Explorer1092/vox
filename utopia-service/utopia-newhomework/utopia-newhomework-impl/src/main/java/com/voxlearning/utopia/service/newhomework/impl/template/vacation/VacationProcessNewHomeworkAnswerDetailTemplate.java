package com.voxlearning.utopia.service.newhomework.impl.template.vacation;


import com.voxlearning.utopia.service.newhomework.api.mapper.report.vacationhomework.ReportRateContext;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;


abstract public class VacationProcessNewHomeworkAnswerDetailTemplate extends NewHomeworkSpringBean {
    abstract public ObjectiveConfigType getObjectiveConfigType();

    /**
     * 获取单个学生整份作业模板接口
     * For：PC端 & 手机端H5
     * @param reportRateContext
     */
    abstract public void processNewHomeworkAnswerDetailPersonal(ReportRateContext reportRateContext);

}