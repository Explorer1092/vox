package com.voxlearning.utopia.service.newhomework.api.entity;

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
import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/1/17
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-siberia")
@DocumentDatabase(database = "vox-homework")
@DocumentCollection(collection = "homework_black_white_list")
@DocumentIndexes({
        @DocumentIndex(def = "{'businessType':1,'idType':1,'blackWhiteId':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190125")
public class HomeworkBlackWhiteList implements Serializable {
    private static final long serialVersionUID = 929873936627177228L;

    @DocumentId
    private String id;
    private String businessType;     // 业务类型(VACATION_DAY_PACKAGE, ...)
    private String idType;           // ID类型(STUDENT_ID, GROUP_ID)
    private String blackWhiteId;     // 黑(白)名单ID
    public Boolean disabled;         // 默认false，删除true
    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;

    public static String generateId(String businessType, String idType, String blackWhiteId) {
        return StringUtils.join(businessType, "-", idType, "-", blackWhiteId);
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(HomeworkBlackWhiteList.class, id);
    }

    public static String ck_bi(String businessType, String idType) {
        return CacheKeyGenerator.generateCacheKey(HomeworkBlackWhiteList.class,
                new String[]{"bt", "it"},
                new Object[]{businessType, idType});
    }

}
