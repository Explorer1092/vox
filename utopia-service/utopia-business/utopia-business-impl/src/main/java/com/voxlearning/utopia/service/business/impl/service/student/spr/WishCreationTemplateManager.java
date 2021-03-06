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
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * Student parent reward -- wish creation template manager
 *
 * @author RuiBao
 * @version 0.1
 * @since 1/12/2015
 */
@Named
@Slf4j
@NoArgsConstructor
public class WishCreationTemplateManager {
    private final Map<WishType, WishCreationTemplate> templates = new HashMap<>();

    public void register(WishCreationTemplate template) {
        templates.put(template.getType(), template);
    }

    public WishCreationTemplate get(WishType wishType) {
        return templates.get(wishType);
    }
}
