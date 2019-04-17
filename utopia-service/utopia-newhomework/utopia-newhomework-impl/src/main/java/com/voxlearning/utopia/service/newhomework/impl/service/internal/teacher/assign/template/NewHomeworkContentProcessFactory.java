package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tanguohong on 16/7/7.
 */
@Named
public class NewHomeworkContentProcessFactory extends SpringContainerSupport {

    private Map<NewHomeworkContentProcessTemp, NewHomeworkContentProcessTemplate> templateMap;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.templateMap = new HashMap<>();
        Map<String, NewHomeworkContentProcessTemplate> beans = applicationContext.getBeansOfType(NewHomeworkContentProcessTemplate.class);
        for (NewHomeworkContentProcessTemplate bean : beans.values()) {
            this.templateMap.put(bean.getNewHomeworkContentTemp(), bean);
        }
    }

    public NewHomeworkContentProcessTemplate getTemplate(NewHomeworkContentProcessTemp newHomeworkContentProcessTemp) {
        if (newHomeworkContentProcessTemp == null) {
            return null;
        }
        return this.templateMap.get(newHomeworkContentProcessTemp);
    }
}
