package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactoryUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by zhouwei on 2018/9/3
 **/
@Named
@Slf4j
public class TeacherTaskHandlerInit extends SpringContainerSupport {

    private List<TeacherTaskInit> handlers;

    private Map<TeacherTaskTpl.Tpl, TeacherTaskInit> handlerMap;

    @Override
    public void afterPropertiesSet(){
        handlers = new ArrayList<>();
        handlers.addAll(BeanFactoryUtils.beansOfTypeIncludingAncestors(
                getApplicationContext(),
                TeacherTaskInit.class,
                false,
                true)
                .values());

        this.handlerMap = this.handlers.stream().collect(Collectors.toMap(k -> k.getTeacherTaskTpl(), v -> v));
    }

    public TeacherTaskInit getHandler(TeacherTaskTpl.Tpl tpl){
        if(this.handlerMap == null){
            logger.error("TeacherTaskHandlerInit handler map is empty!");
            return null;
        }
        TeacherTaskInit init = handlerMap.get(tpl);
        if (null == init) {
            logger.error("TeacherTaskHandlerInit is NULL!");
        }
        return init;
    }

}
