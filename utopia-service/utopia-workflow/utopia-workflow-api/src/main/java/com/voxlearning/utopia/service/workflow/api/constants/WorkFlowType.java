package com.voxlearning.utopia.service.workflow.api.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 定义工作流的类型
 * @author fugui.chang
 * @since 2016/12/28
 */
public enum  WorkFlowType {
    AGENT_MODIFY_DICT_SCHOOL(1, "字典表调整", "agent_modify_dict_school_apply"),
    AGENT_MATERIAL_APPLY(2, "物料申请", "agent_material_apply"),
    AGENT_PRODUCT_FEEDBACK(3, "产品反馈", "agent_product_feedback"),
    ADMIN_SEND_APP_PUSH(4, "发送AppPush消息", "admin_send_app_push"),
    ADMIN_WECHAT_NOTICE(5, "发送微信模板消息", "admin_wechat_batch_send"),
    AGENT_UNIFIED_EXAM_APPLY(6,"统考申请","agent_unified_exam_apply"),
    AGENT_DATA_REPORT_APPLY(7,"大数据报告申请","agent_data_report_apply")
    ;

    @Getter private final Integer type;
    @Getter private final String desc;
    @Getter private final String workflowName;

    WorkFlowType(Integer type, String desc, String workflowName){
        this.type = type;
        this.desc = desc;
        this.workflowName = workflowName;
    }

    private static final Map<Integer, WorkFlowType> workflowTypeMap;

    static {
        workflowTypeMap = new HashMap<>();
        for (WorkFlowType type : values()) {
            workflowTypeMap.put(type.getType(), type);
        }
    }

    public static WorkFlowType typeOf(Integer id) {
        if (id == null) {
            return null;
        }
        return workflowTypeMap.get(id);
    }

    public static WorkFlowType nameOf(String value){
        try {
            return valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

}
