/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.parent.homework.impl;

import com.google.common.collect.Sets;
import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkResultLoader;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkRewardService;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.api.mapper.StudentInfo;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 作业奖励实现
 *
 * @author Wenlong Meng
 * @since Feb 21, 2019
 */
@Named
@ExposeService(interfaceClass = HomeworkRewardService.class)
public class HomeworkRewardServiceImpl extends SpringContainerSupport implements HomeworkRewardService {

    UtopiaCache utopiaCache = CacheSystem.CBS.getCache("flushable");
    @Inject
    private HomeworkResultLoader homeworkResultLoader;
    private static Set<String> bizTypes = Sets.newHashSet("EXAM", "MENTAL_ARITHMETIC");
    /**
     * 根据学生id查询作业奖励
     *
     * @param userId
     * @return
     */
    @Override
    public List<Map<String, Object>> loadByUserId(Long userId) {
        String key = HomeworkUtil.generatorDayID("parentHomework_reward", userId);
        List<Map<String, Object>> result = utopiaCache.load(key);
        if(!ObjectUtils.anyBlank(result)){
            utopiaCache.delete(key);
        }
        return result;
    }

    /**
     * 作业奖励
     *
     * @param hr
     * @param studentInfo
     * @return
     */
    @Override
    public int reward(HomeworkResult hr, StudentInfo studentInfo) {
        if(!bizTypes.contains(hr.getBizType())){
            return 0;
        }
        boolean first = homeworkResultLoader.loadHomeworkResultByUserId(hr.getUserId())
                .stream().filter(h->h.getFinished() && h.getBizType().equals(hr.getBizType()) && h.getSubject().equals(hr.getSubject())).count() == 1;
        if(!first){
            return 0;
        }
        Long studentId = hr.getUserId();
        String bizType = hr.getBizType();
        String key = HomeworkUtil.generatorDayID("parentHomework_reward", studentId);
        List<Map<String, Object>> m = utopiaCache.load(key);
        if(m == null){
            m = new ArrayList<>();
        }
        m.add(MapUtils.m("homeworkId", hr.getHomeworkId(), "studentId", studentId, "studentName",studentInfo.getStudentName(), "bizType", bizType, "subject", hr.getSubject()));
        utopiaCache.set(key, DateUtils.getCurrentToDayEndSecond(), m);
        return 1;
    }
}
