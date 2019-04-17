package com.voxlearning.utopia.service.mizar.api.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.mizar.api.constants.MizarUserRoleType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Mizar User Department Entity
 * Created by alex on 2016/8/16.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "mizar_user_department")
@DocumentIndexes({
        @DocumentIndex(def = "{'userId':1}", background = true),
        @DocumentIndex(def = "{'departmentId':1}", background = true)
})
@UtopiaCacheRevision("20161208")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class MizarUserDepartment implements Serializable {
    private static final long serialVersionUID = 2209172134431659375L;

    @DocumentId private String id;
    private String userId;                  // 用户登录ID
    private String departmentId;            // 部门ID
    private Boolean disabled;               // 是否有效
    private List<Integer> roles;            // 角色

    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;

    public MizarUserDepartment(String userId, String departmentId) {
        this.userId = userId;
        this.departmentId = departmentId;
        this.disabled = false;
    }

    public static String ck_user(String userId) {
        return CacheKeyGenerator.generateCacheKey(MizarUserDepartment.class, "uid", userId);
    }

    public static String ck_department(String departmentId) {
        return CacheKeyGenerator.generateCacheKey(MizarUserDepartment.class, "did", departmentId);
    }

    @JsonIgnore
    public boolean isDisabledTrue() {
        return Boolean.TRUE.equals(disabled);
    }

    public boolean containsRole(MizarUserRoleType role) {
        return role == null || (roles != null && roles.contains(role.getId()));
    }

}
