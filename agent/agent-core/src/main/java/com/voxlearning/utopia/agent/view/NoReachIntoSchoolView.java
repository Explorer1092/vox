package com.voxlearning.utopia.agent.view;

import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by yaguang.wang
 * on 2017/10/9.
 */
@Getter
@Setter
@NoArgsConstructor
public class NoReachIntoSchoolView extends BaseTodayIntoSchoolView {
    private String groupName;   // 分区名
    private String bdName;      // 专员名

    public static NoReachIntoSchoolView createNoReachIntoSchoolView(AgentGroup group, AgentUser bdUser, BaseTodayIntoSchoolView baseTodayIntoSchoolView) {
        if (group == null || bdUser == null) {
            return null;
        }
        NoReachIntoSchoolView view = new NoReachIntoSchoolView();
        view.setBdName(bdUser.getRealName());
        view.setGroupName(group.getGroupName());
        if (baseTodayIntoSchoolView != null) {
            view.setIntoSchoolCount(baseTodayIntoSchoolView.getIntoSchoolCount());
            view.setVisitTeacherAvg(baseTodayIntoSchoolView.getVisitTeacherAvg());
        }
        return view;
    }
}
