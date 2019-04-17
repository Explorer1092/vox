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

package com.voxlearning.utopia.service.crm.api.constants.agent;

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.utopia.service.crm.api.constants.ProductType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by Alex on 14-7-28.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentProductType {

    MATERIAL(1, "材料", "Material"),
//    CARD88(20, "88元实物卡", "WalkerFantasyAdventure"),
//    CARD158(21, "158元实物卡", "WalkerArdourRunning"),
    WALKER(22, ProductType.CARD_WALKER.getCardName(), ProductType.CARD_WALKER.getCardType().name()),
    AFENTI(23, ProductType.CARD_AFENTI.getCardName(), ProductType.CARD_AFENTI.getCardType().name()),
    PICARO(24, ProductType.CARD_PICARO.getCardName(), ProductType.CARD_PICARO.getCardType().name()),

    ;


    @Getter private final int type;
    @Getter private final String name;
    @Getter private final String productCardType;

    private static final Map<Integer, AgentProductType> map;
    private static final Map<String, AgentProductType> productCardTypeMap;

    static {
        map = new LinkedHashMap<>();
        for (AgentProductType productType : values()) {
            map.put(productType.type, productType);
        }

        productCardTypeMap = new LinkedHashMap<>();
        for (AgentProductType productType : values()) {
            productCardTypeMap.put(productType.productCardType, productType);
        }
    }

    public static Map<Integer, AgentProductType> toMap() {
        return map;
    }

    public static Map<String, AgentProductType> toProductCardTypeMap() {
        return productCardTypeMap;
    }

    public static AgentProductType parse(Integer type) {
        return toMap().get(type);
    }

    public static AgentProductType parse(String type) {
        return toProductCardTypeMap().get(type);
    }

    public static List<KeyValuePair<Integer, String>> toKeyValuePairs() {
        List<KeyValuePair<Integer, String>> pairs = new ArrayList<>();
        for (AgentProductType agentProductType : values()) {
            KeyValuePair<Integer, String> pair = new KeyValuePair<>();
            pair.setKey(agentProductType.type);
            pair.setValue(agentProductType.name);
            pairs.add(pair);
        }
        return pairs;
    }

}