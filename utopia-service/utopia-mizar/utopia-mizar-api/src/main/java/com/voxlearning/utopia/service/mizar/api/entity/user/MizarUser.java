package com.voxlearning.utopia.service.mizar.api.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Mizar User Entity
 * Created by alex on 2016/8/15.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "mizar_user")
@DocumentIndexes({
        @DocumentIndex(def = "{'accountName':1}", background = true),
})
@UtopiaCacheRevision("20161215")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class MizarUser implements CacheDimensionDocument {
    private static final long serialVersionUID = 2209172137431659375L;

    @DocumentId private String id;

    private String accountName;             // 用户登录名
    private String realName;                // 用户真实姓名
    private String password;                // 用户密码
    private String passwordSalt;            // 用户密码Salt
    private String userComment;             // 备注
    private String mobile;                  // 电话
    //private List<Integer> userRoles;      // 用户角色列表
    private Integer status;                 // 用户状态，0:新建，强制更新密码，1:有效，9:冻结/关闭
    private String portrait;                // 用户头像
    private String talkFunId;               // 欢拓后台ID

    @DocumentFieldIgnore
    private List<MizarDepartment> departments;// 所属的组列表
    @DocumentFieldIgnore
    private List<String> groupRoleIds;        // 部门角色列表，格式"部门id-角色"
    @DocumentFieldIgnore
    private List<Integer> roleIds;            // 角色列表

    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;

    @JsonIgnore
    public boolean isValid() {
        return status != null && status != 9;
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("ALL"),
                newCacheKey(id),
                newCacheKey("ACCOUNT", accountName),
                newCacheKey("MOBILE", mobile)
        };
    }

}
