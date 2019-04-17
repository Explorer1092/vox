package com.voxlearning.utopia.enanalyze.mq;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 消费者标记
 *
 * @author xiaolei.li
 * @version 2018/7/27
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ConsumerLabel {

    /**
     * 主题
     *
     * @return
     */
    Topic topic();

}
