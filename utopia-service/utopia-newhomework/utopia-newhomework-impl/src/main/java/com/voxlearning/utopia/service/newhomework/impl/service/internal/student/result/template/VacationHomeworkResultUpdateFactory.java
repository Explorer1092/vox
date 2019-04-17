package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class VacationHomeworkResultUpdateFactory extends SpringContainerSupport {
    private Map<NewHomeworkContentProcessTemp, VacationHomeworkResultUpdateTemplate> templateMap;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.templateMap = new HashMap<>();
        Map<String, VacationHomeworkResultUpdateTemplate> beans = applicationContext.getBeansOfType(VacationHomeworkResultUpdateTemplate.class);
        for (VacationHomeworkResultUpdateTemplate bean : beans.values()) {
            this.templateMap.put(bean.getNewHomeworkResultUpdateTemp(), bean);
        }
    }

    public VacationHomeworkResultUpdateTemplate getTemplate(NewHomeworkContentProcessTemp newHomeworkResultUpdateTemp) {
        if (newHomeworkResultUpdateTemp == null) {
            return null;
        }
        return this.templateMap.get(newHomeworkResultUpdateTemp);
    }
}
