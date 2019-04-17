package com.voxlearning.utopia.service.piclisten.impl.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/1/18
 * 沪教点读机订单相关字段
 */
@Getter
@Setter
public class SephOrderInfo implements Serializable {
    private static final long serialVersionUID = -3841273661986859347L;

    @JsonProperty("UserID")
    private String userId;
    @JsonProperty("RTime")
    private String rTime;
    @JsonProperty("OrderID")
    private String orderId;
    @JsonProperty("TextBookID")
    private String textBookId;
    @JsonProperty("Price")
    private BigDecimal price;
    @JsonProperty("Cycle")
    private String cycle;

    @Getter
    @Setter
    public class ChangeBookInfo extends SephOrderInfo {
        private static final long serialVersionUID = 2887473928052772370L;
        private String newCourseId;

        public Map<String, Object> toMap() {
            Map<String, Object> params = new HashMap<>();
            if (StringUtils.isNotBlank(userId)) {
                params.put("UID", userId);
            }
            if (StringUtils.isNotBlank(rTime)) {
                params.put("RTime", rTime);
            }
            if (StringUtils.isNotBlank(textBookId)) {
                params.put("OldCourseID", textBookId);
            }
            if (StringUtils.isNotBlank(newCourseId)) {
                params.put("CourseID", newCourseId);
            }
            params.put("DeviceType", 2);
            params.put("DeviceCode", "");
            return params;
        }
    }


    public Map<String, Object> toCancelMap() {
        Map<String, Object> params = new HashMap<>();
        if (StringUtils.isNotBlank(userId)) {
            params.put("UID", userId);
        }
        if (StringUtils.isNotBlank(rTime)) {
            params.put("RTime", rTime);
        }
        if (StringUtils.isNotBlank(textBookId)) {
            params.put("CourseID", textBookId);
        }
        return params;
    }

}
