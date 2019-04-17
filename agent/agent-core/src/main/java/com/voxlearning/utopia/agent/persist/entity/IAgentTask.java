package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.utopia.api.constant.AgentTaskCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Jia HuanYin
 * @since 2015/11/26
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class IAgentTask implements Serializable {
    private static final long serialVersionUID = 396863444604614712L;

    private String taskId;
    private Long createrId;
    private String createrName;
    private String title;
    private String content;
    private Date endTime;
    private AgentTaskCategory category;
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;


}
