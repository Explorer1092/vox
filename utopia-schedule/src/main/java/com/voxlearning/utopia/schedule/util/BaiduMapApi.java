package com.voxlearning.utopia.schedule.util;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 后台请求百度API
 * Created by alex on 2016/5/27.
 */
@Slf4j
public class BaiduMapApi {

    private static final String QUERY_PRECISE_LOCATION = "http://api.map.baidu.com/geoconv/v1/?coords={},{}&from=1&to=5&ak=stpdH3wKubAUFfjRZ8ELoN2A";
    private static final String QUERY_URL = "http://api.map.baidu.com/geocoder?stpdH3wKubAUFfjRZ8ELoN2A" +
            "&callback=renderReverse&coord_type=wgs84&location={},{}&output=json";

    /**
     * @param y 纬度
     * @param x 经度
     * @return
     */
    public static MapMessage getAddress(String y, String x, String coordinateType) {
        try {

            //对x, y 坐标进行校正 wgs84 GPS类型
            if (Objects.equals(coordinateType, "wgs84")) {
                MapMessage preciseLocationMap = getPreciseLocation(x, y);
                if (!preciseLocationMap.isSuccess() || preciseLocationMap.get("x") == null || preciseLocationMap.get("y") == null) {
                    return MapMessage.errorMessage("获取位置信息失败，地点坐标{},{}", x, y);
                }
                x = ConversionUtils.toString(preciseLocationMap.get("x"));
                y = ConversionUtils.toString(preciseLocationMap.get("y"));
            }

            String accessUrl = StringUtils.formatMessage(QUERY_URL, y, x);
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(accessUrl).execute();
            if (response == null || response.getStatusCode() != 200) {
                log.error("failed to get address from baidu with response:" + response);
                return MapMessage.errorMessage("获取位置信息失败，地点坐标{},{}", x, y);
            }

            Map<String, Object> apiResult = JsonUtils.fromJson(response.getResponseString());
            if (apiResult == null || !apiResult.containsKey("result") || !(apiResult.get("result") instanceof Map)) {
                log.error("failed to get address from baidu with response:" + response);
                return MapMessage.errorMessage("获取位置信息失败，地点坐标{},{}", x, y);
            }

            String address = ConversionUtils.toString(((Map) apiResult.get("result")).get("formatted_address"));

            return MapMessage.successMessage().add("address", address);
        } catch (Exception e) {
            log.error("获取位置信息失败，地点坐标{},{}", x, y, e);
            return MapMessage.errorMessage("获取位置信息失败，地点坐标{},{}", x, y);
        }
    }

    /**
     * 获取更精确的经纬度信息
     *
     * @param x GPS经度
     * @param y GPS纬度
     * @return 调整后的百度 x, y
     */
    public static MapMessage getPreciseLocation(String x, String y) {
        try {
            String accessUrl = StringUtils.formatMessage(QUERY_PRECISE_LOCATION, x, y);
            return getLocation(x, y, accessUrl);
        } catch (Exception e) {
            log.error("获取位置信息失败，地点坐标{},{}", x, y, e);
            return MapMessage.errorMessage();
        }
    }


    public static MapMessage getLocation(String x, String y, String accessUrl) {
        Map<String, Double> retMap = new HashMap<>();
        try {
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(accessUrl).execute();
            if (response == null || response.getStatusCode() != 200) {
                log.error("failed to get precise location from baidu with response:" + response);
                return MapMessage.errorMessage();
            }

            Map<String, Object> apiResult = JsonUtils.fromJson(response.getResponseString());
            if (apiResult == null || !apiResult.containsKey("result") || !(apiResult.get("result") instanceof List)) {
                log.error("failed to get precise location from baidu with response:" + response.getResponseString());
                return MapMessage.errorMessage();
            }
            List result = (List) apiResult.get("result");
            if(CollectionUtils.isEmpty(result)){
                return MapMessage.errorMessage();
            }
            Object retObj = (result).get(0);
            if(retObj != null && retObj instanceof  Map){
                retMap = (Map<String, Double>)retObj;
            }else{
                return MapMessage.errorMessage();
            }
            return MapMessage.successMessage().add("x", retMap.get("x")).add("y", retMap.get("y"));

        } catch (Exception e) {
            log.error("获取位置信息失败，地点坐标{},{}", x, y, e);
            return MapMessage.errorMessage();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println(BaiduMapApi.getAddress("40.00123611111111", "116.48690833333333", "bd"));
    }
}
