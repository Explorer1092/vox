package com.voxlearning.utopia.service.parent.homework.impl.template;

import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;

/**
 * Template接口
 *
 * @author Wenlong Meng
 * @since Feb 20.2019
 */
public interface TemplateProcessor {

    //local variables

    /**
     * process hc
     *
     * @param hc args
     * @return result
     */
    void process(HomeworkContext hc);
}
