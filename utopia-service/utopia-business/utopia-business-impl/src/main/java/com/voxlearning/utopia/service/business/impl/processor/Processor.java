package com.voxlearning.utopia.service.business.impl.processor;

import com.voxlearning.utopia.business.api.context.AbstractContext;

/**
 * @author peng.zhang.a
 * @since 16-10-12
 */
public interface Processor<T extends AbstractContext<T>> {

    T process(T context);
}
