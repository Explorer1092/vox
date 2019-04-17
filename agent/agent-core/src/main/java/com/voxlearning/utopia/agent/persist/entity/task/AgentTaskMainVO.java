package com.voxlearning.utopia.agent.persist.entity.task;

import com.voxlearning.utopia.agent.constants.AgentTaskType;
import lombok.Getter;
import lombok.Setter;
import org.omg.PortableInterceptor.INACTIVE;

import java.io.Serializable;
import java.util.Date;

/**
 * 任务vo
 *
 * @author deliang.che
 * @since  2018-05-30
 **/
@Getter
@Setter
public class AgentTaskMainVO implements Serializable {
    private String mainTaskId;          //主任务ID
    private String title;               //标题
    private String comment;             //备注
    private AgentTaskType taskType;     //任务类型
    private Date endTime;               //截止时间
    private Integer allNum;             //子任务总数
    private Integer finishedNum;        //子任务完成数
    private String status;              //状态（finished：完成  unfinished：未完成）

}
