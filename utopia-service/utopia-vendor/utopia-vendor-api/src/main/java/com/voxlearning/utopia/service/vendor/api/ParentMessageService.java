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

package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Jia HuanYin
 * @since 2015/9/15
 */
@ServiceVersion(version = "1.1")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ParentMessageService extends IPingable {

    /**
     * 少暴露了linkType参数，如果是有站外跳转的链接，linkType传空被默认置成了站内，然后报错。
     */
    @Deprecated
    boolean postParentMessage(List<Long> parentIds,
                              Long studentId,
                              String content,
                              String imageUrl,
                              String linkUrl,
                              String senderName,
                              ParentMessageTag tag,
                              ParentMessageType type);

    boolean postParentMessage(List<Long> parentIds,
                              Long studentId,
                              String content,
                              String imageUrl,
                              Integer linkType,
                              String linkUrl,
                              String senderName,
                              ParentMessageTag tag,
                              ParentMessageType type);
}
