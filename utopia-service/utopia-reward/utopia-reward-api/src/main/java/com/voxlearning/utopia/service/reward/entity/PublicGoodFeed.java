package com.voxlearning.utopia.service.reward.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.reward.api.enums.LikeSourceEnum;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Objects;

/**
 * 公益活动 动态(点赞 留言)
 */
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-public-good")
@DocumentCollection(collection = "vox_public_good_feed_{}", dynamic = true)
@UtopiaCacheRevision("20180621")
public class PublicGoodFeed implements CacheDimensionDocument {

    private static final long serialVersionUID = 7426131009686814378L;

    @DocumentId
    private String id;
    private Long userId;                            // 用户
    private Long activityId;                        // 活动ID
    private Long opId;                              // 操作人
    private String opName;                          // 操作人名称
    private Type type;                              // 类型  点赞 or 留言 or 阶段性成果
    private String comments;                        // 留言内容 成果内容
    private LikeSourceEnum sourceEnum;              // 平台
    private Long count;                             // 三方平台点赞数

    @DocumentCreateTimestamp
    private Date createTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{"UID", String.valueOf(userId)};
    }

    public PublicGoodFeed generateId() {
        Objects.requireNonNull(userId);
        id = userId + "-" + RandomUtils.nextObjectId();
        return this;
    }

    public static enum Type {
        LIKE,          // 点赞
        COMMENTS,      // 留言
        MILESTONES     // 里程碑
    }
}
