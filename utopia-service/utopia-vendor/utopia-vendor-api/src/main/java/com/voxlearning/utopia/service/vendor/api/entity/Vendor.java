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

package com.voxlearning.utopia.service.vendor.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.common.DisabledAccessor;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * The 3rd Vendor Information
 *
 * @author Zhilong Hu
 * @author Xiaohai Zhang
 * @serial
 * @since 2014-06-6
 */
@Getter
@Setter
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160728")
@DocumentTable(table = "VOX_VENDOR")
@DocumentConnection(configName = "hs_vendor")
public class Vendor extends AbstractDatabaseEntity implements DisabledAccessor, CacheDimensionDocument {
    private static final long serialVersionUID = 4306459661360543741L;

    @UtopiaSqlColumn(name = "DISABLED") private Boolean disabled;                       // 是否已被删除
    @UtopiaSqlColumn(name = "CNAME") @NonNull private String cname;                     // 中文名称
    @UtopiaSqlColumn(name = "ENAME") private String ename;                              // 英文名称
    @UtopiaSqlColumn(name = "SHORT_NAME") private String shortName;                     // 略称
    @UtopiaSqlColumn(name = "ADDRESS") private String address;                          // 地址
    @UtopiaSqlColumn(name = "WEB_SITE") private String webSite;                         // 网址
    @UtopiaSqlColumn(name = "LOGO_URL") private String logoUrl;                         // LOGO地址
    @UtopiaSqlColumn(name = "CONTACT1_NAME") @NonNull private String contact1Name;      // 第一联络人姓名
    @UtopiaSqlColumn(name = "CONTACT1_TEL") private String contact1Tel;                 // 第一联络人电话
    @UtopiaSqlColumn(name = "CONTACT1_MOB") @NonNull private String contact1Mob;        // 第一联络人手机
    @UtopiaSqlColumn(name = "CONTACT1_EMAIL") @NonNull private String contact1Email;    // 第一联络人邮箱
    @UtopiaSqlColumn(name = "CONTACT2_NAME") private String contact2Name;               // 第二联络人姓名
    @UtopiaSqlColumn(name = "CONTACT2_TEL") private String contact2Tel;                 // 第二联络人电话
    @UtopiaSqlColumn(name = "CONTACT2_MOB") private String contact2Mob;                 // 第二联络人手机
    @UtopiaSqlColumn(name = "CONTACT2_EMAIL") private String contact2Email;             // 第二联络人邮箱

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("A")
        };
    }

    public static String generateCacheKeyAll() {
        return CacheKeyGenerator.generateCacheKey(Vendor.class, "A");
    }

    public Vendor() {
    }

    public static Vendor newInstance(String cname, String contact1Name, String contact1Mob, String contact1Email) {
        Vendor vendor = new Vendor();
        vendor.setCname(cname);
        vendor.setContact1Name(contact1Name);
        vendor.setContact1Mob(contact1Mob);
        vendor.setContact1Email(contact1Email);
        return vendor;
    }

    @JsonIgnore
    public boolean isDisabledTrue() {
        return Boolean.TRUE.equals(disabled);
    }

    public Vendor withDisabled(Boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    /**
     * Create a mock instance for supporting unit tests.
     */
    public static Vendor mockInstance() {
        Vendor inst = new Vendor();
        inst.cname = RandomUtils.nextObjectId();
        inst.contact1Name = "";
        inst.contact1Mob = "";
        inst.contact1Email = "";
        return inst;
    }
}
