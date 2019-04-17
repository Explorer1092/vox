package com.voxlearning.utopia.agent.workflow;

/**
 * Created by Alex on 14-8-8.
 */
public interface WorkFlowProcessorFactory {

    WorkFlowProcessor getProcessor(WorkFlowContext context);
}
