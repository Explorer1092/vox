package com.voxlearning.utopia.service.parent.homework.impl.template.assign;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.api.mapper.UserPreference;
import com.voxlearning.utopia.service.parent.homework.impl.HomeworkUserPreferencesServiceImpl;
import com.voxlearning.utopia.service.parent.homework.impl.cache.CacheKey;
import com.voxlearning.utopia.service.parent.homework.impl.cache.HomeWorkCache;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;

/**
 * 纸质口算存教材
 * @author chongfeng.qi
 * @date 20190121
 */
@Named
public class HomeworkBookProcessor implements HomeworkProcessor {
    private static int threeMonth = 90 * 24 * 60 * 60; // 缓存三个月
    @Inject
    private HomeworkUserPreferencesServiceImpl homeworkUserPreferencesService;

    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();
        UserPreference userPreference = new UserPreference();
        userPreference.setUserId(param.getStudentId());
        userPreference.setSubject(param.getSubject());
        userPreference.setBookId(param.getBookId());
        // 设置教材
        homeworkUserPreferencesService.upsertUserPreferencesNoMessage(Collections.singletonList(userPreference));
        HomeWorkCache.set(threeMonth, MapUtils.map("unitId", hc.getUnitId(), "sectionIds", param.getSectionIds()), CacheKey.UNIT,  param.getBizType(), param.getStudentId(), param.getSubject());
    }
}
