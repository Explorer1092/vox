/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.nekketsu.adventure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PK武装
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/9/15 17:39
 */
public class PkEquipment implements Serializable {
    private static final long serialVersionUID = -1066935180506858038L;

    @Getter @Setter private String id;
    @Getter @Setter private String img;        //图片
    @Getter @Setter private String name;        //名称
    @Getter @Setter private String desc;        //描述
    @Setter private Integer price;      //价格，大冒险钻石数
//    @Getter @Setter private EquipmentAttribute baseAttribute;        //基础属性
//    @Getter @Setter private EquipmentAttribute secondaryAttribute;   //次级属性

    public Integer getPrice() {
        if (priceMap.containsKey(this.getId())) {
            return priceMap.get(this.getId());
        }
        return 0;
    }

    private static Map<String, Integer> priceMap = new LinkedHashMap<>();
    static {
        priceMap.put("E00049", 240);
        priceMap.put("E00050", 240);
        priceMap.put("E00051", 240);
        priceMap.put("E00052", 180);
        priceMap.put("E00053", 180);
        priceMap.put("E00054", 120);
    }

    @JsonIgnore
    public static Integer getPrice(String id) {
        if (priceMap.containsKey(id)) {
            return priceMap.get(id);
        }
        return 0;
    }
}
