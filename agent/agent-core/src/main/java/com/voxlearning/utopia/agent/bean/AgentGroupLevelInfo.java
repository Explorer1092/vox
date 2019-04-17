package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/6/23.
 */
@Getter
@Setter
public class AgentGroupLevelInfo implements Serializable{
    private static final long serialVersionUID = 3280314426069654539L;
    private Integer dplLevId;
    private String dpLName;
}
