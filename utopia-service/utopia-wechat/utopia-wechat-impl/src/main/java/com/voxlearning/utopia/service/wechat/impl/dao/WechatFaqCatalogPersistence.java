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

package com.voxlearning.utopia.service.wechat.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.wechat.api.entities.WechatFaqCatalog;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Persistence implementation of entity {@code WechatFaqCatalog}.
 *
 * @author xin.xin
 * @since 2014-04-18
 */
@Named
@CacheBean(type = WechatFaqCatalog.class)
public class WechatFaqCatalogPersistence extends AlpsStaticJdbcDao<WechatFaqCatalog, Long> {

    public WechatFaqCatalogPersistence() {
        registerBeforeInsertListener(documents -> documents.stream()
                .filter(e -> e.getDisabled() == null)
                .forEach(e -> e.setDisabled(Boolean.FALSE)));
    }

    @Override
    protected void calculateCacheDimensions(WechatFaqCatalog document, Collection<String> dimensions) {
        dimensions.add(WechatFaqCatalog.ck_id(document.getId()));
        dimensions.add(WechatFaqCatalog.ck_type(document.getType()));
    }

    @CacheMethod
    public List<WechatFaqCatalog> findAllCatalogsByType(@CacheParameter("type") Integer type) {
        Criteria criteria = Criteria.where("TYPE").is(type).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }
}
