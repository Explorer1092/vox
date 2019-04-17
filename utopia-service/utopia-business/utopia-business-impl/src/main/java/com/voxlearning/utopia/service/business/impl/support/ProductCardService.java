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

package com.voxlearning.utopia.service.business.impl.support;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.ProductCardStatus;
import com.voxlearning.utopia.entity.product.ProductCard;
import com.voxlearning.utopia.service.business.impl.dao.ProductCardPersistence;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * 代理付费产品相关的操作封装
 * <p>
 * Created by Alex on 14-8-21.
 */
@Named
@Slf4j
@NoArgsConstructor
public class ProductCardService extends SpringContainerSupport {

    @Inject private UtopiaSqlFactory utopiaSqlFactory;
    @Inject private ProductCardPersistence productCardPersistence;

    private UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
    }

    // 制卡
    public void makeNewCards(final OrderProductServiceType productType, final Double cardAmount, final Integer validPeriod, final String extInfo, int count) {
        // 限制程序执行时间
        if (count < 1 || count > 3000) {
            throw new IllegalArgumentException("无效的制卡数量:" + count);
        }

        // 检查卡的类型
        if (productType == null
                || (OrderProductServiceType.Walker != productType
                && OrderProductServiceType.StudyCraft != productType
                && OrderProductServiceType.AfentiExam != productType)) {
            throw new IllegalArgumentException("无效的卡类型:" + productType);
        }

        // 批量生成密码
        final Set<Long> newCardKeySet = generateNewCardKeys(count);
        // 获取数据库中最大的序号
        final Long maxCardSeq = productCardPersistence.getMaxCardSeq();

        // 将新生成的卡号和密码存进数据库
        utopiaSql.withTransaction(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                Long newCardSeq = maxCardSeq == null ? 1L : maxCardSeq + 1;

                for (Long cardKey : newCardKeySet) {
                    ProductCard newCard = ProductCard.newInstance(productType, cardAmount, validPeriod, newCardSeq++, cardKey);
                    newCard.setExtInfo(extInfo);
                    productCardPersistence.persist(newCard);
                }
            }
        });
    }

    // 开卡
    public MapMessage openCard(final Long agentUserId, final Long cardSeqStart, final Long cardSeqEnd, final String openRegion) {
        if (agentUserId == null || agentUserId == 0L
                || cardSeqStart == null || cardSeqEnd == null || cardSeqEnd < cardSeqStart
                || StringUtils.isBlank(openRegion)) {
            return MapMessage.errorMessage("无效的开卡请求参数!");
        }

        // 判断卡数量是否正确
        final List<ProductCard> existCards = productCardPersistence.findByCardSeqRange(cardSeqStart, cardSeqEnd);
        if (existCards.size() != (cardSeqEnd - cardSeqStart + 1)) {
            return MapMessage.errorMessage("部分卡片不存在!");
        }

        // 判断卡状态是否为NEW
        long count = existCards.stream()
                .filter(e -> !ProductCardStatus.NEW.getCode().equals(e.getCardStatus()))
                .count();
        if (count > 0) {
            return MapMessage.errorMessage("部分卡片状态不正确，无法开通!");
        }

        // 执行更新并检查卡的状态是否正常
        utopiaSql.withTransaction(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                for (ProductCard existCard : existCards) {
                    productCardPersistence.openCard(agentUserId, existCard.getId(), openRegion);
                }
            }
        });

        return MapMessage.successMessage();
    }

    // 调整开卡区域
    public MapMessage adjustCardRegion(final Long agentUserId, final Long cardSeqStart, final Long cardSeqEnd, final String openRegion) {
        if (agentUserId == null || agentUserId == 0L
                || cardSeqStart == null || cardSeqEnd == null || cardSeqEnd < cardSeqStart
                || StringUtils.isBlank(openRegion)) {
            return MapMessage.errorMessage("无效的请求参数!");
        }

        // 判断卡数量是否正确
        final List<ProductCard> existCards = productCardPersistence.findByCardSeqRange(cardSeqStart, cardSeqEnd);
        if (existCards.size() != (cardSeqEnd - cardSeqStart + 1)) {
            return MapMessage.errorMessage("部分卡片不存在!");
        }

        // 判断卡状态是否为已开卡
        long count = existCards.stream()
                .filter(e -> !ProductCardStatus.OPENED.getCode().equals(e.getCardStatus()))
                .count();
        if (count > 0) {
            return MapMessage.errorMessage("部分卡片状态不正确，无法处理!");
        }

        // 执行更新并检查卡的状态是否正常
        utopiaSql.withTransaction(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                for (ProductCard existCard : existCards) {
                    productCardPersistence.adjustCardRegion(agentUserId, existCard.getId(), openRegion);
                }
            }
        });

        return MapMessage.successMessage();
    }

    // 激活实物卡
    public MapMessage activateCard(Long cardKey, Long userId, Integer regionCode) {
        if (cardKey == null || userId == null || regionCode == null) {
            return MapMessage.errorMessage("无效的请求参数!");
        }

        // 判断卡是否存在
        ProductCard card = productCardPersistence.findByCardKey(cardKey);
        if (card == null) {
            return MapMessage.errorMessage("对不起，您输入的密码无效，请确认输入的密码是否正确。如有疑问，请联系售卡人!");
        }

        // 判断卡的状态
        if (!ProductCardStatus.OPENED.getCode().equals(card.getCardStatus())) {
            return MapMessage.errorMessage("对不起，您输入的密码无效，请确认输入的密码是否正确。如有疑问，请联系售卡人!");
        }

        // 判断卡的区域, 这里不好依赖Region的实现，按照区域Code的编码规则进行验证
        // regionCode本身, RegionCode的父级,或者RegionCode的父父级存在开卡区域里面
        // FIXME 有空想想有没有更好的实现方式
//        String tobeCheckedCode1 = String.valueOf(regionCode);
//        String tobeCheckedCode2 = tobeCheckedCode1.substring(0, 4) + "00";
//        String tobeCheckedCode3 = tobeCheckedCode1.substring(0, 2) + "0000";
//        if (!card.getOpenRegion().contains(tobeCheckedCode1) && !card.getOpenRegion().contains(tobeCheckedCode2)
//                && !card.getOpenRegion().contains(tobeCheckedCode3)) {
//            return MapMessage.errorMessage("对不起，您输入的密码无效，请确认输入的密码是否正确。如有疑问，请联系售卡人!");
//        }

        // 激活
        if (productCardPersistence.activateCard(card.getId(), userId) != 1) {
            return MapMessage.errorMessage("对不起，您输入的密码无效，请确认输入的密码是否正确。如有疑问，请联系售卡人!");
        }

        MapMessage successMsg = MapMessage.successMessage();
        successMsg.add("cardSeq", card.getCardSeq());
        return successMsg;
    }

    // 激活口袋学社实物卡
    public MapMessage activateStudyCraftCard(Long cardKey, Long userId) {
        if (cardKey == null || userId == null) {
            return MapMessage.errorMessage("无效的请求参数!");
        }

        // 判断卡是否存在
        ProductCard card = productCardPersistence.findByCardKey(cardKey);
        if (card == null) {
            return MapMessage.errorMessage("对不起，您输入的密码无效，请确认输入的密码是否正确。如有疑问，请联系售卡人!");
        }

        // 判断卡的类型是否正确
        if (!OrderProductServiceType.StudyCraft.name().equals(card.getCardType())) {
            return MapMessage.errorMessage("对不起，您输入的密码无效，请确认输入的密码是否正确。如有疑问，请联系售卡人!");
        }

        // 判断卡的状态
        if (!ProductCardStatus.NEW.getCode().equals(card.getCardStatus())) {
            return MapMessage.errorMessage("对不起，您输入的密码无效，请确认输入的密码是否正确。如有疑问，请联系售卡人!");
        }

        // 激活
        if (productCardPersistence.activateStudyCraftCard(card.getId(), userId) != 1) {
            return MapMessage.errorMessage("对不起，您输入的密码无效，请确认输入的密码是否正确。如有疑问，请联系售卡人!");
        }

        card = productCardPersistence.findByCardKey(cardKey);

        MapMessage successMsg = MapMessage.successMessage();
        successMsg.add("cardInfo", card);
        return successMsg;
    }


    // 退卡
    public MapMessage returnCard(Long agentUserId, Long cardSeq, String reason) {
        if (agentUserId == null || cardSeq == null) {
            return MapMessage.errorMessage("无效的请求参数!");
        }

        ProductCard card = productCardPersistence.findByCardSeq(cardSeq);
        if (card == null) {
            return MapMessage.errorMessage("对不起，您输入的卡号不存在，请确认输入的卡号是否正确。");
        }

        // agentUserId == 0表示管理员退卡，应该允许不一样
        // agentUserId > 0 表示代理退卡，必须由开卡代理自己操作才允许
        if (agentUserId > 0L && !agentUserId.equals(card.getAgentUserId())) {
            return MapMessage.errorMessage("对不起，您无权操作此卡。");
        }

        if (card.getCardStatus().equals(ProductCardStatus.NEW.getCode()) || card.getCardStatus().equals(ProductCardStatus.RETURNED.getCode())) {
            return MapMessage.errorMessage("卡未发行或已被退掉。");
        }

        // 判断开卡时间是否超过15天
        if (card.getActivateDatetime() != null && DateUtils.dayDiff(new Date(), card.getActivateDatetime()) > 15) {
            return MapMessage.errorMessage("超过15天的卡不能退!");
        }

        if (productCardPersistence.returnCard(card.getId(), reason) != 1) {
            return MapMessage.errorMessage("退卡操作失败!");
        }

        return MapMessage.successMessage();
    }

    public MapMessage batchReturnCard(Long userId, Long cardSeqStart, Long cardSeqEnd, String reason) {

        Map<String, List<Long>> result = new HashMap<>();
        List<Long> okCardList = new ArrayList<>();
        List<Long> badCardList = new ArrayList<>();
        if (cardSeqStart == null || cardSeqEnd == null) {
            return MapMessage.errorMessage("批量退卡的开始/结束序号不能为空！");
        }

        List<ProductCard> productCards = productCardPersistence.findByCardSeqRange(cardSeqStart, cardSeqEnd);
        if (productCards == null) {
            return MapMessage.errorMessage("非法的卡号集合！");
        }

        for (ProductCard card : productCards) {
            if (!card.getCardStatus().equals(ProductCardStatus.OPENED.getCode()) || (userId > 0L && !userId.equals(card.getAgentUserId()))) {
                badCardList.add(card.getCardSeq());
            } else {
                productCardPersistence.returnCard(card.getId(), reason);
                okCardList.add(card.getCardSeq());
            }
        }

        result.put("okCardList", okCardList);
        result.put("badCardList", badCardList);

        return MapMessage.successMessage().add("result", result);
    }

    public ProductCard getByCardSeq(Long cardSeq) {
        if (cardSeq == null) {
            throw new IllegalArgumentException("卡号不能为空！");
        }
        return productCardPersistence.findByCardSeq(cardSeq);
    }

    public ProductCard getByCardKey(Long cardKey) {
        if (cardKey == null) {
            throw new IllegalArgumentException("卡密码不能为空！");
        }
        return productCardPersistence.findByCardKey(cardKey);
    }

    public List<ProductCard> getByCardSeqRange(Long start, Long end) {
        if (start == null || end == null || start > end) {
            throw new IllegalArgumentException("参数形式正确！");
        }
        return productCardPersistence.findByCardSeqRange(start, end);
    }

    // 批量生成密码
    private Set<Long> generateNewCardKeys(int count) {
        int madeCount = 0;
        final Set<Long> newCardKeySet = new HashSet<>();
        while (madeCount < count) {
            // 为了减少数据库查询次数，每次限制100张
            int internalMadeCount = count - madeCount;
            if (internalMadeCount > 100) {
                internalMadeCount = 100;
            }

            Set<Long> internalCardKeys = new HashSet<>();
            for (int i = 0; i < internalMadeCount; i++) {
                internalCardKeys.add(ConversionUtils.toLong(RandomUtils.randomNumeric(12)));
            }

            // 去除数据库中已经存在的密码
            List<ProductCard> existCards = productCardPersistence.findByCardKeys(internalCardKeys);
            for (ProductCard existCard : existCards) {
                internalCardKeys.remove(existCard.getCardKey());
            }

            newCardKeySet.addAll(internalCardKeys);
            madeCount += internalCardKeys.size();
        }

        return newCardKeySet;
    }
}
