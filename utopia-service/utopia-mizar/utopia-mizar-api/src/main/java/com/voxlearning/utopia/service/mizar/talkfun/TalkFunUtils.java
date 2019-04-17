package com.voxlearning.utopia.service.mizar.talkfun;


import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Range;
import com.voxlearning.alps.spi.common.DigestSPI;
import com.voxlearning.alps.spi.common.JsonStringDeserializer;
import com.voxlearning.alps.spi.common.JsonStringSerializer;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import org.slf4j.Logger;

import javax.activation.UnsupportedDataTypeException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 欢拓云直播 工具类
 * <p>
 * 协议说明
 * 1、使用HTTP协议进行信息交互，字符编码统一采用UTF-8
 * 2、除非特殊说明，同时支持GET和POST两种参数传递方式
 * 3、除非特殊说明，返回信息支持JSON格式
 * 4、除了sign外，其余所有请求参数值都需要进行URL编码
 * <p>
 * 当前使用测试账号 ga***ng/ga******st
 *
 * @author yuechen.wang
 * @date 2017/01/09
 */
public class TalkFunUtils {

    private static final Logger logger = LoggerFactory.getLogger(TalkFunUtils.class);

    public static final Range<Integer> PAGE_RANGE = Range.between(1, 1000);
    public static final Range<Integer> SIZE_RANGE = Range.between(1, 100);

    // 角色枚举
    public static final String ROLE_USER = TalkFunConstants.ROLE_USER;
    public static final String ROLE_ADMIN = TalkFunConstants.ROLE_ADMIN;
    public static final String ROLE_SUPER_ADMIN = TalkFunConstants.ROLE_SUPER_ADMIN;
    public static final String ROLE_GUEST = TalkFunConstants.ROLE_GUEST;
    private static final List<String> VALID_ROLE = Arrays.asList(ROLE_USER, ROLE_ADMIN, ROLE_SUPER_ADMIN, ROLE_GUEST);

    /**
     * 除非特殊说明，接口地址统一为：http://api.talk-fun.com/portal.php
     */
    protected static final String TALK_FUN_API_URL = "http://api.talk-fun.com/portal.php";

    // 由欢拓分配提供的唯一标识
    protected static final String OPEN_ID_TEST = "11431";
    protected static final String OPEN_ID_PROD = "11439";
    protected static final String OPEN_ID_BACKUP = "11386";

    // 调用欢拓api时需要使用到的密钥
    protected static final String OPEN_TOKEN_TEST = "304c84bea5fbb80e14c8a2daf6ccc3db";
    protected static final String OPEN_TOKEN_PROD = "1fab2e4d549b73fe2a52b4c48d73c8d8";
    protected static final String OPEN_TOKEN_BACKUP = "632e1c5b51fe9729b4fa63ee1350e328";

    protected static final String DEFAULT_FORMAT = "json";
    protected static final String DEFAULT_VERSION = "1.0";
    protected static final String DEFAULT_CHARSET = "UTF-8";

    protected static final Integer REQUEST_TIMEOUT = 3 * 1000;

    /**
     * 根据参数组装好请求的URL地址
     * <p>
     * sign生成规则可以分为4个步骤：
     * 1、把【其它】所有参数按key升序排序。
     * 2、把key和它对应的value拼接成一个字符串。按步骤1中顺序，把所有键值对字符串拼接成一个字符串。
     * 经过json_encode，再url_encode所得
     * 3、把分配给的openToken拼接在第2步骤得到的字符串后面。
     * 4、计算第3步骤字符串的md5值，使用md5值的16进制字符串作为sign的值。
     *
     * @param paramMap 参数列表
     * @param sign     根据参数计算的数字签名
     */
    public static void validateSign(Map<String, String> paramMap, String sign, Mode runtime) {
        if (MapUtils.isEmpty(paramMap) || StringUtils.isBlank(sign) || runtime == null) {
            throw new IllegalArgumentException("无效的参数");
        }
        String reqOpenID = paramMap.get("openID");
        // 校验这个openID是不是合法的openID
        if (!openID(runtime).equals(reqOpenID)) {
            throw new IllegalArgumentException("无效的openID");
        }
        // 将参数拼成json字符串
        // 拼接参数
        StringBuilder sb = new StringBuilder();
        paramMap.entrySet().forEach(e -> sb.append(e.getKey()).append(e.getValue()));
        sb.append(openToken(runtime));
        String reqSign = "";
        try {
            reqSign = DigestSPI.getInstance().md5Hex(sb.toString().getBytes(DEFAULT_CHARSET));
        } catch (UnsupportedEncodingException ex) {
            logger.error("Sign Valid Failed, paramMap={}, sign={}, runtime={}", JsonStringSerializer.getInstance().serialize(paramMap), sign, runtime, ex);
        }
        if (!sign.equals(reqSign)) throw new IllegalArgumentException("数字签名校验失败");
    }

