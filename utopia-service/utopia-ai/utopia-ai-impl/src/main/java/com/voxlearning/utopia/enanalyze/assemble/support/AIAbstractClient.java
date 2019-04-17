package com.voxlearning.utopia.enanalyze.assemble.support;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.http.client.execute.AbstractHttpRequestBody;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.core.HttpClientType;
import lombok.AllArgsConstructor;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.InitializingBean;

/**
 * ai接口客户端
 * 做了一些基础工作
 * <ul>
 * <li>服务接口定义</li>
 * <li>http超时设定</li>
 * <li>http超时设定</li>
 * <li>post请求headers处理</li>
 * </ul>
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
public class AIAbstractClient implements InitializingBean {

    /**
     * httpclient
     */
    private HttpRequestExecutor executor = null;

    /**
     * 服务定义
     */
    @AllArgsConstructor
    enum ServiceDefinition {

        AI_OCR("ai组的光学字符识别服务", "http://ac-eng.17zuoye.com/ocr", "http://ac-eng.17zuoye.com/ocr", 60 * 1000, 60 * 1000),
        AI_NLP("ai组的自然语言识别服务", "http://ac-eng.17zuoye.com/nlp", "http://ac-eng.17zuoye.com/nlp", 60 * 1000, 60 * 1000),;

        public final String DESC;
        public final String URL_QA;
        public final String URL_PRO;
        public final int CONNECTION_TIMEOUT;
        public final int SOCKET_TIMEOUT;
    }

    /**
     * 获取一个post请求
     *
     * @param sd 服务定义
     * @return post请求
     */
    POST post(ServiceDefinition sd) {
        String url;
        if (RuntimeMode.current().ge(Mode.STAGING)) {
            url = sd.URL_PRO;
        } else {
            url = sd.URL_QA;
        }
        POST post = executor.post(url);
        setHeaders(post);
        post.connectionTimeout(sd.CONNECTION_TIMEOUT);
        post.socketTimeout(sd.SOCKET_TIMEOUT);
        return post;
    }

    /**
     * 给我一个请求，我来帮你搞定头部信息
     *
     * @param request 请求
     */
    private void setHeaders(AbstractHttpRequestBody request) {
        request.headers(
                new BasicHeader("appkey", "7f095d84e4c6727abf789df3dce8e675"),
                new BasicHeader("device-id", "test"),
                new BasicHeader("session-id", "test"),
                new BasicHeader("sys", "postman"),
                new BasicHeader("protocol", "http"));
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        executor = HttpRequestExecutor.instance(HttpClientType.POOLING);
    }
}
