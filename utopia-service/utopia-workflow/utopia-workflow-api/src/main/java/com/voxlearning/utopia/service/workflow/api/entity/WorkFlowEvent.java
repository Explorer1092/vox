package com.voxlearning.utopia.service.workflow.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author fugui.chang
 * @since 2016/11/8
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WorkFlowEvent implements Serializable {
    private static final long serialVersionUID = -3811104117745752347L;
    private String name;
    private String status;
    private String mqmsg;
}
