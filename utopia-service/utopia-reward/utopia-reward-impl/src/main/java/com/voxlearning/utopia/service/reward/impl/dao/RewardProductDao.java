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

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;

import javax.inject.Named;

/**
 * Persistence implementation of entity {@link RewardProduct}.
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 * @since Jul 14, 2014
 */
@Named
public class RewardProductDao extends StaticCacheDimensionDocumentJdbcDao<RewardProduct, Long> {

    public int increaseWishQuantity(Long id, int delta) {
        if (delta == 0) {
            return 0;
        }
        if (delta < 0) {
            return decreaseWishQuantity(id, -delta);
        }
        Criteria criteria = Criteria.where("ID").is(id);
        Update update = new Update().inc("WISH_QUANTITY", delta);
        return (int) $update(update, criteria);
    }

    public int decreaseWishQuantity(Long id, int delta) {
        if (delta == 0) {
            return 0;
        }
        if (delta < 0) {
            return increaseWishQuantity(id, -delta);
        }
        Criteria criteria = new Criteria()
                .and("ID").is(id)
                .and("WISH_QUANTITY").gte(delta);
        Update update = new Update()
                .inc("WISH_QUANTITY", -delta);
        return (int) $update(update, criteria);
    }

    public int increaseSoldQuantity(Long id, int delta) {
        if (delta == 0) {
            return 0;
        }
        if (delta < 0) {
            return decreaseSoldQuantity(id, -delta);
        }
        Criteria criteria = Criteria.where("ID").is(id);
        Update update = new Update().inc("SOLD_QUANTITY", delta);
        return (int) $update(update, criteria);
    }

    public int decreaseSoldQuantity(Long id, int delta) {
        if (delta == 0) {
            return 0;
        }
        if (delta < 0) {
            return increaseSoldQuantity(id, -delta);
        }
        Criteria criteria = new Criteria()
                .and("ID").is(id)
                .and("SOLD_QUANTITY").gte(delta);
        Update update = new Update()
                .inc("SOLD_QUANTITY", -delta);
        return (int) $update(update, criteria);
    }
}
