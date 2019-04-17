package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.view.BaseTodayIntoSchoolView;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Created by yaguang.wang
 * on 2017/10/9.
 */
@Named
public class IntoSchoolEarlyWarningHandler extends SpringContainerSupport {
    @Inject private WorkRecordService workRecordService;
    @Inject private AgentNotifyService agentNotifyService;
    @Inject private BaseOrgService baseOrgService;

    public void handle() {
        Calendar c = Calendar.getInstance();
        String now = DateUtils.dateToString(c.getTime(), "yyyyMMdd");
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            return;
        }
        List<AgentGroup> regionGroups = baseOrgService.getAgentGroupByRole(AgentGroupRoleType.Region);
        regionGroups.forEach((AgentGroup p) -> {
            Set<Long> bdIds = baseOrgService.loadGroupUserByGroupId(p.getId(), AgentRoleType.BusinessDeveloper);
            Map<Long, List<AgentUserSchool>> bdSchools = baseOrgService.getUserSchoolByUsers(bdIds);
            Set<Long> validBd = new HashSet<>();
            bdSchools.forEach((k, v) -> {
                Long schoolSize = v.stream().filter(s1 -> s1.getSchoolLevel() == SchoolLevel.JUNIOR.getLevel()).map(AgentUserSchool::getSchoolId).count();
                if(schoolSize != 0){
                    validBd.add(k);
                }
            });
            Map<Long, BaseTodayIntoSchoolView> bdView = workRecordService.loadBdTodayIntoSchoolByBdIds(validBd, now);
            Integer noReachBdCount = validBd.size() - bdView.size();
            Map<Long, BaseTodayIntoSchoolView> isNotReachBd = new HashMap<>();
            bdView.forEach((k, v) -> {
                if (!v.isReach()) {
                    isNotReachBd.put(k, v);
                }
            });
            noReachBdCount += isNotReachBd.size();
            List<AgentGroupUser> groupUsers = baseOrgService.getGroupUserByGroup(p.getId());
            AgentGroupUser groupUser = groupUsers.stream().filter(p1 -> p1.getUserRoleType() == AgentRoleType.Region).findFirst().orElse(null);
            if (groupUser != null && noReachBdCount != 0) {
                AgentUser regionUser = baseOrgService.getUser(groupUser.getUserId());
                agentNotifyService.sendNotify(AgentNotifyType.INTO_SCHOOL_REACH_WARNING.getType(),
                        "进校未达标", "当日进校未达标专员共计" + noReachBdCount + "人",
                        Collections.singletonList(regionUser.getId()), "/mobile/into_school/no_reach.vpage?date=" + now);
            }
        });

    }
}
