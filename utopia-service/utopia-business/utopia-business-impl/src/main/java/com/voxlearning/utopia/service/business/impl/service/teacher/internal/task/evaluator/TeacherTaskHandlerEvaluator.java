package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.evaluator;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.entity.constant.TeacherTaskCalType;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import org.springframework.beans.factory.BeanFactoryUtils;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class TeacherTaskHandlerEvaluator extends SpringContainerSupport{

    private List<TeacherTaskEvaluator> handlers;

    private Map<TeacherTaskTpl.TplEvaluatorEvent, TeacherTaskEvaluator> handlerMap;

    @Override
    public void afterPropertiesSet(){
        handlers = new ArrayList<>();
        handlers.addAll(BeanFactoryUtils.beansOfTypeIncludingAncestors(
                getApplicationContext(),
                TeacherTaskEvaluator.class,
                false,
                true)
                .values());

        this.handlerMap = this.handlers.stream().collect(Collectors.toMap(k -> k.getTplEvaluatorEvent(), v -> v));
    }

    public TeacherTaskEvaluator getHandler(TeacherTaskTpl.TplEvaluatorEvent tplEvaluatorEvent){
        if(this.handlerMap == null){
            logger.error("TeacherTaskHandlerEvaluator handler map is empty!");
            return null;
        }
        return handlerMap.get(tplEvaluatorEvent);
    }

}
