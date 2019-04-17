package com.voxlearning.utopia.service.parent.homework.impl.template.base;

import java.util.function.Consumer;

/**
 * Process接口
 *
 * @author Wenlong Meng
 * @since Feb 25.2019
 */
public interface IProcessor<C extends BaseContext> extends Consumer<C> {

    /**
     * process
     *
     * @param c context see {@link BaseContext}
     * @return r
     */
    void process(C c);

    /**
     * exec
     *
     * @param c args
     * @return result
     */
    default void accept(C c) {
        if(!c.isTerminate()){
            process(c);
        }
    }
}
