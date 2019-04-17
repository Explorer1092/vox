package com.voxlearning.utopia.service.push.impl.jiguang;

import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.service.push.impl.base.BizTemplate;
import com.voxlearning.utopia.service.push.impl.invoker.HttpPoolingRequestExecutor;
import com.voxlearning.utopia.service.push.impl.support.VendorPushConfiguration;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 极光处理模板
 *
 * @author Wenlong Meng
 * @since Mar 13, 2019
 */
public class JGBizTemplate extends BizTemplate<MapMessage> {

    private String command;
    private Map<String, Object> params;
    private String response;

    /**
     * 构建指定业务极光模板类
     *
     * @param biz
     * @param command
     * @param params
     */
    public JGBizTemplate(String biz, String command, Map<String, Object> params){
        super(biz);
        this.command = command;
        this.params = params;
    }

    /**
     * 业务处理
     *
     * @return
     */
    @Override
    protected MapMessage process() {
        HttpPoolingRequestExecutor.InternalHttpRequestExecutor executor = HttpPoolingRequestExecutor.get();
        AlpsHttpResponse response = executor.delete(url())
                    .headers(auth())
                    .connectionTimeout(5000)
                    .socketTimeout(15000)
                    .params(params)
                    .execute();
        this.response = response.getResponseString();
        return MapMessage.successMessage().set("response", response);
    }

    protected String url(){
        JGHttpConfig jgHttpConfig = JGConstants.JG_URLS.get(command);
        return jgHttpConfig.getUrl();
    }

    /**
     * 授权
     *
     * @return
     */
    private Header[] auth(){
        String jgAuth = VendorPushConfiguration.getJPushAuthentication(biz);
        return new Header[]{new BasicHeader("Authorization", jgAuth), new BasicHeader("Content-Type", "application/json")};
    }

    /**
     * 后处理，紧跟{@link #onError(Throwable)} or {@link #onSuccess()} 后
     */
    @Override
    protected void afterProcess() {
        LoggerUtils.info("push_jg_slow_log", biz, command, this.response, watch.elapsed(TimeUnit.MILLISECONDS));
    }

}
