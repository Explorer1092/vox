package com.voxlearning.utopia.agent.bean;

import com.voxlearning.utopia.entity.agent.AgentTaskDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/5/10.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrmTaskMessage implements  Serializable {
    private static final long serialVersionUID = 6261663653637640203L;

    private List<AgentTaskDetail> taskDetails;      //需要发给CRM的任务详情
    private String type;                            //任务的类型
}
