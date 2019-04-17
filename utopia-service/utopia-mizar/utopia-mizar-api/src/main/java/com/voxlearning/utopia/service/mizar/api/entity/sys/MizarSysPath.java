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

package com.voxlearning.utopia.service.mizar.api.entity.sys;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Mizar系统路径表，用于权限管理
 *
 * @author Zhilong Hu
 * @serial
 * @since 2016-8-15
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "mizar_sys_path")
@UtopiaCacheRevision("20161207")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class MizarSysPath implements Serializable {
    private static final long serialVersionUID = 2269025113244531305L;

    @DocumentId private String id;

    private String appName;                      // 系统应用名
    private String pathName;                     // 系统路径名
    private String description;                  // 用户描述
    @DocumentFieldIgnore
    private List<MizarSysPathRole> authRoleList; // 角色列表

    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(MizarSysPath.class, id);
    }

    public static String ck_all() {
        return CacheKeyGenerator.generateCacheKey(MizarSysPath.class, "ALL");
    }

}