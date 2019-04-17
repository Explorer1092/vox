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

package com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.filterList;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.VendorAppFilterContext;
import com.voxlearning.utopia.service.config.api.constant.GlobalTagName;
import com.voxlearning.utopia.service.config.api.entity.GlobalTag;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 加载黑名单信息
 *
 * @author peng.zhang.a
 * @since 16-10-13
 */
@Named
public class Filter_LoadBlackList extends FilterBase {

    @Inject private GlobalTagServiceClient globalTagServiceClient;

    @Override
    public void execute(VendorAppFilterContext context) {

        boolean inWhiteList = globalTagServiceClient.getGlobalTagBuffer().findByName(GlobalTagName.PaymentWhiteListUsers.name())
                .stream()
                .filter(p -> SafeConverter.toLong(p) == context.getStudentDetail().getId())
                .findFirst()
                .orElse(null) != null;

        context.setInWhiteList(inWhiteList);
        if (inWhiteList) {
            return;
        }

        Map<GlobalTagName, List<GlobalTag>> blacks = new LinkedHashMap<>();
        blacks.put(GlobalTagName.ParentBlackListUsers, globalTagServiceClient.getGlobalTagBuffer().findByName(GlobalTagName.ParentBlackListUsers.name()));
        blacks.put(GlobalTagName.AfentiBlackListUsers, globalTagServiceClient.getGlobalTagBuffer().findByName(GlobalTagName.AfentiBlackListUsers.name()));

        Set<String> parentBlackUsers = CollectionUtils.toLinkedList(blacks.get(GlobalTagName.ParentBlackListUsers)).stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getTagValue() != null)
                .map(GlobalTag::getTagValue)
                .collect(Collectors.toSet());

        Set<String> studentBlackUsers = CollectionUtils.toLinkedList(blacks.get(GlobalTagName.AfentiBlackListUsers)).stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getTagValue() != null)
                .map(GlobalTag::getTagValue)
                .collect(Collectors.toSet());
        context.setParentBlackUsers(parentBlackUsers);
        context.setStudentBlackUsers(studentBlackUsers);
    }
}
