package com.voxlearning.utopia.agent.view;


import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.agent.constants.AgentKpiType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author song.wang
 * @date 2018/2/12
 */
@Getter
@Setter
public class AgentKpiBudgetView {
    private Integer month;
    private Integer groupOrUser;
    private Long groupId;
    private String groupName;
    private Long userId;
    private String userName;
    private List<AgentKpiBudgetItem> kpiBudgetList;
    private boolean confirmed;

}