    /**
     * 将参数进行 URL Encode
     */
    public static void appendParam(Map<String, Object> paramMap, String key, Object value) {
        if (paramMap == null) {
            return;
        }
        paramMap.put(key, value);
    }

    /**
     * 校验用户身份，用户身份(user/admin/spadmin/guest，分别对应普通用户/管理员/超级管理员/游客)
     */
    public static boolean validRole(String role) {
        return VALID_ROLE.contains(role);
    }

    /**
     * 简单处理返回的JSON，并将其转换为实体
     *
     * @param json   返回json
     * @param mapper 需要转换成目标实体类
     */
    public static <T> T parseReturnData(String json, Class<T> mapper) {
        if (StringUtils.isBlank(json) || mapper == null) {
            return null;
        }
        try {
            // 先将返回的参数 Url Decode，之后在 Json Decode
            json = URLDecoder.decode(json, DEFAULT_CHARSET); // 去掉无用空格
            // 处理 传进来的Class
            TKFieldScanner.TKDocumentAnalysis analysis = new TKFieldScanner(TKFieldNameResolver.getInstance()).scan(mapper);
            // 根据Map的key去映射生成实体
            return parseReturnData(json, mapper, analysis);
        } catch (Exception ex) {
            logger.error("Failed to parse TalkFun return data", ex);
            return null;
        }
    }

    /**
     * 简单处理返回的JSON，并将其转换为实体
     *
     * @param json   返回json
     * @param mapper 需要转换成目标实体类
     */
    public static <T> List<T> parseReturnList(String json, Class<T> mapper) {
        if (StringUtils.isBlank(json) || mapper == null) {
            return Collections.emptyList();
        }
        try {
            // 先将返回的参数 Url Decode，之后在 Json Decode
            json = URLDecoder.decode(json, DEFAULT_CHARSET); // 去掉无用空格
            List<Map> data = JsonStringDeserializer.getInstance().deserializeList(json, Map.class);
            if (CollectionUtils.isEmpty(data)) {
                return Collections.emptyList();
            }
            // 处理 传进来的Class
            TKFieldScanner.TKDocumentAnalysis analysis = new TKFieldScanner(TKFieldNameResolver.getInstance()).scan(mapper);
            return data.stream().map(j -> parseReturnData(j, mapper, analysis)).filter(Objects::nonNull).collect(Collectors.toList());
        } catch (Exception ex) {
            logger.error("Failed to parse TalkFun return data", ex);
            return null;
        }
    }

    public static MapMessage successResponse() {
        return MapMessage.successMessage().add("code", 0);
    }

    public static MapMessage errorResponse(String errorMsg) {
        return MapMessage.errorMessage().add("code", 400).add("msg", errorMsg);
    }

    public static String openID(Mode runtime) {
        return runtime.ge(Mode.STAGING) ? OPEN_ID_PROD : OPEN_ID_TEST;
    }

    public static String openToken(Mode runtime) {
        return runtime.ge(Mode.STAGING) ? OPEN_TOKEN_PROD : OPEN_TOKEN_TEST;
    }

