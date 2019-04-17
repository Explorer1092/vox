package com.voxlearning.utopia.service.newhomework.impl.support;

import com.voxlearning.utopia.service.newhomework.api.context.ITemplateContext;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/13
 */
public interface ITemplateTask<E extends ITemplateContext> {
    void execute(E context);
}
