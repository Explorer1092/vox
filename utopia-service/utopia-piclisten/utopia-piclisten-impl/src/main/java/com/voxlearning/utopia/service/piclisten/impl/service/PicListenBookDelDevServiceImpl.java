package com.voxlearning.utopia.service.piclisten.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.api.entity.CommonConfig;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.piclisten.api.PicListenBookDelDevService;
import com.voxlearning.utopia.service.piclisten.api.entity.PicListenBookUserDev;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/1/12
 */
@Named
@ExposeService(interfaceClass = PicListenBookDelDevService.class)
public class PicListenBookDelDevServiceImpl extends SpringContainerSupport implements PicListenBookDelDevService {


    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    private static final String PEP_QUERY_DEV_URL_KEY = "PEP_QUERY_DEV_URL_KEY";
    private static final String PEP_DEL_DEV_URL_KEY = "PEP_DEL_DEV_URL_KEY";
    private static final String PEP_GET_TOKEN_URL_KEY = "PEP_GET_TOKEN_URL_KEY";
    private static final String CONFIG_PICLISTEN_RENJIAO_APPID = "piclisten.renjiao.appid";

    /**
     * 人教--查询用户设备数量
     *
     * @param userId
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<PicListenBookUserDev.DevInfo> queryPicDevList(Long userId) {
        if (userId == 0L) {
            return Collections.emptyList();
        }
        String token = getToken(userId);
        if (StringUtils.isBlank(token)) {
            return Collections.emptyList();
        }
        String queryUrl = generateUrl(PEP_QUERY_DEV_URL_KEY);
        if (StringUtils.isBlank(queryUrl)) {
            return Collections.emptyList();
        }
        String platformKey = ProductConfig.get(CONFIG_PICLISTEN_RENJIAO_APPID);
        queryUrl = StringUtils.formatMessage(queryUrl, platformKey, userId);
        AlpsHttpResponse execute = HttpRequestExecutor.defaultInstance().post(queryUrl).addParameter("access_token", token)
                .contentType("application/x-www-form-urlencoded").execute();
        Map<String, Object> responseMap = JsonUtils.fromJson(execute.getResponseString());
        if (MapUtils.isEmpty(responseMap)) {
            return Collections.emptyList();
        }
        if ("success".equals(SafeConverter.toString(responseMap.get("errmsg")))) {
            List<Map<String, Object>> devList = (List<Map<String, Object>>) responseMap.getOrDefault("devlist", null);
            if (CollectionUtils.isEmpty(devList)) {
                return Collections.emptyList();
            }
            List<PicListenBookUserDev.DevInfo> devInfos = new ArrayList<>();
            devList.forEach(e -> {
                PicListenBookUserDev.DevInfo devInfo = new PicListenBookUserDev.DevInfo();
                devInfo.setDevId(SafeConverter.toString(e.get("dev_id")));
                devInfo.setDevName(SafeConverter.toString(e.get("dev_name")));
                devInfo.setCreateTime(SafeConverter.toString(e.get("create_time")));
                devInfos.add(devInfo);
            });
            return devInfos;
        } else {
            logger.error("Query user RJ devList fail, userId {}, error {}", userId, SafeConverter.toString(responseMap.get("errmsg")));
            return Collections.emptyList();
        }

    }

    /**
     * 人教--移除用户设备
     *
     * @param userId
     * @param devIdList
     */
    @Override
    public MapMessage delPicDev(Long userId, List<String> devIdList) {
        if (userId == 0L || CollectionUtils.isEmpty(devIdList)) {
            return MapMessage.errorMessage();
        }
        String token = getToken(userId);
        if (StringUtils.isBlank(token)) {
            return MapMessage.errorMessage("获取token失败");
        }
        String delUrl = generateUrl(PEP_DEL_DEV_URL_KEY);
        if (StringUtils.isBlank(delUrl)) {
            return MapMessage.errorMessage();
        }
        String devIds = StringUtils.join(devIdList.toArray(), ",");
        if (StringUtils.isBlank(devIds)) {
            return MapMessage.errorMessage();
        }
        String platformKey = ProductConfig.get(CONFIG_PICLISTEN_RENJIAO_APPID);
        delUrl = StringUtils.formatMessage(delUrl, platformKey, userId);
        AlpsHttpResponse execute = HttpRequestExecutor.defaultInstance().post(delUrl)
                .addParameter("access_token", token)
                .addParameter("device_id", "sy") // 人家说不为空 传随意值
                .addParameter("device_ids", devIds)
                .contentType("application/x-www-form-urlencoded")
                .execute();
        Map<String, Object> map = JsonUtils.fromJson(execute.getResponseString());
        if ("success".equals(SafeConverter.toString(map.get("errmsg")))) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage(SafeConverter.toString(map.get("errmsg")));
    }


    private String getToken(Long userId) {
        String url = generateUrl(PEP_GET_TOKEN_URL_KEY);
        if (StringUtils.isBlank(url)) {
            return "";
        }
        url = StringUtils.formatMessage(url, ProductConfig.get(CONFIG_PICLISTEN_RENJIAO_APPID), userId);
        AlpsHttpResponse execute = HttpRequestExecutor.defaultInstance().post(url).execute();
        Map<String, Object> map = JsonUtils.fromJson(execute.getResponseString());
        if ("success".equals(SafeConverter.toString(map.get("errmsg")))) {
            return SafeConverter.toString(map.get("access_token"));
        } else {
            logger.error("Get RJ access_token faild, userId {}, code {}", userId, SafeConverter.toString(map.get("errmsg")));
            return "";
        }
    }

    private String generateUrl(String configKey) {
        if (StringUtils.isBlank(configKey)) {
            return "";
        }
        CommonConfig commonConfig = commonConfigServiceClient
                .getCommonConfigBuffer()
                .findByCategoryName(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType())
                .stream()
                .filter(e -> SafeConverter.toInt(e.getConfigRegionCode()) == 0)
                .filter(e -> configKey.equals(e.getConfigKeyName()))
                .findFirst().orElse(null);
        if (commonConfig == null || StringUtils.isBlank(commonConfig.getConfigKeyValue())) {
            return "";
        }
        return commonConfig.getConfigKeyValue();
    }
}
