package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2016/1/25
 */
@Named
public class NewHomeworkContentLoaderFactory extends SpringContainerSupport {
    private Map<ObjectiveConfigType, NewHomeworkContentLoaderTemplate> templates = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.templates = new HashMap<>();
        Map<String, NewHomeworkContentLoaderTemplate> beans = applicationContext.getBeansOfType(NewHomeworkContentLoaderTemplate.class);
        for (NewHomeworkContentLoaderTemplate bean : beans.values()) {
            this.templates.put(bean.getObjectiveConfigType(), bean);
        }
    }

    public NewHomeworkContentLoaderTemplate getTemplate(ObjectiveConfigType objectiveConfigType) {
        if (objectiveConfigType == null) {
            return null;
        }
        return this.templates.get(objectiveConfigType);
    }
}
