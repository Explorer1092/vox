package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.service.productfeedback.AgentProductFeedbackService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentProductFeedback;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;

/**
 * Created by yaguang.wang
 * on 2017/9/21.
 */
@Named
public class ProductFeedbackHandler extends SpringContainerSupport {
    @Inject
    private AgentNotifyService agentNotifyService;
    @Inject
    private AgentProductFeedbackService agentProductFeedbackService;

    public void handle(Long workflowId) {
        AgentProductFeedback dataReportApply = agentProductFeedbackService.loadByWorkflowId(workflowId);
        if (dataReportApply == null) {
            return;
        }
        String content = dataReportApply.getContent();// 反馈的内容
        Boolean onlineFlag = SafeConverter.toBoolean(dataReportApply.getOnlineFlag());   // 是否已经上线
        Long agentUser = SafeConverter.toLong(dataReportApply.getAccount());
        AgentProductFeedbackType feedbackType = dataReportApply.getFeedbackType();
        String message = "您提交的反馈\"" + feedbackType.getDesc() + "——" + (StringUtils.isNoneBlank(content) && content.length() > 20 ? content.substring(0, 20) + "..." : content) + "\"" + (onlineFlag ? "已经上线啦" : "收到新的回复啦");
        agentNotifyService.sendNotify(AgentNotifyType.PRODUCT_FEEDBACK_NOTICE.getType(), "产品反馈", message, Collections.singleton(agentUser), "/mobile/feedback/view/feedbackinfo.vpage?feedbackId=" + dataReportApply.getId());
    }
}
