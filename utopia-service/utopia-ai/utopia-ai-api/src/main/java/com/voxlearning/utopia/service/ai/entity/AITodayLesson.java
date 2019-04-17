package com.voxlearning.utopia.service.ai.entity;

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
import java.util.Date;


/**
 * 轻运营班主任后台
 * @see http://wiki.17zuoye.net/pages/viewpage.action?pageId=40404951
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-misc")
@DocumentDatabase(database = "vox-ai")
@DocumentCollection(collection = "vox_ai_today_lesson")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180803")
public class AITodayLesson implements Serializable {


    private static final long serialVersionUID = -1278014005151684169L;

    @DocumentId
    private String id;
    private String bookId;
    private String unitId;
    private String title;
    private String subject;
    private String videoDesc;
    private String videoUrl;
    private String videoImg;
    private String videoContent;
    private String eggContent;
    private String eggVideoUrl;
    private String eggImg;
    private String hotContent;
    private String summaryContent;
    private String summaryLink;
    private String tipsVideoUrl;
    private String tipsVideoImg;
    private String tipsNextClass;

    @DocumentCreateTimestamp
    private Date createDate; // 做题时间
    @DocumentUpdateTimestamp
    private Date updateDate; // 更新时间

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(AITodayLesson.class, id);
    }

}
