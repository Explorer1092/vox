package com.voxlearning.utopia.service.parent.homework.impl.util;

/**
 * prod接口动态代理factory
 *
 * @author Wenlong Meng
 * @since Feb 28, 2019
 */
public class ProdApiProxyFactory {

    /**
     * 获取代理
     *
     * @return
     */
    public static <T> T proxy(Class<T> api){
        return new ProdApiProxy<>(api).proxy();
    }

}
