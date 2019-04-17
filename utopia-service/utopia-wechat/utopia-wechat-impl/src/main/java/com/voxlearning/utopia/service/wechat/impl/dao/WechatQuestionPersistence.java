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
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.wechat.api.entities.WechatQuestion;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author xin
 * @since 14-4-24 上午10:36
 */
@Named
@CacheBean(type = WechatQuestion.class)
public class WechatQuestionPersistence extends AlpsStaticJdbcDao<WechatQuestion, Long> {

    public WechatQuestionPersistence() {
        registerBeforeInsertListener(documents -> documents.stream()
                .filter(e -> e.getDisabled() == null)
                .forEach(e -> e.setDisabled(Boolean.FALSE)));
    }

    @Override
    protected void calculateCacheDimensions(WechatQuestion document, Collection<String> dimensions) {
        dimensions.add(WechatQuestion.ck_id(document.getId()));
    }

    public List<WechatQuestion> findWechatQuestionByCreateTimeOrState(int state, Date start, Date end) {
        List<Criteria> list = new LinkedList<>();
        if (state > 0) {
            list.add(Criteria.where("STATE").is(state));
        }
        if (start != null) {
            list.add(Criteria.where("CREATE_DATETIME").gte(start));
        }
        if (end != null) {
            list.add(Criteria.where("CREATE_DATETIME").lte(end));
        }
        Criteria criteria = Criteria.and(list.toArray(new Criteria[list.size()]));
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return query(Query.query(criteria).with(sort));
    }

}
