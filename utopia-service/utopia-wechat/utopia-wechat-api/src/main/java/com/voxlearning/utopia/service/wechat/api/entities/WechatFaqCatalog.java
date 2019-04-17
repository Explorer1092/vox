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
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Wechat FQA catalog data structure.
 * <p>
 * FIXME: DISABLED字段可以设置数据库缺省值FALSE
 *
 * @author xin.xin
 * @serial
 * @since 2014-04-18
 */
@UtopiaCacheExpiration(3600)
@DocumentTable(table = "VOX_WECHAT_FAQ_CATALOG")
public class WechatFaqCatalog extends AbstractDatabaseEntityWithDisabledField {
    private static final long serialVersionUID = -7279834427529305283L;

    @UtopiaSqlColumn(name = "NAME") @NonNull @Getter @Setter private String name;
    @UtopiaSqlColumn(name = "PICURL") @Getter @Setter private String picUrl;
    @UtopiaSqlColumn(name = "DESCRIPTION") @Getter @Setter private String description;
    @UtopiaSqlColumn(name = "TYPE") @Getter @Setter private Integer type;

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(WechatFaqCatalog.class, id);
    }

    public static String ck_type(Integer type) {
        return CacheKeyGenerator.generateCacheKey(WechatFaqCatalog.class, "type", type);
    }

    public static WechatFaqCatalog newInstance(String name, Integer type) {
        if (name == null) throw new NullPointerException();
        if (type == null) throw new NullPointerException();
        WechatFaqCatalog inst = new WechatFaqCatalog();
        inst.setDisabled(false);
        inst.setName(name);
        inst.setType(type);
        return inst;
    }
}
