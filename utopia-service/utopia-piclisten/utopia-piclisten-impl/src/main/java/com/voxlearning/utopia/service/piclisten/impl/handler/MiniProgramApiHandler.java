package com.voxlearning.utopia.service.piclisten.impl.handler;

import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.core.concurrent.CachedExecutorService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.utopia.api.enums.MiniProgramApi;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.piclisten.api.MiniProgramCheckService;
import com.voxlearning.utopia.service.piclisten.consumer.cache.manager.MiniProgramCacheManager;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramType;
import com.voxlearning.utopia.service.wechat.api.entities.UserMiniProgramRef;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author RA
 */

@Named
@Slf4j
public class MiniProgramApiHandler extends SpringContainerSupport {


    @Inject
    private MiniProgramCacheManager miniProgramCacheManager;

    @Inject
    private WechatLoaderClient wechatLoaderClient;

    @Inject
    private UserLoaderClient userLoaderClient;

    @Inject
    private MiniProgramCheckService miniProgramCheckService;


    private ExecutorService executorService = CachedExecutorService.getExecutorService();


    private static final String MESSAGE_CHECK_REMIND_TEMPLATE_ID = "8M3KAqjB3J1cOw5WZlPr49gqFofz_o7QPq3GRoVgCfA";

    private final HttpRequestExecutor httpClient = HttpRequestExecutor.instance(HttpClientType.POOLING);


    public String getAccessToken() {
        return getAccessToken(false);
    }


    public String getAccessToken(boolean force) {
        String key = MiniProgramType.PICLISTEN.getAccessTokenCacheKey();
        IRedisCommands redisCommands = miniProgramCacheManager.getRedisCommands();
        Object obj = redisCommands.sync().getRedisStringCommands().get(key);
        if (force) {
            obj = null;
        }
        if (obj == null) {
            // Fetch new access token
            MiniProgramType miniProgramType = MiniProgramType.PICLISTEN;

            String url = MiniProgramApi.ACCESS_TOKEN.url(ProductConfig.get(miniProgramType.getAppId()), ProductConfig.get(miniProgramType.getAppSecret()));
            Map<String, Object> ret = get(url);
            if (!blank(ret.get("access_token"))) {

                String accessToken = SafeConverter.toString(ret.get("access_token"));
                Long expiresIn = SafeConverter.toLong(ret.get("expires_in"));

                redisCommands.sync().getRedisStringCommands().set(key, accessToken);
                redisCommands.sync().getRedisKeyCommands().expire(key, expiresIn);
                return accessToken;
            } else {
                log.error("Get access_token error, errcode: {}, errmsg: {}", ret.get("errcode"), ret.get("errmsg"));
                return "";
            }

        } else {
            return String.valueOf(obj);
        }
    }


    public void sendCheckRemindNotice(Long pid, Long uid, String formId) {
        Map<String, Object> param = new HashMap<>();

        UserMiniProgramRef ref = wechatLoaderClient.loadMiniProgramUserRef(pid, MiniProgramType.PICLISTEN);
        if (ref == null) {
            log.warn("Send check remind notice failed, user {} not exist.", pid);
            return;
        }
        String openId = ref.getOpenId();
        param.put("touser", openId);
        param.put("template_id", MESSAGE_CHECK_REMIND_TEMPLATE_ID);
        param.put("page", "pages/index/main");
        param.put("form_id", formId);
        param.put("emphasis_keyword", "");

        Map<String, Object> data = new HashMap<>();

//        Integer checked = 1;
//        MiniProgramCheck mpc = miniProgramCheckService.loadByUid(uid);
//        if (mpc != null) {
//            checked=mpc.getChecked();
//        }

        Map<String, Object> rmap = miniProgramCacheManager.getUserDayPlanData(pid, uid);

//        String planMinutes = "";
        String remindTime = "";
        if (rmap != null) {
//            planMinutes = String.valueOf(rmap.get("plan_minutes"));
            remindTime=String.valueOf(rmap.get("remind_time"));
        }

        HashMap<String, Object> v1 = new HashMap<>();
        v1.put("value", "好的英语能力，来自于每一天的积累");
        HashMap<String, Object> v2 = new HashMap<>();
        v2.put("value", remindTime);

        data.put("keyword1", v1);
        data.put("keyword2", v2);

        param.put("data", data);

        String url = MiniProgramApi.MESSAGE_SEND.url(getAccessToken());

        Map<String, Object> ret = postJson(url, param);

        if (ret.get("errcode").equals(0)) {
            log.info("[MPMAPI] Send mini program check remind notice successful,pid: {},openId: {}, formId: {}", pid, openId, formId);
        } else {
            log.error("[MPMAPI] Send mini program check remind notice failed,pid: {},openId: {}, formId: {},error: {}", pid, openId, formId, ret.get("errmsg"));
        }
    }

    public void asyncSendCheckRemindNotice(Long pid, Long uid, String formId) {
        executorService.submit(() -> {
            sendCheckRemindNotice(pid, uid, formId);
        });
    }


    public Map<String, Object> postJson(String url, Map<String, Object> param) {
        String resp = httpClient.post(url).contentType("application/json").json(param).execute().getResponseString();
        if (resp == null) {
            return errorMap(url);
        }
        return JsonUtils.fromJson(resp);
    }

    private Map<String, Object> get(String url) {
        String resp = httpClient.get(url).execute().getResponseString();
        if (resp == null) {
            return errorMap(url);
        }
        return JsonUtils.fromJson(resp);
    }

    private Map<String, Object> errorMap(String url) {
        log.error("Request weixin api {} failed", url);
        Map<String, Object> tmp = new HashMap<>();
        tmp.put("errcode", -10000000);
        tmp.put("errmsg", "network exception");
        return tmp;
    }

    private boolean blank(Object src) {
        if (src == null) {
            return true;
        }
        return StringUtils.isBlank(String.valueOf(src));
    }

}
