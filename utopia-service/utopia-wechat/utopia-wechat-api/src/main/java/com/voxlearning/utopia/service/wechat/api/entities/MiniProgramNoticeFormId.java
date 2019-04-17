package com.voxlearning.utopia.service.wechat.api.entities;


import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-xcx")
@DocumentCollection(collection = "mini_program_notice_form")
@DocumentIndexes({
        @DocumentIndex(def = "{'openId':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class MiniProgramNoticeFormId implements Serializable {


    private static final long serialVersionUID = 3625028917932470448L;


    @DocumentId
    private String id;

    private String  openId;  // open id

    private String formId; // form id


    private MiniProgramType type;

    @DocumentCreateTimestamp
    private Date createTime;


    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(MiniProgramNoticeFormId.class, id);
    }

    public static String ck_openId(String uid) {
        return CacheKeyGenerator.generateCacheKey(MiniProgramNoticeFormId.class, "OPENID", uid);
    }

    public static String ck_type(MiniProgramType type) {
        return CacheKeyGenerator.generateCacheKey(UserMiniProgramCheck.class, "TYPE", type);
    }

}
