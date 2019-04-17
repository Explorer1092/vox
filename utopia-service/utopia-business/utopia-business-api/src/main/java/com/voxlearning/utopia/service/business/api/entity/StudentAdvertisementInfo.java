package com.voxlearning.utopia.service.business.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author janko
 * @date 2016/10/30
 * @desc
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-bigdata")
@DocumentCollection(collection = "student_advertisement_info")
@UtopiaCacheRevision("20180328")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class StudentAdvertisementInfo implements Serializable {

    private static final long serialVersionUID = -212638318859812176L;

    @DocumentId private String id;
    @DocumentField Long userId;
    @DocumentField String clickUrl;        //跳转链接
    @DocumentField String slotId;        //广告位id
    @DocumentField String advertisementId;        //广告id
    @DocumentField String messageText;    //广告信息
    @DocumentField String imgUrl;
    @DocumentField Integer rank;
    @DocumentField String btnContent;
    @DocumentField Long showStartTime;
    @DocumentField Long showEndTime;


    public String cacheKeyByUserId(Long userId) {
        return CacheKeyGenerator.generateCacheKey(StudentAdvertisementInfo.class, "UID", userId);
    }
}
