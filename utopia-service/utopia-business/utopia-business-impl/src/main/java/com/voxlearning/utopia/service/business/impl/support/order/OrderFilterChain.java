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

package com.voxlearning.utopia.service.business.impl.support.order;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;

/**
 * Created by xinxin on 29/2/2016.
 */
@Slf4j
public abstract class OrderFilterChain implements FilterChain {

    private Integer currentPosition = 0;

    protected LinkedList<Filter> filters = new LinkedList<>();

    @Override
    public void doFilter(OrderFilterContext context) {
        if (currentPosition < filters.size()) {
            Filter filter = filters.get(currentPosition++);
            filter.doFilter(context, this);
        }
    }
}
