package com.voxlearning.utopia.service.workflow.api.entity;

import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowProcessUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author fugui.chang
 * @since 2016/11/8
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WorkFlowStatus implements Serializable{
    private static final long serialVersionUID = 2598911573288835263L;

    private String name;
    private String[] processor;
    private List<WorkFlowProcessUser> processUserList;
    private Map<String,WorkFlowEvent> eventMap;
    private Boolean aftertreatment;
}
