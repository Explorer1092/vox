package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.constant.NatureSpellingType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Named
public class ProcessAppDetailByCategoryIdFactory extends SpringContainerSupport {
    private Map<NatureSpellingType, ProcessAppDetailByCategoryIdTemplate> templates;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.templates = new HashMap<>();
        Map<String, ProcessAppDetailByCategoryIdTemplate> beans = applicationContext.getBeansOfType(ProcessAppDetailByCategoryIdTemplate.class);
        for (ProcessAppDetailByCategoryIdTemplate bean : beans.values()) {
            this.templates.put(bean.getNatureSpellingType(), bean);
        }
    }


    public ProcessAppDetailByCategoryIdTemplate getTemplate(NatureSpellingType natureSpellingType) {
        if (natureSpellingType == null) {
            natureSpellingType = NatureSpellingType.COMMON;
        }
        if (!this.templates.containsKey(natureSpellingType)) {
            natureSpellingType = NatureSpellingType.COMMON;
        }
        return this.templates.get(natureSpellingType);
    }
}
