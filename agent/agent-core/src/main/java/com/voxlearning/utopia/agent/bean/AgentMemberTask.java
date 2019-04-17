package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Jia HuanYin
 * @since 2015/11/27
 */
@Getter
@Setter
public class AgentMemberTask implements Serializable {
    private static final long serialVersionUID = 4065075998678299586L;

    private Long memberId;
    private String memberName;
    private boolean hasFollowing;

    public AgentMemberTask(Long memberId, String memberName, boolean hasFollowing) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.hasFollowing = hasFollowing;
    }
}
