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

package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.service.reward.entity.RewardProductTagRef;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Persistence implementation of entity {@link RewardProductTagRef}.
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 * @since Jul 14, 2014
 */
@Named
@CacheBean(type = RewardProductTagRef.class)
public class RewardProductTagRefDao extends StaticCacheDimensionDocumentJdbcDao<RewardProductTagRef, Long> {

    @CacheMethod
    public List<RewardProductTagRef> findByTagId(@CacheParameter(value = "tagId") final Long tagId) {
        Criteria criteria = Criteria.where("TAG_ID").is(tagId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<RewardProductTagRef> findByProductId(@CacheParameter(value = "productId") final Long productId) {
        Criteria criteria = Criteria.where("PRODUCT_ID").is(productId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public Map<Long, List<RewardProductTagRef>> findByProductIdList(@CacheParameter(value = "productId", multiple = true) final List<Long> productIdList) {
        Map<Long, List<RewardProductTagRef>> result = new HashMap<>();
        Criteria criteria = Criteria.where("PRODUCT_ID").in(productIdList);
        List<RewardProductTagRef> list =query(Query.query(criteria));
        if (CollectionUtils.isNotEmpty(list)) {
            for (RewardProductTagRef ref : list) {
                if (result.containsKey(ref.getProductId())) {
                    result.get(ref.getProductId()).add(ref);
                } else {
                    List<RewardProductTagRef> subList = new ArrayList<>();
                    subList.add(ref);
                    result.put(ref.getProductId(), subList);
                }
            }
        }
        return result;
    }

    public int deleteByProductId(final Long productId) {
        Criteria criteria = Criteria.where("PRODUCT_ID").is(productId);
        List<RewardProductTagRef> originals = query(Query.query(criteria));
        if (originals.isEmpty()) {
            return 0;
        }
        int rows = (int) $remove(criteria);
        if (rows > 0) {
            evictDocumentCache(originals);
        }
        return rows;
    }
}
