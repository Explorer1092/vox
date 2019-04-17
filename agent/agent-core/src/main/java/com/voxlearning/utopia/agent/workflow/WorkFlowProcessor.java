package com.voxlearning.utopia.agent.workflow;

/**
 * Created by Alex on 14-8-8.
 */
public interface WorkFlowProcessor {

    /**
     * 同意处理
     *
     * @param context 工作流上下文
     */
    void agree(WorkFlowContext context);

    /**
     * 拒绝处理
     *
     * @param context 工作流上下文
     */
    void reject(WorkFlowContext context);

}
