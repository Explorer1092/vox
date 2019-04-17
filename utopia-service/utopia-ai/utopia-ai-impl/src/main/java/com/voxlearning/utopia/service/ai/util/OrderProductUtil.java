package com.voxlearning.utopia.service.ai.util;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;

import java.util.Map;

/**
 * @author guangqing
 * @since 2018/8/15
 */
public final class OrderProductUtil {


    /**
     *  grade
     */
    public static int parseIntegerTypeFieldFromAttr(OrderProduct product, String fieldName) {
        if (StringUtils.isBlank(fieldName)) {
            return 0;
        }
        if (product == null || StringUtils.isBlank(product.getAttributes())) {
            return 0;
        }
        Map<String, Object> map = JsonUtils.fromJson(product.getAttributes());
        if (MapUtils.isEmpty(map)) {
            return 0;
        }
        return SafeConverter.toInt(map.get(fieldName));
    }


    public static boolean isShortProduct(OrderProduct product) {
        if (product == null || StringUtils.isBlank(product.getAttributes())) {
            return false;
        }
        Map<String, Object> map = JsonUtils.fromJson(product.getAttributes());
        if (MapUtils.isEmpty(map)) {
            return false;
        }
        return Boolean.TRUE.equals(SafeConverter.toBoolean(map.get("short")));
    }
}
