package com.voxlearning.utopia.service.afenti.impl.util;

import com.voxlearning.utopia.service.afenti.api.context.AbstractAfentiContext;

/**
 * @author Ruib
 * @since 2016/7/11
 */
public interface IAfentiTask<E extends AbstractAfentiContext<E>> {
    void execute(E context);
}
