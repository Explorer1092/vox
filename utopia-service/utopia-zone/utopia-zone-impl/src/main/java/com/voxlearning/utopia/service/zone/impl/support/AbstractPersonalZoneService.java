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

package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.user.api.TinyGroupLoader;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.zone.api.PersonalZoneLoader;
import com.voxlearning.utopia.service.zone.api.PersonalZoneService;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneProduct;
import com.voxlearning.utopia.service.zone.buffer.ClazzZoneProductBuffer;

import javax.inject.Inject;

import static com.voxlearning.utopia.api.constant.Currency.SILVER_COIN;

abstract public class AbstractPersonalZoneService extends SpringContainerSupport implements PersonalZoneService {

    @Inject private UserOrderLoaderClient userOrderLoaderClient;

    abstract protected PersonalZoneLoader getPersonalZoneLoader();

    protected MapMessage $changeBubble(StudentDetail student,
                                       Long bubbleId,
                                       ClazzZoneProductBuffer zoneProductBuffer,
                                       TinyGroupLoader tinyGroupLoader) {
        MapMessage response = __validateUserAndBubble(student, bubbleId, zoneProductBuffer, tinyGroupLoader);
        if (!response.isSuccess()) {
            return response;
        }

        // 在__validateUserAndBubble方法中其实已经验证过是否有足够的银币购买新气泡了
        // 这里先直接更换气泡，再扣除银币好了
        // 避免扣除银币成功但是气泡更换失败的情况发生

        if (!__changeBubble(student, bubbleId).isSuccess()) {
            return MapMessage.errorMessage("气泡更换失败");
        }
        boolean needPay = SafeConverter.toBoolean(response.get("needPay"));
        if (needPay && !__purchaseBubble(student, bubbleId).isSuccess()) {
            return MapMessage.errorMessage("气泡购买失败");
        }
        return MapMessage.successMessage("气泡更换成功");
    }

    private MapMessage __validateUserAndBubble(StudentDetail student,
                                               Long bubbleId,
                                               ClazzZoneProductBuffer zoneProductBuffer,
                                               TinyGroupLoader tinyGroupLoader) {
        if (student == null || student.getId() == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        ClazzZoneProduct bubble = zoneProductBuffer.load(bubbleId);
        if (bubble == null) {
            return MapMessage.errorMessage("您选择的气泡不存在，请重新选择");
        }

        boolean needPay = false;
        switch (bubble.fetchSubspecies()) {
            case AFENTI_EXAM: {
                if (!userOrderLoaderClient.isVipUser(student.getId())) {
                    return MapMessage.errorMessage("购买阿分题，解锁专属气泡");
                }
                break;
            }
            case AFENTI_BASIC: {
//                if (!student.isAfentiBasicFlag()) {
//                    return MapMessage.errorMessage("购买冒险岛，解锁专属气泡");
//                }
                break;
            }
            case TALENT: {
//                if (!student.isAfentiTalentFlag()) {
//                    return MapMessage.errorMessage("购买单词达人，解锁专属气泡");
//                }
                break;
            }
            case PAY: {
                if (getPersonalZoneLoader().hasBubble(student.getId(), bubbleId)) break;
                UserIntegral ui = student.getUserIntegral();
                if (ui == null || bubble.getCurrency() != SILVER_COIN || ui.getUsable() < bubble.getPrice())
                    return MapMessage.errorMessage("余额不足");
                needPay = true;
                break;
            }
            case CAPTAIN: {
                return MapMessage.errorMessage("队长专属气泡不能购买");
            }
            case BEST_TINY_GROUP_ENGLISH: {
                if (!tinyGroupLoader.isBestTinyGroupAvailable(student.getId(), Subject.ENGLISH)) {
                    return MapMessage.errorMessage("最佳小组气泡由老师奖励获得");
                }
                break;
            }
            case BEST_TINY_GROUP_MATH: {
                if (!tinyGroupLoader.isBestTinyGroupAvailable(student.getId(), Subject.MATH)) {
                    return MapMessage.errorMessage("最佳小组气泡由老师奖励获得");
                }
                break;
            }
            case BEST_TINY_GROUP_CHINESE: {
                if (!tinyGroupLoader.isBestTinyGroupAvailable(student.getId(), Subject.CHINESE)) {
                    return MapMessage.errorMessage("最佳小组气泡由老师奖励获得");
                }
                break;
            }
            case FREE:
            default:
                break;
        }
        return MapMessage.successMessage().add("needPay", needPay).add("bubble", bubble);
    }
}
