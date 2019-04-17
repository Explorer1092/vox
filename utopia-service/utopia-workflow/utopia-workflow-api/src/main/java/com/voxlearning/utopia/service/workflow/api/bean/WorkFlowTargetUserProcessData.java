package com.voxlearning.utopia.service.workflow.api.bean;

import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 *
 *
 * @author song.wang
 * @date 2017/1/5
 */
@Getter
@Setter
public class WorkFlowTargetUserProcessData implements Serializable{
    private static final long serialVersionUID = -6040817206997592612L;

    private WorkFlowRecord workFlowRecord;
    private WorkFlowProcessHistory processHistory;
}
