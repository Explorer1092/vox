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

package com.voxlearning.utopia.service.reward.mapper;

import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

/**
 * Pagination implementation of data structure {@link RewardProductDetail}.
 *
 * @author Xiaohai Zhang
 * @serial
 * @since Dec 4, 2014
 */
public class RewardProductDetailPagination extends PageImpl<RewardProductDetail> {
    private static final long serialVersionUID = -1321213613980669282L;

    public RewardProductDetailPagination() {
        super(Collections.emptyList());
    }

    public RewardProductDetailPagination(List<RewardProductDetail> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public RewardProductDetailPagination(List<RewardProductDetail> content) {
        super(content);
    }
}
