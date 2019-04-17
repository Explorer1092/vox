package com.voxlearning.utopia.service.parent.homework.impl.template;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.Processors;

import java.util.function.Consumer;

/**
 * 作业Template抽象类
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-16
 */
public class AbstractProcessorTemplate extends SpringContainerSupport {

    //local variables
    protected Consumer processor;

    /**
     * 初始化作业流程
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Processors annotation = getClass().getAnnotation(Processors.class);
        for (Class<? extends Consumer> beanClass : annotation.value()) {
            Consumer loader = applicationContext.getBean(beanClass);
            if (loader == null) {
                throw new IllegalStateException(beanClass.getName() + " not found");
            }
            if(processor == null){
                processor = loader;
            }else{
                processor = processor.andThen(loader);
            }
            logger.debug("load ", beanClass.getName());
        }
    }
}
