package com.voxlearning.utopia.service.vendor.api.entity;

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
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Created by Summer on 2017/2/10.
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-vendor")
@DocumentDatabase(database = "vox-vendor")
@DocumentCollection(collection = "vox_vendor_apps_user_ref_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 604800)
@UtopiaCacheRevision("20170220")
public class
VendorAppsUserRef implements CacheDimensionDocument {

    private static final long serialVersionUID = -8694309898229579712L;
    @DocumentId
    private String id;
    private Long appId;
    private String appKey;
    private Long userId;
    private String sessionKey;
    private Long oldId;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("UID", userId)
        };
    }

    public static String generateId(Long userId) {
        if (userId == null) {
            throw new RuntimeException();
        }
        return userId + "-" + new ObjectId().toString();
    }

    public static boolean isLegalSessionKey(String sk) {
        if (sk == null || sk.trim().length() == 0) {
            return false;
        }
        for (int i = 0; i < sk.length(); i++) {
            char c = sk.charAt(i);
            if (c >= '0' && c <= '9') {
                continue;
            }
            if (c >= 'a' && c <= 'f') {
                continue;
            }
            if (c >= 'A' && c <= 'F') {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Create a mock instance for supporting unit tests.
     */
    public static VendorAppsUserRef mockInstance() {
        VendorAppsUserRef inst = new VendorAppsUserRef();
        inst.appId = 0L;
        inst.appKey = new ObjectId().toString();
        inst.userId = 0L;
        inst.sessionKey = new ObjectId().toString();
        return inst;
    }
}