    /**
     * 欢拓课程直播
     * FIXME Directly copy form TalkFun JDK
     */
    public static String courseLiveUrl(Map<String, Object> paramMap, Mode runtime, boolean backup) throws Exception {
        Map<String, Object> params = new HashMap<>(paramMap);
        params.put("openID", backup ? OPEN_ID_BACKUP : openID(runtime));
        params.put("timestamp", String.valueOf(new Date().getTime()));
        params.put("expire", 3600);
        params.put("options", JsonStringSerializer.getInstance().serialize(paramMap.remove("options")));
        params.put("sign", generateSign(params, runtime, backup));
        String accessAuth = JsonStringSerializer.getInstance().serialize(params);
        accessAuth = Base64.getUrlEncoder().encodeToString(accessAuth.getBytes("UTF-8"));
        return "http://open.talk-fun.com/room.php?accessAuth=" + accessAuth;
    }

    /**
     * 欢拓课程回放
     * FIXME Directly copy form TalkFun JDK
     */
    public static String coursePlaybackUrl(Map<String, Object> paramMap, Mode runtime, boolean backup) throws Exception {
        Map<String, Object> params = new HashMap<>(paramMap);
        params.put("openID", backup ? OPEN_ID_BACKUP : openID(runtime));
        params.put("timestamp", String.valueOf(new Date().getTime()));
        params.put("expire", 3600);
        params.put("options", JsonStringSerializer.getInstance().serialize(paramMap.remove("options")));
        params.put("sign", generateSign(params, runtime, backup));
        String accessAuth = JsonStringSerializer.getInstance().serialize(params);
        accessAuth = Base64.getUrlEncoder().encodeToString(accessAuth.getBytes("UTF-8"));
        return "http://open.talk-fun.com/player.php?accessAuth=" + accessAuth;
    }

    public static String generateAccessKey(Map<String, Object> paramMap, Mode runtime, boolean backup) throws Exception {
        Map<String, Object> params = new HashMap<>(paramMap);
        params.put("openID", backup ? OPEN_ID_BACKUP : openID(runtime));
        params.put("timestamp", String.valueOf(new Date().getTime()));
        params.put("expire", 3600);
        params.put("options", JsonStringSerializer.getInstance().serialize(paramMap.remove("options")));
        params.put("sign", generateSign(params, runtime, backup));
        String accessKey = JsonStringSerializer.getInstance().serialize(params);
        accessKey = Base64.getUrlEncoder().encodeToString(accessKey.getBytes("UTF-8"));
        return accessKey;
    }

    /**
     * 构造欢拓云签名
     * FIXME Directly copy form TalkFun JDK
     */
    private static String generateSign(Map<String, Object> params, Mode runtime, boolean backup) {
        params.remove("sign");
        Object[] array = params.keySet().toArray();
        Arrays.sort(array);
        String keyStr = "";
        for (Object anArray : array) {
            String key = anArray.toString();
            keyStr += key + params.get(key);
        }
        String openToken = backup ? OPEN_TOKEN_BACKUP : openToken(runtime);
        keyStr += openToken;
        return DigestSPI.getInstance().md5Hex(keyStr);

    }

    public static String safeUrlEncode(String value) {
        if (value == null) {
            return null;
        }
        try {
            return URLEncoder.encode(value, DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new UtopiaRuntimeException("Failed to generate Url Encode of TalkFun param, value=" + value, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T parseReturnData(Object obj, Class<T> mapper, TKFieldScanner.TKDocumentAnalysis analysis) {
        if (obj == null || mapper == null || analysis == null) {
            return null;
        }
        try {
            Map<String, Object> dataMap;
            // already json
            if (obj instanceof String) {
                dataMap = JsonStringDeserializer.getInstance().deserialize(obj.toString());
            } else if (obj instanceof Map) {
                dataMap = (Map<String, Object>) obj;
            } else {
                throw new UnsupportedDataTypeException("Invalid Data Type, type must be String or Map");
            }
            // 根据Map的key去映射生成实体
            T bean = mapper.newInstance();
            // 先处理时间戳字段
            analysis.timeFields.forEach(f -> {
                long st = SafeConverter.toLong(dataMap.get(f.getName()));
                Date time = st == 0 ? null : new Date(st * 1000);
                f.setValue(bean, time);
            });
            // 处理普通字段
            analysis.normalFields.forEach(f -> f.setValue(bean, dataMap.get(f.getName())));
            return bean;
        } catch (Exception ex) {
            logger.error("Failed to parse TalkFun return data", ex);
            return null;
        }
    }
}
