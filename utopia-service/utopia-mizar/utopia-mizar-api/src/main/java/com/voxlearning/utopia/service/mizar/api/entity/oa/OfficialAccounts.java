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
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Summer Yang on 2016/7/4.
 * <p>
 * 公众号
 */
@DocumentTable(table = "VOX_OFFICIAL_ACCOUNTS")
@NoArgsConstructor
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161122")
@DocumentConnection(configName = "hs_misc")
public class OfficialAccounts extends AbstractDatabaseEntity {

    private static final long serialVersionUID = 5451980315483776705L;

    @UtopiaSqlColumn @Getter @Setter private String accountsKey;
    @UtopiaSqlColumn @Getter @Setter private String name;
    @UtopiaSqlColumn @Getter @Setter private String title;
    @UtopiaSqlColumn @Getter @Setter private String instruction;
    @UtopiaSqlColumn @Getter @Setter private String imgUrl;
    @UtopiaSqlColumn @Getter @Setter private Status status;
    @UtopiaSqlColumn @Getter @Setter private Boolean paymentBlackLimit;   // 是否限制黑名单用户
    @UtopiaSqlColumn @Getter @Setter private Boolean followLimit;         // 是否允许主动关注
    @UtopiaSqlColumn @Getter @Setter private Boolean disabled;
    @UtopiaSqlColumn @Getter @Setter private String generalAdminUsers;
    @UtopiaSqlColumn @Getter @Setter private String seniorAdminUsers;
    @UtopiaSqlColumn @Getter @Setter private Integer maxPublishNumsD;
    @UtopiaSqlColumn @Getter @Setter private Integer maxPublishNumsM;
    @UtopiaSqlColumn @Getter @Setter private Integer publishNums;
    @UtopiaSqlColumn @Getter @Setter private String greetings; // 问候语

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(OfficialAccounts.class, id);
    }

    public static String ck_key(String accountsKey) {
        return CacheKeyGenerator.generateCacheKey(OfficialAccounts.class, "AK", accountsKey);
    }

    public static String ck_all() {
        return CacheKeyGenerator.generateCacheKey(OfficialAccounts.class, "ALL");
    }

    public enum Status {
        Online, Offline
    }

    // 特殊账号枚举
    public enum SpecialAccount {

        FAIRY_LAND(12L, "fairyland"),    // 自学乐园
        SUBMIT_ALBUM(4L, "album"),       // 订阅专辑
        MICRO_COURSE(13L, "weiketang"),  // 微课堂
        GRIND_EAR_SERVICE(14L, "dianduji"),  //磨耳朵
        ;

        private Long accountId;
        private String accountKey;

        SpecialAccount(Long accountId, String accountKey) {
            this.accountId = accountId;
            this.accountKey = accountKey;
        }

        public Long getId() {
            return this.accountId;
        }

        public String getKey() {
            return this.accountKey;
        }
    }
}