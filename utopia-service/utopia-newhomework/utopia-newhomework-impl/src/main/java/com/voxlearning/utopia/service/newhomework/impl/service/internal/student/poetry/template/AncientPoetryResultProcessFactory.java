package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.poetry.template;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.constant.ModelType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Named
public class AncientPoetryResultProcessFactory extends SpringContainerSupport {

    private Map<ModelType, AncientPoetryResultProcessTemplate> templateMap;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.templateMap = new HashMap<>();
        Map<String, AncientPoetryResultProcessTemplate> beans = applicationContext.getBeansOfType(AncientPoetryResultProcessTemplate.class);
        for (AncientPoetryResultProcessTemplate bean : beans.values()) {
            this.templateMap.put(bean.getProcessResultModel(), bean);
        }
    }

    public AncientPoetryResultProcessTemplate getTemplate(ModelType ancientPoetryResultProcessTemplate) {
        if (ancientPoetryResultProcessTemplate == null) {
            return null;
        }
        return this.templateMap.get(ancientPoetryResultProcessTemplate);
    }
}
