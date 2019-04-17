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

package com.voxlearning.utopia.service.wechat.api.entities;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by XiaoPeng.Yang on 15-5-14.
 */
@Getter
@Setter
@DocumentTable(table = "VOX_WECHAT_FACE_INVITE_RECORD")
@UtopiaCacheExpiration(86400)
@UtopiaCacheRevision("20160728")
public class WechatFaceInviteRecord extends AbstractDatabaseEntity implements CacheDimensionDocument {
    private static final long serialVersionUID = 327151749859960143L;

    private String openId;
    private Long inviter;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("openId", openId)
        };
    }
}
