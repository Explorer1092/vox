/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.afenti.consumer;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.service.afenti.api.AfentiService;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.constant.PurchaseType;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.Getter;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Date;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.DEFAULT;

public class AfentiServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(AfentiServiceClient.class);

    @Getter
    @ImportService(interfaceClass = AfentiService.class)
    private AfentiService afentiService;

    public MapMessage completeGuide(Long studentId, String name) {
        if (null == studentId || StringUtils.isBlank(name))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {
            afentiService.completeGuide(studentId, name);
        } catch (Exception ex) {
            logger.error("User {} failed completing AFENTI guide {}", studentId, name);
        }
        // always return success message
        return MapMessage.successMessage();
    }

    public MapMessage getAfentiLastestOrderStatus(Long userId, Subject subject) {
        try {
            return afentiService.getAfentiLastestOrderStatus(userId, subject);
        } catch (Exception e) {
            logger.error("getAfentiLastestOrderStatus failed {}");
            return MapMessage.errorMessage();
        }
    }

    public boolean hasValidAfentiOrder(Long userId, Subject subject) {
        if (userId == null || !UtopiaAfentiConstants.AVAILABLE_SUBJECT.contains(subject)) return false;

        try {
            return afentiService.hasValidAfentiOrder(userId, subject);
        } catch (Exception ex) {
            logger.error("FAILED TO CHECK IS USER '{}' HAS AFENTI ORDER OR NOT", userId, ex);
            return false;
        }
    }

    public MapMessage generateAfentiRank(Collection<String> bookIds, Subject subject) {
        if (CollectionUtils.isEmpty(bookIds)) return MapMessage.errorMessage();

        try {
            return afentiService.generateAfentiRank(bookIds, subject);
        } catch (Exception ex) {
            logger.error("Failed generating afenti rank manager", ex);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage generateAfentiRankForMath(Collection<String> bookIds) {
        if (CollectionUtils.isEmpty(bookIds)) return MapMessage.errorMessage();

        try {
            return afentiService.generateAfentiRankForMath(bookIds);
        } catch (Exception ex) {
            logger.error("Failed generating afenti math rank manager", ex);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage generateAfentiRankForChinese(Collection<String> bookIds) {
        if (CollectionUtils.isEmpty(bookIds)) return MapMessage.errorMessage();

        try {
            return afentiService.generateAfentiRankForChinese(bookIds);
        } catch (Exception ex) {
            logger.error("Failed generating afenti chinese rank manager", ex);
            return MapMessage.errorMessage();
        }
    }

    public boolean addUserPurchaseInfo(StudentDetail studentDetail, PurchaseType purchaseType, Date createDate) {
        if (studentDetail == null || purchaseType == null) {
            return false;
        }

        try {
            return afentiService.addUserPurchaseInfo(studentDetail, purchaseType, createDate);
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean addUserRewardInfo(StudentDetail studentDetail, Integer integral) {
        return afentiService.addUserRewardInfo(studentDetail, integral);
    }

    public AfentiBook fetchAfentiBook(Long studentId, Subject subject, AfentiLearningType type) {
        if (null == studentId || subject == null || type == null)
            return null;
        MapMessage result = afentiService.fetchAfentiBook(studentId, subject, type);
        return result == null ? null : (AfentiBook) result.get("book");
    }
}
