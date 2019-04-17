package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 注册认证的数据
 * Created by yaguang.wang on 2016/9/21.
 */
@Getter
@Setter
@NoArgsConstructor
public class AgentRegAndAuthData {
    private Long targetId;
    private String targetName;
    private String type;
    private Boolean isGroupManager;
    private Long thisMonthRegNum;
    private Long thisMonthAuthNum;
    private Long yesterdayRegNum;
    private Long yesterdayAuthNum;
}
