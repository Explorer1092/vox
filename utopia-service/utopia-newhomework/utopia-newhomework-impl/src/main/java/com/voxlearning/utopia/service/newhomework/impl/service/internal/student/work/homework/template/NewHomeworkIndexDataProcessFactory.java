package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework.template;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkIndexDataProcessTemp;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author guohong.tan
 * @since 2017/6/29
 */
@Named
public class NewHomeworkIndexDataProcessFactory extends SpringContainerSupport {

    private Map<NewHomeworkIndexDataProcessTemp, NewHomeworkIndexDataProcessTemplate> templateMap;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.templateMap = new HashMap<>();
        Map<String, NewHomeworkIndexDataProcessTemplate> beans = applicationContext.getBeansOfType(NewHomeworkIndexDataProcessTemplate.class);
        for (NewHomeworkIndexDataProcessTemplate bean : beans.values()) {
            this.templateMap.put(bean.getNewHomeworkIndexDataTemp(), bean);
        }
    }

    public NewHomeworkIndexDataProcessTemplate getTemplate(NewHomeworkIndexDataProcessTemp newHomeworkIndexDataProcessTemp) {
        if (newHomeworkIndexDataProcessTemp == null) {
            return null;
        }
        return this.templateMap.get(newHomeworkIndexDataProcessTemp);
    }
}
