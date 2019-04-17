package com.voxlearning.washington.data.utils;

/**
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
