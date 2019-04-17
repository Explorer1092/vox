package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.util.SpringContainerSupport;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Named
public class FetchStudentSemesterReportFactory extends SpringContainerSupport {
    private Map<Subject, FetchStudentSemesterReportTemple> templates = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.templates = new HashMap<>();
        Map<String, FetchStudentSemesterReportTemple> beans = applicationContext.getBeansOfType(FetchStudentSemesterReportTemple.class);
        for (FetchStudentSemesterReportTemple bean : beans.values()) {
            this.templates.put(bean.getSubject(), bean);
        }
    }

    public FetchStudentSemesterReportTemple getTemplate(Subject subject) {
        if (subject == null) {
            return null;
        }
        return this.templates.get(subject);
    }
}
