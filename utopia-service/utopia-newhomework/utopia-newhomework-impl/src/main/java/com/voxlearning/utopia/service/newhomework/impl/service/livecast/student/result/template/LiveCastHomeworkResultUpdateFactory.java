package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result.template;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2017/7/10
 */
@Named
public class LiveCastHomeworkResultUpdateFactory extends SpringContainerSupport {
    private Map<NewHomeworkContentProcessTemp, LiveCastHomeworkResultUpdateTemplate> templateMap;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        templateMap = new HashMap<>();
        Map<String, LiveCastHomeworkResultUpdateTemplate> beans = applicationContext.getBeansOfType(LiveCastHomeworkResultUpdateTemplate.class);
        for (LiveCastHomeworkResultUpdateTemplate bean : beans.values()) {
            this.templateMap.put(bean.getNewHomeworkResultUpdateTemp(), bean);
        }
    }

    public LiveCastHomeworkResultUpdateTemplate getTemplate(NewHomeworkContentProcessTemp newHomeworkResultUpdateTemp) {
        if (newHomeworkResultUpdateTemp == null) {
            return null;
        }
        return this.templateMap.get(newHomeworkResultUpdateTemp);
    }
}
