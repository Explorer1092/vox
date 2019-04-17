package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.constant.TermReviewContentType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2016/11/21
 */
@Named
public class TermReviewContentLoaderFactory extends SpringContainerSupport {
    private Map<TermReviewContentType, TermReviewContentLoaderTemplate> templates = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.templates = new HashMap<>();
        Map<String, TermReviewContentLoaderTemplate> beans = applicationContext.getBeansOfType(TermReviewContentLoaderTemplate.class);
        for (TermReviewContentLoaderTemplate bean : beans.values()) {
            this.templates.put(bean.getTermReviewContentType(), bean);
        }
    }

    public TermReviewContentLoaderTemplate getTemplate(TermReviewContentType termReviewContentType) {
        if (termReviewContentType == null) {
            return null;
        }
        return this.templates.get(termReviewContentType);
    }
}
