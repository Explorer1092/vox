/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.service.student.internal;

import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.utopia.api.constant.MissionState;
import com.voxlearning.utopia.entity.mission.Mission;
import com.voxlearning.utopia.service.business.impl.mission.MissionLoaderImpl;
import com.voxlearning.utopia.service.business.impl.service.AsyncBusinessCacheServiceImpl;
import com.voxlearning.utopia.service.business.impl.service.BusinessStudentServiceImpl;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author RuiBao
 * @version 0.1
 * @since 1/15/2015
 */
@Named
public class LoadStudentParentRewardCard extends AbstractStudentIndexDataLoader {

    @Inject private AsyncBusinessCacheServiceImpl asyncBusinessCacheService;

    @Inject
    private BusinessStudentServiceImpl businessStudentService;
    @Inject
    private MissionLoaderImpl missionLoader;

    @Override
    protected StudentIndexDataContext doProcess(StudentIndexDataContext context) {
        StudentDetail student = context.getStudent();

        List<Mission.Location> missionLocations = null;

        if (asyncBusinessCacheService.StudentParentRewardCacheManager_showCard(student.getId()).take()) {
            missionLocations = missionLoader.loadStudentMissions(student.getId()).originalLocationsAsList();
            Mission.Location location = missionLocations.stream()
                    .filter(t -> t.getState() == MissionState.ONGOING)
                    .sorted((o1, o2) -> Long.compare(o2.getMissionTime(), o1.getMissionTime()))
                    .findFirst()
                    .orElse(null);
            Mission mission = missionLoader.loadMission(location == null ? null : location.getId());
            if (mission != null) {
                Map<String, Object> content = new HashMap<>();
                content.put("wish", mission.formalizeWishContent());
                content.put("mission", mission.formalizeMissionContent());
                content.put("totalCount", mission.getTotalCount());
                content.put("finishCount", mission.getFinishCount());
                context.getParam().put("parentReward", content);
            }
        }

//        http://project.17zuoye.net/redmine/issues/16180
        //是否创建10学豆奖励
        if (!businessStudentService.isCurrentMonthIntegralMissionArranged(student.getId())) {
            if (missionLocations == null) {
                missionLocations = missionLoader.loadStudentMissions(student.getId()).originalLocationsAsList();
            }
            //取学生的愿望
//                当月许愿数量：1.当月，2.学生许愿（包括两类：1.学生许愿mission为wish，2.家长把wish改为ongoing）
            long wishCount = missionLocations.stream()
                    .filter(t -> MonthRange.current().contains(t.getCreateTime())) //当月
                    .filter(t -> t.getState() == MissionState.WISH ||  //学生许愿，mission_state=wish
                            Math.abs(t.getCreateTime() - t.getMissionTime()) > 1000)   //学生许愿后，家长把mission_state从wish改为ongoing（因为学生许愿时mission_state=wish,mission_datetime=null,家长改为ongoing后，mission_datetime才有值，大于1s表示是从wish改为ongoing的愿望）
                    .count();
            //本月没有愿望，则显示该卡
            if (wishCount == 0) {
                context.getParam().put("showprr", true);
            }
        }
        return context;
    }
}
