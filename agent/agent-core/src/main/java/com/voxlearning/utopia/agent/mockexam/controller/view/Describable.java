package com.voxlearning.utopia.agent.mockexam.controller.view;

/**
 * @author xiaolei.li
 * @version 2018/8/16
 */
public interface Describable<E extends Enum> {

    /**
     * 获取名称
     *
     * @param enums
     * @return 名称
     */
    String getDesc(E enums);
}
