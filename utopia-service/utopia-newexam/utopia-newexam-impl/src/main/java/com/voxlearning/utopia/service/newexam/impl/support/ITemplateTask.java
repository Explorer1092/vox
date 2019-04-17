package com.voxlearning.utopia.service.newexam.impl.support;


import java.io.Serializable;

/**
 * Created by tanguohong on 2016/3/10.
 *
 */
public interface ITemplateTask<E extends Serializable> {
    void execute(E context);
}
