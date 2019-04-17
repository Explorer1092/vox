package com.voxlearning.utopia.service.mizar.impl.support;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.common.DigestSPI;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.mizar.api.utils.talkfun.TalkFunCommand;
import com.voxlearning.utopia.service.mizar.talkfun.TalkFunUtils;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class TalkFunHttpUtils extends TalkFunUtils {
    private static final Logger logger = LoggerFactory.getLogger(TalkFunHttpUtils.class);

    public static MapMessage post(Map<String, Object> paramMap, TalkFunCommand command, Mode runtime) {
        return post(paramMap, command, runtime, false);
    }

    public static MapMessage post(Map<String, Object> paramMap, TalkFunCommand command, Mode runtime, boolean backup) {
        String url = generateUrl(paramMap, command, runtime, backup);
        logger.debug("Post Talk-Fun Url: \n{}", url);
        AlpsHttpResponse response = HttpRequestExecutor.instance(HttpClientType.POOLING)
                .post(url)
                .socketTimeout(REQUEST_TIMEOUT)
                .execute();
        if (response.hasHttpClientException()) {
            logger.error("Failed post Talk-Fun Api, op={}, url={}", command.getDesc(), url, response.getHttpClientException());
            return MapMessage.errorMessage("在欢拓平台创建课程失败：" + response.getHttpClientExceptionMessage());
        }
        return parseReturnMsg(response.getResponseString(Charset.forName(DEFAULT_CHARSET)));
    }

    public static MapMessage get(Map<String, Object> paramMap, TalkFunCommand command, Mode runtime) {
        return get(paramMap, command, runtime, false);
    }

    public static MapMessage get(Map<String, Object> paramMap, TalkFunCommand command, Mode runtime, boolean backup) {
        String url = generateUrl(paramMap, command, runtime, backup);
        logger.debug("Post Talk-Fun Url: \n{}", url);
        AlpsHttpResponse response = HttpRequestExecutor.instance(HttpClientType.POOLING)
                .get(url)
                .socketTimeout(REQUEST_TIMEOUT)
                .execute();
        if (response.hasHttpClientException()) {
            logger.error("Failed get Talk-Fun Api, op={}, url={}", command.getDesc(), url, response.getHttpClientException());
            return MapMessage.errorMessage("在欢拓平台创建课程失败：" + response.getHttpClientExceptionMessage());
        }
        return parseReturnMsg(response.getResponseString(Charset.forName(DEFAULT_CHARSET)));
    }

    public static String generateUrl(Map<String, Object> paramMap, TalkFunCommand command, Mode runtime) {
        Objects.requireNonNull(paramMap, "请求参数不能为空!");
        Objects.requireNonNull(runtime, "未知的运行环境");
        try {
            // 将参数拼成json字符串
            String json = JsonUtils.toJson(paramMap);
            long timestamp = System.currentTimeMillis() / 1000;
            String cmd = command.getCmd();
            String params = safeUrlEncode(json);
            // 根据key升序排列
            Map<String, String> queryMap = new TreeMap<>();
            queryMap.put("openID", openID(runtime)); // 合作方唯一标识码
            queryMap.put("timestamp", String.valueOf(timestamp)); // 当前Unix时间戳
            queryMap.put("cmd", cmd); // 调用接口的名称
            queryMap.put("format", DEFAULT_FORMAT); // 返回参数格式，默认json
            queryMap.put("ver", DEFAULT_VERSION); // 协议版本号，默认1.0
            queryMap.put("params", params); // 接口参数
            // 拼接参数
            StringBuilder sb = new StringBuilder();
            queryMap.entrySet().forEach(e -> sb.append(e.getKey()).append(e.getValue()));
            sb.append(openToken(runtime));
            String sign = DigestSPI.getInstance().md5Hex(sb.toString().getBytes(DEFAULT_CHARSET)); // MD5
            logger.debug("TalkFun MD5 Signature:\n origin={} \n sign={}", sb.toString(), sign);
            queryMap.put("sign", sign); // 签名
            return UrlUtils.buildUrlQuery(TALK_FUN_API_URL, queryMap);
        } catch (UnsupportedEncodingException ex) {
            throw new UtopiaRuntimeException("Failed to generate TalkFun generateUrl", ex);
        }
    }

    public static String generateUrl(Map<String, Object> paramMap, TalkFunCommand command, Mode runtime, boolean backup) {
        Objects.requireNonNull(paramMap, "请求参数不能为空!");
        Objects.requireNonNull(runtime, "未知的运行环境");
        try {
            // 将参数拼成json字符串
            String json = JsonUtils.toJson(paramMap);
            long timestamp = System.currentTimeMillis() / 1000;
            String cmd = command.getCmd();
            String params = safeUrlEncode(json);
            // 根据key升序排列
            Map<String, String> queryMap = new TreeMap<>();
            queryMap.put("openID", backup ? OPEN_ID_BACKUP : openID(runtime)); // 合作方唯一标识码
            queryMap.put("timestamp", String.valueOf(timestamp)); // 当前Unix时间戳
            queryMap.put("cmd", cmd); // 调用接口的名称
            queryMap.put("format", DEFAULT_FORMAT); // 返回参数格式，默认json
            queryMap.put("ver", DEFAULT_VERSION); // 协议版本号，默认1.0
            queryMap.put("params", params); // 接口参数
            // 拼接参数
            StringBuilder sb = new StringBuilder();
            queryMap.entrySet().forEach(e -> sb.append(e.getKey()).append(e.getValue()));
            sb.append(backup ? OPEN_TOKEN_BACKUP : openToken(runtime));
            String sign = DigestSPI.getInstance().md5Hex(sb.toString().getBytes(DEFAULT_CHARSET)); // MD5
            logger.debug("TalkFun MD5 Signature:\n origin={} \n sign={}", sb.toString(), sign);
            queryMap.put("sign", sign); // 签名
            return UrlUtils.buildUrlQuery(TALK_FUN_API_URL, queryMap);
        } catch (UnsupportedEncodingException ex) {
            throw new UtopiaRuntimeException("Failed to generate TalkFun generateUrl", ex);
        }
    }

    /**
     * 解析请求返回的json串
     */
    private static MapMessage parseReturnMsg(String json) {
        if (StringUtils.isBlank(json)) {
            return MapMessage.errorMessage("无效的参数");
        }
        try {
            Map<String, Object> resMap = JsonUtils.convertJsonObjectToMap(json);
            MapMessage retMsg = MapMessage.of(resMap);
            // 去掉data参数
            if (SafeConverter.toInt(resMap.get("code")) != 0) {
                return MapMessage.errorMessage(SafeConverter.toString(resMap.get("msg")))
                        .add("code", resMap.get("code"));
            }
            retMsg.setSuccess(true);
            logger.debug("TalkFun Response is: \n{}", JsonUtils.toJsonPretty(retMsg));
            return retMsg;
        } catch (Exception ex) {
            logger.error("Failed to parse TalkFun response result", ex);
            return MapMessage.errorMessage("结果解析失败：" + StringUtils.firstLine(ex.getMessage()));
        }
    }
}
