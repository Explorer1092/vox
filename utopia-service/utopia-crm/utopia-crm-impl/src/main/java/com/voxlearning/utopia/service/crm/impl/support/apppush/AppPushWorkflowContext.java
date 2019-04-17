package com.voxlearning.utopia.service.crm.impl.support.apppush;

import com.voxlearning.utopia.service.crm.api.entities.crm.AppPushWfMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 简易工作流的上下文环境
 */
public class AppPushWorkflowContext {

    @Getter private AppPushWfMessage workflowMessage;
    @Getter @Setter
    private List<Long> targetUserIds;

    public AppPushWorkflowContext(AppPushWfMessage message) {
        this.workflowMessage = message;
    }



}
