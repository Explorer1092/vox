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
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Mizar系统路径角色关联表，用于权限管理
 * Created by alex on 2016/8/15.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "mizar_sys_path_role")
@UtopiaCacheRevision("20161209")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class MizarSysPathRole implements Serializable {
    private static final long serialVersionUID = 2269025113244531306L;

    @DocumentId private String id;

    //private Integer role;             // 角色ID
    private String roleGroupId;         // 角色ID，格式为"部门id-角色"
    private String path;                // 系统路径ID

    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;

    public static String ck_role(String roleId) {
        return CacheKeyGenerator.generateCacheKey(MizarSysPathRole.class, "R", roleId);
    }

    public static String ck_path(String pathId) {
        return CacheKeyGenerator.generateCacheKey(MizarSysPathRole.class, "P", pathId);
    }

    public static String ck_all() {
        return CacheKeyGenerator.generateCacheKey(MizarSysPathRole.class, "ALL");
    }

}
