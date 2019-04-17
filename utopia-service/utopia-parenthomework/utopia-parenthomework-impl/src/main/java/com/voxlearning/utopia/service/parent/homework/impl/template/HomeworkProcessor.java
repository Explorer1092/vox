package com.voxlearning.utopia.service.parent.homework.impl.template;

import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;

import java.util.function.Consumer;

/**
 * 处理器接口
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-15
 */
public interface HomeworkProcessor extends Consumer<HomeworkContext> {

    /**
     * process hc
     *
     * @param hc args
     * @return result
     */
    void process(HomeworkContext hc);

    /**
     * process hc
     *
     * @param hc args
     * @return result
     */
    default void accept(HomeworkContext hc) {
        if(hc.getMapMessage() == null){
            process(hc);
        }
    }
}
