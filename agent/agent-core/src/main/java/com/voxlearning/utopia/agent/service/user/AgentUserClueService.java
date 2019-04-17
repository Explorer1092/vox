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

package com.voxlearning.utopia.agent.service.user;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.persist.AgentUserCluePersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentUserClue;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.user.api.entities.School;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;

/**
 * @author shiwei.liao
 * @since 2015/7/22.
 */

@Named
public class AgentUserClueService extends AbstractAgentService {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private AgentUserCluePersistence agentUserCluePersistence;

    public List<AgentUserClue> getAgentUserClueBySchoolIdList(List<Long> schoolIds) {
        return agentUserCluePersistence.getAgentUserClueBySchoolIdList(schoolIds);
    }

    public AgentUserClue getAgentUserClueBySchoolId(Long schoolId) {
        return agentUserCluePersistence.getAgentUserClueBySchoolId(schoolId);
    }

    public MapMessage saveAgentUserClue(AgentUserClue newClue) {
        Long schoolId = newClue.getSchoolId();
        AgentUserClue clue = agentUserCluePersistence.getAgentUserClueBySchoolId(schoolId);
        Date date = new Date();
        if (clue != null) {
            //update
            clue.setUserId(newClue.getUserId());
            clue.setUserName(newClue.getUserName());
            clue.setSchoolAddress(newClue.getSchoolAddress());
            clue.setKeyContactName(newClue.getKeyContactName());
            clue.setKeyContactPhone(newClue.getKeyContactPhone());
            clue.setTotalStudentCount(newClue.getTotalStudentCount());
            clue.setHighLevelCount(newClue.getHighLevelCount());
            clue.setLowLevelCount(newClue.getLowLevelCount());
            clue.setEnglishTeacherCount(newClue.getEnglishTeacherCount());
            clue.setMathTeacherCount(newClue.getMathTeacherCount());
            clue.setChineseTeacherCount(newClue.getChineseTeacherCount());
            clue.setProvideType(newClue.getProvideType());
            clue.setProvidePhone(newClue.getProvidePhone());
            clue.setUpdateDatetime(date);
            AgentUserClue modified = agentUserCluePersistence.replace(clue);
            if (modified == null) {
                return MapMessage.errorMessage();
            }
            return MapMessage.successMessage();
        } else {
            //insert
            School school = raikouSystem.loadSchool(schoolId);
            Region region = raikouSystem.loadRegion(school.getRegionCode());
            newClue.setRegionCode(school.getRegionCode());
            newClue.setSchoolName(school.getCname());
            newClue.setRegionName(region.getName());
            newClue.setCreateDatetime(date);
            newClue.setUpdateDatetime(date);
            agentUserCluePersistence.insert(newClue);
            return MapMessage.successMessage();
        }

    }

}
