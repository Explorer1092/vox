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

package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.content.api.entity.ChineseWordStock;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.inject.Named;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Named
public class CrmChineseWordStockDao extends AlpsStaticJdbcDao<ChineseWordStock, Long> {
    @Override
    protected void calculateCacheDimensions(ChineseWordStock document, Collection<String> dimensions) {
    }

    public List<String> findAllPianPang() {
        String s = "SELECT DISTINCT PIANPANG FROM VOX_CHINESE_WORD_STOCK WHERE WORD_TYPE = 0 AND DISABLED = FALSE";
        AtomicReference<String> sql = new AtomicReference<>(s);
        MapSqlParameterSource source = new MapSqlParameterSource();
        return getNamedParameterJdbcTemplate().queryForList(sql.get(), source, String.class);
    }

    public List<String> findAllBuShou() {
        String s = "SELECT DISTINCT BUSHOU FROM VOX_CHINESE_WORD_STOCK WHERE WORD_TYPE = 0 AND DISABLED = FALSE";
        AtomicReference<String> sql = new AtomicReference<>(s);
        MapSqlParameterSource source = new MapSqlParameterSource();
        return getNamedParameterJdbcTemplate().queryForList(sql.get(), source, String.class);
    }

    public List<ChineseWordStock> find(Integer wordType,
                                       String wordContent,
                                       String wordPinyin,
                                       String pianPang,
                                       String buShou,
                                       Integer clazzLevel) {
        List<Criteria> list = new LinkedList<>();
        list.add(Criteria.where("DISABLED").is(false));
        if (wordType != null && wordType != -1) {
            list.add(Criteria.where("WORD_TYPE").is(wordType));
        }
        if (!StringUtils.isEmpty(wordContent)) {
            list.add(Criteria.where("WORD_CONTENT").like(wordContent + "%"));
        }
        if (!StringUtils.isEmpty(wordPinyin)) {
            list.add(Criteria.where("PINYIN_NUMBER").like(wordPinyin + "%"));
        }
        if (!StringUtils.isEmpty(pianPang)) {
            list.add(Criteria.where("PIANPANG").is(pianPang));
        }
        if (!StringUtils.isEmpty(buShou)) {
            list.add(Criteria.where("BUSHOU").is(buShou));
        }
        if (clazzLevel != null && clazzLevel > 0) {
            list.add(Criteria.where("CLAZZ_LEVEL").lte(clazzLevel));
        }
        Criteria criteria = Criteria.and(list.toArray(new Criteria[list.size()]));
        return query(Query.query(criteria));
    }
}
