package com.voxlearning.utopia.service.parent.homework.impl.template.base;

/**
 * Template接口
 *
 * @author Wenlong Meng
 * @since Feb 20.2019
 */
public interface ITemplate<A, R> {

    /**
     * process
     *
     * @param a args
     * @return r
     */
    R process(A a);
}
