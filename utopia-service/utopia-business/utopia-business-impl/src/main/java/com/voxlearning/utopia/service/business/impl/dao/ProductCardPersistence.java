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

package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.dao.jdbc.persistence.AbstractEntityPersistence;
import com.voxlearning.utopia.entity.product.ProductCard;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.inject.Named;
import java.util.List;
import java.util.Set;

/**
 * 付费产品卡数据库访问类, 数据库访问相对而言不频繁，可以不走缓存
 * Created by Alex on 14-8-21.
 */
@Named
public class ProductCardPersistence extends AbstractEntityPersistence<Long, ProductCard> {


    public Long getMaxCardSeq() {
        String sql = "SELECT MAX(CARD_SEQ) FROM VOX_PRODUCT_CARD";
        return getUtopiaSql().withSql(sql).queryValue(Long.class);
    }

    public ProductCard findByCardSeq(Long cardSeq) {
        String sql = "WHERE CARD_SEQ=?";
        return withSelectFromTable(sql).useParamsArgs(cardSeq).queryObject();
    }

    public ProductCard findByCardKey(Long cardKey) {
        String sql = "WHERE CARD_KEY=?";
        return withSelectFromTable(sql).useParamsArgs(cardKey).queryObject();
    }

    public List<ProductCard> findByCardKeys(Set<Long> cardKeys) {
        MapSqlParameterSource source = new MapSqlParameterSource().addValue("cardKeys", cardKeys);
        String sql = "WHERE CARD_KEY IN (:cardKeys)";
        return withSelectFromTable(sql).useParams(source.getValues()).queryAll();
    }

    public List<ProductCard> findByCardSeqRange(Long seqStart, Long seqEnd) {
        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("seqStart", seqStart).addValue("seqEnd", seqEnd);
        String sql = "WHERE CARD_SEQ >= :seqStart AND CARD_SEQ <= :seqEnd";
        return withSelectFromTable(sql).useParams(source.getValues()).queryAll();
    }

    public int openCard(Long agentUserId, Long cardId, String openRegion) {
        String sql = "UPDATE VOX_PRODUCT_CARD SET CARD_STATUS=2, OPEN_DATETIME=now(), OPEN_REGION=?, AGENT_USER_ID=? WHERE ID=? AND CARD_STATUS=1";
        return utopiaSql.withSql(sql).useParamsArgs(openRegion, agentUserId, cardId).executeUpdate();
    }

    public int adjustCardRegion(Long agentUserId, Long cardId, String openRegion) {
        String sql = "UPDATE VOX_PRODUCT_CARD SET OPEN_DATETIME=now(), OPEN_REGION=?, AGENT_USER_ID=? WHERE ID=? AND CARD_STATUS=2";
        return utopiaSql.withSql(sql).useParamsArgs(openRegion, agentUserId, cardId).executeUpdate();
    }

    public int activateCard(Long cardId, Long userId) {
        String sql = "UPDATE VOX_PRODUCT_CARD SET CARD_STATUS=3, ACTIVATE_DATETIME=now(), USER_ID=? WHERE ID=? AND CARD_STATUS=2";
        return utopiaSql.withSql(sql).useParamsArgs(userId, cardId).executeUpdate();
    }

    public int activateStudyCraftCard(Long cardId, Long userId) {
        String sql = "UPDATE VOX_PRODUCT_CARD SET CARD_STATUS=3, ACTIVATE_DATETIME=now(), USER_ID=? WHERE ID=? AND CARD_TYPE='StudyCraft'";
        return utopiaSql.withSql(sql).useParamsArgs(userId, cardId).executeUpdate();
    }

    public int returnCard(Long cardId, String reason) {
        String sql = "UPDATE VOX_PRODUCT_CARD SET CARD_STATUS=9, RETURN_DATETIME=now(), RETURN_REASON=? WHERE ID=?";
        return utopiaSql.withSql(sql).useParamsArgs(reason, cardId).executeUpdate();
    }

}