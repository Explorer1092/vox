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

package com.voxlearning.utopia.service.zone.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author RuiBao
 * @version 0.1
 * @serial
 * @since 14-5-13
 */
@Getter
@Setter
public class BubbleMapper implements Serializable {
    private static final long serialVersionUID = 4561384975765412936L;

    private Long bubbleId;
    private String name;
    private String category;
    private Integer price;
    private Long periodOfValidity;
    private boolean currentUsing;
    private boolean owned;
}
