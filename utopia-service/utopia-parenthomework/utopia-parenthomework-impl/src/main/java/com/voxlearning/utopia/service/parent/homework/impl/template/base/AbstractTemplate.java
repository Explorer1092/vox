package com.voxlearning.utopia.service.parent.homework.impl.template.base;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.core.utils.LoggerUtils;

import java.util.function.Consumer;

/**
 * Template抽象类
 *
 * @author Wenlong Meng
 * @since Feb 25, 2019
 */
public abstract class AbstractTemplate<C extends BaseContext> extends SpringContainerSupport implements ITemplate<C, MapMessage> {

    //local variables
    protected Consumer processor;

    /**
     * process
     *
     * @param c
     * @return
     */
    @Override
    public MapMessage process(C c) {

        try{
            // 流程处理
            processor.accept(c);
            // 封装数据
            MapMessage mapMessage = c.getMapMessage();
            if (mapMessage == null) {
                c.setMapMessage(MapMessage.successMessage());
                c.setMapMessage(mapMessage);
            }
            LoggerUtils.debug(this.getClass().getSimpleName()+ ".process", c.getMapMessage());
        }catch (Exception e){
            logger.error("{}", JsonUtils.toJson(c), e);
            c.setMapMessage(MapMessage.errorMessage());
        }
        LoggerUtils.debug("debug", c);
        return c.getMapMessage();
    }

    /**
     * 初始化作业流程
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Processors annotation = getClass().getAnnotation(Processors.class);
        if(annotation==null){
            return;
        }
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
