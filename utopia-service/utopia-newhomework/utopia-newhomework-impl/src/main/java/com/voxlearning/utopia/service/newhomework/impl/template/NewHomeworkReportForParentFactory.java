package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.util.SpringContainerSupport;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Named
public class NewHomeworkReportForParentFactory extends SpringContainerSupport {
    private Map<Subject, NewHomeworkReportForParentTemple> templates = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.templates = new HashMap<>();
        Map<String, NewHomeworkReportForParentTemple> beans = applicationContext.getBeansOfType(NewHomeworkReportForParentTemple.class);
        for (NewHomeworkReportForParentTemple bean : beans.values()) {
            this.templates.put(bean.getSubject(), bean);
        }
    }

    public NewHomeworkReportForParentTemple getTemplate(Subject subject) {
        if (subject == null) {
            return null;
        }
        return this.templates.get(subject);
    }
}
