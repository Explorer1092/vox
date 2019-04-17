package com.voxlearning.utopia.service.business.impl.processor;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.business.api.context.AbstractContext;

/**
 * @author peng.zhang.a
 * @since 16-10-12
 */
abstract public class AbstractExecuteTask<T extends AbstractContext<T>> extends SpringContainerSupport implements ExecuteTask<T> {
}
