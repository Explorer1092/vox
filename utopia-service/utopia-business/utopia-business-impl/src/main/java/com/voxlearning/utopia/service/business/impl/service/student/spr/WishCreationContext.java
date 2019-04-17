/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.service.student.spr;

import com.voxlearning.utopia.api.constant.WishType;

import java.util.LinkedHashMap;

/**
 * @author RuiBao
 * @version 0.1
 * @since 1/12/2015
 */

public class WishCreationContext extends LinkedHashMap<String, Object> {
    private static final long serialVersionUID = -1637555498354013162L;

    private WishCreationContext(WishType type) {
        put("type", type);
    }

    public static WishCreationContext of(WishType type) {
        return new WishCreationContext(type);
    }

    public WishType getType() {
        return (WishType) get("type");
    }

    public Long getUserId() {
        return (Long) get("userId");
    }

    public Integer getIntegral() {
        return (Integer) get("integral");
    }

    public String getWish() {
        return (String) get("wish");
    }

    public WishCreationContext with(String name, Object value) {
        put(name, value);
        return this;
    }

    public WishCreationContext withUserId(Long userId) {
        with("userId", userId);
        return this;
    }

    public WishCreationContext withIntegral(Integer integral) {
        with("integral", integral);
        return this;
    }

    public WishCreationContext withWish(String wish) {
        with("wish", wish);
        return this;
    }
}
