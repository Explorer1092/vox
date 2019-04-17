package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.lang.util.SpringContainerSupport;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理
 *
 * @author xuesong.zhang
 * @since 2016/8/4
 */
@Named
public class ProcessResultLoaderFactory extends SpringContainerSupport {

    private Map<SchoolLevel, ProcessResultTemplate> templates = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.templates = new HashMap<>();
        Map<String, ProcessResultTemplate> beans = applicationContext.getBeansOfType(ProcessResultTemplate.class);
        for (ProcessResultTemplate bean : beans.values()) {
            this.templates.put(bean.getSchoolLevel(), bean);
        }
    }

    public ProcessResultTemplate getTemplate(SchoolLevel schoolLevel) {
        if (schoolLevel == null) {
            return null;
        }
        return this.templates.get(schoolLevel);
    }

}
