package com.voxlearning.utopia.service.piclisten.impl.support;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xinxin
 * @since 7/13/17.
 */
@Getter
@Setter
public class FltrpOrderInfo implements Serializable {
    private static final long serialVersionUID = -793797231233563557L;

    private String appId;
    private String orderSn;
    private String mobile;
    private String bookEditionCode;
    private String effectivePeriod;
    private String phone;
    private String sign;

    public Map<String, String> toMap() {
        Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotBlank(appId)) {
            params.put("appId", appId);
        }
        if (StringUtils.isNotBlank(orderSn)) {
            params.put("orderSn", orderSn);
        }
        if (StringUtils.isNotBlank(mobile)) {
            params.put("mobile", mobile);
        }
        if (StringUtils.isNotBlank(bookEditionCode)) {
            params.put("bookEditionCode", bookEditionCode);
        }
        if (StringUtils.isNotBlank(effectivePeriod)) {
            params.put("effectivePeriod", effectivePeriod);
        }
        if (StringUtils.isNotBlank(phone)) {
            params.put("phone", phone);
        }
        if (StringUtils.isNotBlank(sign)) {
            params.put("sign", sign);
        }
        return params;
    }
}
