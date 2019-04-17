package com.voxlearning.utopia.service.newhomework.impl.template.report.template;


import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.TypePartContext;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

abstract public class AppObjectiveConfigTypeProcessorTemplate extends NewHomeworkSpringBean {

    abstract public ObjectiveConfigType getObjectiveConfigType();

    abstract public void fetchTypePart(TypePartContext typePartContext);

}
