package com.voxlearning.utopia.agent.service.partner.outerfetch;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.http.client.execute.GET;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.core.HttpClientType;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 基于http实现的客户端
 *
 * @author xiaolei.li
 * @version 2018/8/6
 */
public abstract class AbstractHttpClient {

    public static final String PAPER_DEV_HOST = "http://honeycomb.test.17zuoye.net";
    public static final String PAPER_STAGING_HOST = "http://honeycomb.staging.17zuoye.net";
    public static final String PAPER_PRODUCT_HOST = "http://honeycomb.oaloft.com";

    /**
     * 服务注册表
     */
    public static final Table<Mode, Service, String> SERVICE_REGISTRY = HashBasedTable.create();


    static {
        // 创建试卷
        SERVICE_REGISTRY.put(Mode.DEVELOPMENT, Service.FANS_QUERY, PAPER_DEV_HOST + "/v1/agent/fans_list.vpage");
        SERVICE_REGISTRY.put(Mode.TEST, Service.FANS_QUERY, PAPER_DEV_HOST + "/v1/agent/fans_list.vpage");
        SERVICE_REGISTRY.put(Mode.STAGING, Service.FANS_QUERY, PAPER_STAGING_HOST + "/v1/agent/fans_list.vpage");
        SERVICE_REGISTRY.put(Mode.PRODUCTION, Service.FANS_QUERY, PAPER_PRODUCT_HOST + "/v1/agent/fans_list.vpage");
    }

    @AllArgsConstructor
    public enum Service {

        FANS_QUERY("查询粉丝", RequestMethod.POST, 10 * 1000, 30 * 1000) {
            @Override
            public String getUrl(Mode mode) {
                return SERVICE_REGISTRY.get(mode, this);
            }
        }
        ;

        public final String desc;
        public final RequestMethod method;
        public final int connect_timeout;
        public final int socket_timeout;

        public abstract String getUrl(Mode mode);
    }

    /**
     * 根据服务枚举构建一个可用的POST链接
     *
     * @param service 服务枚举
     * @return post链接
     */
    public static POST build(Service service) {
        HttpRequestExecutor executor = HttpRequestExecutor.instance(HttpClientType.POOLING);
        POST post = executor.post(service.getUrl(RuntimeMode.current()));
        post.socketTimeout(service.socket_timeout);
        post.connectionTimeout(service.connect_timeout);
        return post;
    }

    /**
     * 根据服务枚举构建一个可用的GET链接
     * @param service 服务枚举
     * @return get链接
     */
    public static GET buildGet(Service service){
        GET get = HttpRequestExecutor.defaultInstance().get(service.getUrl(RuntimeMode.current()));
        get.connectionTimeout(service.connect_timeout);
        get.socketTimeout(service.socket_timeout);
        return get;
    }
}