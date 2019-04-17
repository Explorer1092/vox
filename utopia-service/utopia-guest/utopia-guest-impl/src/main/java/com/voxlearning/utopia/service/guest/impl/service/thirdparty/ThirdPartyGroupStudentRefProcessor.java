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

package com.voxlearning.utopia.service.guest.impl.service.thirdparty;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.service.guest.impl.dao.ThirdPartyGroupStudentRefPersistence;
import com.voxlearning.utopia.service.user.api.entities.third.ThirdPartyGroupStudentRef;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 先不做log记录了，目前也用不着，不过processor还是留着吧
 *
 * @author xuesong.zhang
 * @since 2016/9/26
 */
@Slf4j
@Named
public class ThirdPartyGroupStudentRefProcessor {

    @Inject private ThirdPartyGroupStudentRefPersistence thirdPartyGroupStudentRefPersistence;

    public Long persist(ThirdPartyGroupStudentRef thirdPartyGroupStudentRef) {
        if (thirdPartyGroupStudentRef == null) {
            return null;
        }
        return MiscUtils.firstElement(persist(Collections.singleton(thirdPartyGroupStudentRef)));
    }

    public Collection<Long> persist(Collection<ThirdPartyGroupStudentRef> thirdPartyGroupStudentRefs) {
        if (CollectionUtils.isEmpty(thirdPartyGroupStudentRefs)) {
            return Collections.emptyList();
        }
        thirdPartyGroupStudentRefPersistence.inserts(thirdPartyGroupStudentRefs);
        return thirdPartyGroupStudentRefs.stream()
                .map(ThirdPartyGroupStudentRef::getId)
                .collect(Collectors.toList());
    }

    /**
     * disable by group id and user id
     *
     * @param userId
     * @param groupId
     * @return
     */
    public int disableByUserIdAndGroupId(Long userId, Long groupId) {
        if (userId == null || groupId == null) {
            return 0;
        }
        ThirdPartyGroupStudentRef oldValue = thirdPartyGroupStudentRefPersistence.findByUserId(userId).stream()
                .filter(e -> e.getGroupId() != null)
                .filter(e -> e.getGroupId().equals(groupId))
                .findFirst()
                .orElse(null);
        if (oldValue == null) {
            return 0;
        }
        boolean ret = thirdPartyGroupStudentRefPersistence.disable(oldValue.getId());
        return ret ? 1 : 0;
    }
}
