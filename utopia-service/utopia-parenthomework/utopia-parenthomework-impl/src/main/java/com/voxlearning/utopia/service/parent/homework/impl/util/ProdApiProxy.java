package com.voxlearning.utopia.service.parent.homework.impl.util;

import com.google.common.base.Joiner;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import org.apache.http.message.BasicHeader;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * prod接口动态代理
 *
 * @author Wenlong Meng
 * @since Feb 28, 2019
 */
public class ProdApiProxy<T> implements InvocationHandler {

    private final Class<T> api;
    private static final String PROD_HYDRA = "http://10.6.3.241:1889/?group=alps-hydra-production";

    /**
     * 构建指定api的代理
     *
     * @param api
     */
    ProdApiProxy(Class<T> api) {
        this.api = api;
    }

    /**
     * 获取代理
     *
     * @return
     */
    public T proxy(){
        return (T)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{api}, this);
    }

    /**
     * process
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String url = hydraUrl(api, method.getName());
        String reqParams = JsonUtils.toJson(MapUtils.m("paramValues", args));

        String response = HttpRequestExecutor.defaultInstance()
                .post(url)
                .headers(new BasicHeader("Content-Type", "application/json"))
                .json(reqParams)
                .execute().getResponseString();
        LoggerUtils.debug(Joiner.on(".").join(api.getSimpleName(),method.getName()), url, reqParams, response);
        if(method.getReturnType().isAssignableFrom(List.class)){
            return JsonUtils.fromJson(response, method.getReturnType());
        }else {
            return JsonUtils.fromJson(response, method.getReturnType());
        }
    }

    /**
     * 构建prod hydra url
     *
     * @param api
     * @param method
     * @return
             */
    public String hydraUrl(Class<T> api, String method){
        String version = api.getAnnotation(ServiceVersion.class).version();
        String service = api.getName();
        return String.format(hydraUrl()+"&method=%s&version=%s&service=%s", method, version, service);
    }

    /**
     * 获取hydra url
     *
     * @return
     */
    private String hydraUrl(){
        return System.getProperty("ProdApiProxy.hydraUrl", PROD_HYDRA);
    }

}
