package com.voxlearning.utopia.service.newhomework.impl.template.assign;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2016/10/18
 */
@Named
public class AssignHomeworkServiceFactory extends SpringContainerSupport {
    private Map<NewHomeworkType, AssignHomeworkTemplate> templates = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.templates = new HashMap<>();
        Map<String, AssignHomeworkTemplate> beans = applicationContext.getBeansOfType(AssignHomeworkTemplate.class);
        for (AssignHomeworkTemplate bean : beans.values()) {
            this.templates.put(bean.getNewHomeworkType(), bean);
        }
    }

    public AssignHomeworkTemplate getTemplate(NewHomeworkType newHomeworkType) {
        if (newHomeworkType == null) {
            return null;
        }
        return this.templates.get(newHomeworkType);
    }
}
