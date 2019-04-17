package com.voxlearning.utopia.service.crm.api.constants;

import lombok.Getter;

/**
 * 申请类型
 *
 * @author song.wang
 * @date 2016/12/29
 */
public enum ApplyType {
    AGENT_MODIFY_DICT_SCHOOL(1, "字典表调整"),
    AGENT_MATERIAL_APPLY(2, "物料申请"),
    AGENT_PRODUCT_FEEDBACK(3, "产品反馈"),
    ADMIN_SEND_APP_PUSH(4, "AppPush消息"),
    AGENT_UNIFIED_EXAM_APPLY(5,"统考申请"),
    AGENT_DATA_REPORT_APPLY(6, "大数据报告申请");

    @Getter private final Integer type;
    @Getter private final String desc;

    ApplyType(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static ApplyType nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }

}
