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
 * ${app视频中的【热门】【精选活动】【搞笑集锦】用type进行区分}
 *
 * @author zhiqi.yao
 * @create 2018-04-19 16:19
 **/
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-misc")
@DocumentDatabase(database = "vox-ai")
@DocumentCollection(collection = "vox_ai_video_config")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180419")
public class AIVideoConfig implements Serializable {
    private static final long serialVersionUID = 8552435068987441912L;
    /**
     * 视频的id
     */
    @DocumentId
    public String id;
    /**
     *标题
     */
    public String title;
    /**
     *副标题
     */
    public String subhead;
    /**
     *视频描述
     */
    public String description;
    /**
     *视频url
     */
    public String videoUrl;
    /**
     *上传人姓名
     */
    public String uploaderName;
    /**
     *上传人头像
     */
    public String uploaderHead;
    /**
     * 点击视频跳转链接，广告用到
     */
    public String link;
    /**
     *视频的类型 0：今日精讲 1：热门 2:精选活动 3：搞笑集锦
     */
    public String type;
    /**
     * 附言
     */
    public String postscript;
    @DocumentCreateTimestamp
    private Date createDate;
    @DocumentUpdateTimestamp
    private Date updateDate;
    /**
     * 是否被删除
     */
    private Boolean disabled;

    //缓存key
    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(AIVideoConfig.class, id);
    }
}
