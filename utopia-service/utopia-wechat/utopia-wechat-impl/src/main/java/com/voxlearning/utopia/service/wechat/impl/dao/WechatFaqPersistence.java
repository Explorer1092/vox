/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mysql.persistence.NoCacheStaticMySQLPersistence;
import com.voxlearning.utopia.service.wechat.api.entities.WechatFaq;

import javax.inject.Named;
import java.util.List;

/**
 * Persistence implementation of entity {@code WechatFaq}.
 *
 * @author xin.xin
 * @since 2014-04-18
 */
@Named
public class WechatFaqPersistence extends NoCacheStaticMySQLPersistence<WechatFaq, Long> {

    public WechatFaqPersistence() {
        registerBeforeInsertListener(documents -> documents.forEach(e -> {
            if (e.getDisabled() == null) e.setDisabled(false);
        }));
    }

    public List<WechatFaq> findByCatalog(Long catalogId, Integer type) {
        Criteria criteria = Criteria.where("CATALOG_ID").is(catalogId)
                .and("TYPE").is(type)
                .and("STATUS").is("published")
                .and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public List<WechatFaq> findByTitleLike(String keyWord, Integer type) {
        Criteria criteria = Criteria.where("KEYWORD").like("%" + keyWord + "%")
                .and("TYPE").is(type)
                .and("STATUS").is("published")
                .and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public int changeFaqsStatus(List<Long> ids, String status) {
        Criteria criteria = Criteria.where("ID").in(ids);
        Update update = Update.update("STATUS", status);
        return (int) executeUpdate(update, criteria, getTableName());
    }

    public int removeFaqs(List<Long> ids) {
        Criteria criteria = Criteria.where("ID").in(ids);
        Update update = Update.update("DISABLED", true);
        return (int) executeUpdate(update, criteria, getTableName());
    }
}
