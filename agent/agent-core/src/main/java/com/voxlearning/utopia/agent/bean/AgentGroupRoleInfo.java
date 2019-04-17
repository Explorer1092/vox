package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by yaguang.wang on 2016/6/23.
 */
@Getter
@Setter
public class AgentGroupRoleInfo implements Serializable{
    private static final long serialVersionUID = 6580806096708137103L;
    private Integer uRoleId;
    private String uRoleName;
}
