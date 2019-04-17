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

package com.voxlearning.utopia.service.mizar.api.entity.oa;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by Summer Yang on 2016/7/4.
 * 公众号推送文章
 */
@DocumentTable(table = "VOX_OFFICIAL_ACCOUNTS_ARTICLE")
@DocumentConnection(configName = "hs_misc")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161123")
public class OfficialAccountsArticle extends AbstractDatabaseEntity {

    private static final long serialVersionUID = -8938776806153351169L;
    @UtopiaSqlColumn @Getter @Setter private Long accountId;
    @UtopiaSqlColumn @Getter @Setter private String articleUrl;
    @UtopiaSqlColumn @Getter @Setter private String articleTitle;
    @UtopiaSqlColumn @Getter @Setter private String imgUrl;
    @UtopiaSqlColumn @Getter @Setter private String bundleId;
    @UtopiaSqlColumn @Getter @Setter private Boolean hasSend;  // 是否推送了JPUSH
    @UtopiaSqlColumn @Getter @Setter private Boolean bindSid;  // 是否拼接SID
    @UtopiaSqlColumn @Getter @Setter private Status status;
    @UtopiaSqlColumn @Getter @Setter private Boolean disabled;
    @UtopiaSqlColumn @Getter @Setter private Date publishDatetime;// 发布时间
    @UtopiaSqlColumn @Getter @Setter private String publishUser;// 发布用户
    @UtopiaSqlColumn @Getter @Setter private String materialId;// 素材id

    public enum Status {
        Online, Offline, Published
    }

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(OfficialAccountsArticle.class, id);
    }

    public static String ck_accountId(Long accountId) {
        return CacheKeyGenerator.generateCacheKey(OfficialAccountsArticle.class, "accountId", accountId);
    }
}
