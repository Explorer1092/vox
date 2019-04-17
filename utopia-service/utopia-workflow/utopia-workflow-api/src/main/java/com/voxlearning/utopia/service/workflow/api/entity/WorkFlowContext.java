package com.voxlearning.utopia.service.workflow.api.entity;

import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowProcessUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author fugui.chang
 * @since 2016/11/7
 */
@NoArgsConstructor
@AllArgsConstructor
public class WorkFlowContext implements Serializable{
    private static final long serialVersionUID = 1760967991668567996L;

    @Getter @Setter private WorkFlowRecord workFlowRecord;
    @Getter @Setter private String workFlowName; //工作流的名字,要与wfconfig.properties中的定义一致

    @Getter @Setter private String sourceApp;                    // 处理源， admin / agent / mizar
    @Getter @Setter private String processorAccount;             // 处理者账号
    @Getter @Setter private String processorName;                // 处理者姓名
    @Getter @Setter private String processNotes;                 // 处理结果备注

    @Getter @Setter private List<WorkFlowProcessUser> processUserList; // 动态指定后续审核人员列表
}
