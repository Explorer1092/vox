/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.MiddleSchoolHomeworkLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.MiddleSchoolHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.middleschool.MiddleSchoolHomeworkCrmHistory;
import com.voxlearning.utopia.service.newhomework.impl.dao.MiddleSchoolHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.MiddleSchoolHomeworkDoDao;
import com.voxlearning.utopia.service.user.api.StudentLoader;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@Service(interfaceClass = MiddleSchoolHomeworkLoader.class)
@ExposeService(interfaceClass = MiddleSchoolHomeworkLoader.class)
public class MiddleSchoolHomeworkLoaderImpl implements MiddleSchoolHomeworkLoader {

    @Inject private MiddleSchoolHomeworkDao middleSchoolHomeworkDao;
    @Inject private MiddleSchoolHomeworkDoDao middleSchoolHomeworkDoDao;
    @Inject private StudentLoader studentLoader;

    @Override
    public List<MiddleSchoolHomeworkCrmHistory> loadGroupRecentHomeworkList(Collection<GroupMapper> groups, Integer day) {
        Date startDate = DateUtils.getDayStart(DateUtils.calculateDateDay(new Date(), -day));
        Date endDate = new Date();

        if (CollectionUtils.isEmpty(groups)) {
            return Collections.emptyList();
        }
        Map<Long, GroupMapper> groupMap = groups.stream().collect(Collectors.toMap(GroupMapper::getId, Function.identity()));
        Collection<Long> groupIds = groupMap.keySet();

                // 获取作业列表
        List<MiddleSchoolHomework> homeworkList = middleSchoolHomeworkDao.loadGroupHomeworkList(groupIds, startDate, endDate);

        // 获取班级人数
        Map<Long, List<Long>> studentIdsMap = studentLoader.loadGroupStudentIds(groupIds);

        List<MiddleSchoolHomeworkCrmHistory> result = new ArrayList<>();
        homeworkList.forEach(h -> {
            // 作业的完成学生数量
            Long finishedCount = middleSchoolHomeworkDoDao.getFinishedStudentCount(h);
            Long studentCount = studentIdsMap.containsKey(h.getGroupId()) ? studentIdsMap.get(h.getGroupId()).size() : SafeConverter.toLong(0);

            MiddleSchoolHomeworkCrmHistory history = new MiddleSchoolHomeworkCrmHistory();
            history.setHomeworkId(h.getId());
            history.setName(h.getName());
            history.setGroup(groupMap.get(h.getGroupId()));
            history.setCreateTime(h.getCreateTime());
            history.setStartTime(h.getStartTime());
            history.setCloseTime(h.getCloseTime());
            history.setFinishedCount(finishedCount);
            history.setStudentCount(studentCount);
            history.setDisabled(Objects.equals(h.getStatus(),-1L));
            result.add(history);
        });
        return result;
    }
}
