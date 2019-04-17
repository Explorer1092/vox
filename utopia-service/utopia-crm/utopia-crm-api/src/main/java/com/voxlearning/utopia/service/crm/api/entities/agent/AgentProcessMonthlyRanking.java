package com.voxlearning.utopia.service.crm.api.entities.agent;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 过程月榜
 * @author deliang.che
 * @since 2018/8/4
 */
@Getter
@Setter
public class AgentProcessMonthlyRanking implements Serializable{

    private static final long serialVersionUID = -4473090001752213385L;

    private Long userId;
    private String userName;
    private String userAvatar;      //用户头像
    private Long groupId;
    private String groupName;
    private Integer workType; //工作类型 1：工作量 2：进校量 3：见师量
    private Double workNum;   //工作数量（工作量、进校量、见师量）
    private Integer ranking;        //排名
}
