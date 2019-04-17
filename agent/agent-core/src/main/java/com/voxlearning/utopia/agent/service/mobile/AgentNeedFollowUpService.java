package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.dao.mongo.AgentNeedFollowUpDao;
import com.voxlearning.utopia.agent.persist.entity.AgentNeedFollowUp;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AgentNeedFollowUpService
 *
 * @author song.wang
 * @date 2016/7/29
 */
@Named
public class AgentNeedFollowUpService extends AbstractAgentService {

    @Inject
    private AgentNeedFollowUpDao agentNeedFollowUpDao;
    @Inject
    private BaseOrgService baseOrgService;

    public Map<Integer, List<AgentNeedFollowUp>> getNeedFollowUpList(Long userId, Integer startDay, Integer endDay){

        List<Long> schoolIdList = baseOrgService.getManagedSchoolList(userId);
        if(CollectionUtils.isEmpty(schoolIdList)){
            return Collections.emptyMap();
        }
        List<Integer> everyDayList = DayUtils.getEveryDays(startDay, endDay);

        List<AgentNeedFollowUp> followUpList = new ArrayList<>();
        Map<Long, List<AgentNeedFollowUp>> dayFollowUpMap;
        for(Integer day : everyDayList){
            dayFollowUpMap = agentNeedFollowUpDao.findBySchoolList(schoolIdList, day);
            if(MapUtils.isNotEmpty(dayFollowUpMap)){
                dayFollowUpMap.forEach((k, v) -> followUpList.addAll(v));
            }
        }
        return followUpList.stream().collect(Collectors.groupingBy(AgentNeedFollowUp::getDay, Collectors.toList()));
    }
}
