/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.nekketsu.adventure.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 被超越的用户信息
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/8/31 22:37
 */
@Data
public class BeyondClassmate implements Serializable {
    private static final long serialVersionUID = 1646331987192245437L;
    private Long userId;
    private String name;
    private String img;

    public static BeyondClassmate newInstance(Long userId, String name, String img) {
        if (null == userId || 0 == userId
                || (name == null || name.isEmpty()) || (img == null || img.isEmpty())) {
            return null;
        }
        BeyondClassmate beyondClassmate = new BeyondClassmate();
        beyondClassmate.setUserId(userId);
        beyondClassmate.setName(name);
        beyondClassmate.setImg(img);
        return beyondClassmate;
    }

}
