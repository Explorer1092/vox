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
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Mizar Department Entity
 * Created by alex on 2016/8/15.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "mizar_department")
@UtopiaCacheRevision("20161208")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class MizarDepartment implements CacheDimensionDocument {
    private static final long serialVersionUID = 2219122137431659315L;

    @DocumentId private String id;

    private String departmentName;             // 部门名
    private String description;                // 部门介绍
    private Boolean disabled;                  // 是否有效
    //private Integer role;                    // 角色
    private List<Integer> ownRoles;            // 包含的角色

    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;

    @JsonIgnore
    public boolean isValid() {
        return Boolean.FALSE.equals(disabled);
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("ALL"),
                newCacheKey(id)
        };
    }
}
