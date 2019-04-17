package com.voxlearning.utopia.service.ai.impl.service.processor;

import com.voxlearning.utopia.service.ai.context.AbstractAIContext;

public interface IAITask<E extends AbstractAIContext<E>> {
    void execute(E context);
}

