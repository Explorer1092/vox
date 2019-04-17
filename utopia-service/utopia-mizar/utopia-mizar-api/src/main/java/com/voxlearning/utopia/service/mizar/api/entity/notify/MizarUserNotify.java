package com.voxlearning.utopia.service.mizar.api.entity.notify;

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

import java.util.Date;

/**
 * Mizar平台用户消息
 *
 * @author yuechen.wang
 * @date 2016/12/01
 */
@Setter
@Getter
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "mizar_user_notify")
@DocumentIndexes(value = {
        @DocumentIndex(def = "{'notifyId':1}", background = true),
        @DocumentIndex(def = "{'userId':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161009")
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@NoArgsConstructor
public class MizarUserNotify implements CacheDimensionDocument {

    private static final long serialVersionUID = -9036949906478145347L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.OBJECT_ID) private String id;
    private String notifyId;         // 消息ID
    private String userId;           // 用户ID
    private Boolean flag;            // 是否已读
    private Boolean disabled;        // 是否删除

    @DocumentCreateTimestamp private Date createAt;
    @DocumentUpdateTimestamp private Date updateAt;

    public MizarUserNotify(String userId, String notifyId) {
        this.userId = userId;
        this.notifyId = notifyId;
        this.flag = false;
        this.disabled = false;
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id),
                newCacheKey("N", notifyId),
                newCacheKey("U", userId)
        };
    }

    @JsonIgnore
    public boolean isRead() {
        return Boolean.TRUE.equals(flag);
    }

    @JsonIgnore
    public boolean isDisabledTrue() {
        return Boolean.TRUE.equals(disabled);
    }

}
