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
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.service.reward.entity.RewardProductCategoryRef;

import javax.inject.Named;
import java.util.List;

/**
 * Persistence implementation of entity {@link RewardProductCategoryRef}.
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 * @since Jul 14, 2014
 */
@Named
@CacheBean(type = RewardProductCategoryRef.class)
public class RewardProductCategoryRefDao extends StaticCacheDimensionDocumentJdbcDao<RewardProductCategoryRef, Long> {

    @CacheMethod
    public List<RewardProductCategoryRef> findByCategoryId(@CacheParameter("categoryId") Long categoryId) {
        Criteria criteria = Criteria.where("CATEGORY_ID").is(categoryId);
        return query(Query.query(criteria));
    }

    public List<RewardProductCategoryRef> findByProductId(Long productId) {
        Criteria criteria = Criteria.where("PRODUCT_ID").is(productId);
        return query(Query.query(criteria));
    }

    public int deleteByProductId(Long productId) {
        Criteria criteria = Criteria.where("PRODUCT_ID").is(productId);
        List<RewardProductCategoryRef> originals = query(Query.query(criteria));
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
