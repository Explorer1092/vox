package com.voxlearning.utopia.service.afenti.api.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * 我的绘本 实体
 * @author haitian.gan
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-newworld")
@DocumentDatabase(database = "vox-picbook")
@DocumentCollection(collection = "vox_user_picbook_{}",dynamic = true)
@UtopiaCacheRevision("20180227")
@EqualsAndHashCode(of = {"userId","bookId"})
public class UserPicBook implements CacheDimensionDocument {

    private static final long serialVersionUID = 5254694120533886517L;

    // 模块信息中包含的属性
    public static final String[] MODULE_ATTRS = new String[]{"pageId","questionId","finish","module"};

    @DocumentId private String id;
    private Long userId;
    private String bookId;                              // 绘本ID
    private String type;                                // appKey即OrderProductServiceType
    private Boolean read;                               // 阅读过
    private Boolean disabled;                           // 如果退款，删除了的就是False
    private Long buyTime;                               // 购买时间
    private Integer score;                              // 分数

    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{newCacheKey("userId",userId)};
    }

    public UserPicBook generateId() {
        Objects.requireNonNull(userId);
        id = userId + "-" + RandomUtils.nextObjectId();
        return this;
    }

}
