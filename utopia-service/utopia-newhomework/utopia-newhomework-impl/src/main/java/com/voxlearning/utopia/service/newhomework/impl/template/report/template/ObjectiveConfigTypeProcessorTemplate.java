package com.voxlearning.utopia.service.newhomework.impl.template.report.template;

import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.TeacherReportParameter;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;


abstract public class ObjectiveConfigTypeProcessorTemplate extends NewHomeworkSpringBean {

    abstract public ObjectiveConfigType getObjectiveConfigType();

    //老接口，只剩下老客户端会用
    abstract public void processor(TeacherReportParameter teacherReportParameter);

    abstract public void handleKnowledgePointStatistics(TeacherReportParameter teacherReportParameter, NewHomeworkResultAnswer newHomeworkResultAnswer);

    abstract public void handleBaseAppType(TeacherReportParameter teacherReportParameter, boolean isPcWay);

    abstract public void initKnowledgePointIdToQIds(TeacherReportParameter teacherReportParameter);

}
