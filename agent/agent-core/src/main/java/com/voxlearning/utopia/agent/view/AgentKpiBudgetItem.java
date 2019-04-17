package com.voxlearning.utopia.agent.view;

import lombok.Getter;
import lombok.Setter;

/**
 * AgentKpiBudgetItem
 *
 * @author song.wang
 * @date 2018/2/12
 */
@Getter
@Setter
public class AgentKpiBudgetItem {
    private Integer kpiType;
    private String kpiTypeDesc;
    private int budget;
    private boolean confirmed;
}
