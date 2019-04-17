package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author guohong.tan
 * @since 2016/11/23
 */
@Named
public class NewHomeworkResultUpdateFactory extends SpringContainerSupport {

    private Map<NewHomeworkContentProcessTemp, NewHomeworkResultUpdateTemplate> templateMap;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.templateMap = new HashMap<>();
        Map<String, NewHomeworkResultUpdateTemplate> beans = applicationContext.getBeansOfType(NewHomeworkResultUpdateTemplate.class);
        for (NewHomeworkResultUpdateTemplate bean : beans.values()) {
            this.templateMap.put(bean.getNewHomeworkResultUpdateTemp(), bean);
        }
    }

    public NewHomeworkResultUpdateTemplate getTemplate(NewHomeworkContentProcessTemp newHomeworkResultUpdateTemp) {
        if (newHomeworkResultUpdateTemp == null) {
            return null;
        }
        return this.templateMap.get(newHomeworkResultUpdateTemp);
    }
}
