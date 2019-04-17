package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.utopia.service.ai.constant.PageViewType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author guangqing
 * @since 2019/1/4
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-chips")
@DocumentCollection(collection = "vox_chips_user_page_view_log")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190319")
public class ChipsUserPageViewLog implements Serializable {

    private static final long serialVersionUID = 4934210444306222320L;
    public static final String SEP = "-";

    @DocumentId
    private String id;
    private Long userId;
    //每个用户只存储一条的唯一标识;和userId 组成唯一索引;
    //articleId
    private String uniqueKey;
    private String json;//每种类型需要存储的数据结构
    private PageViewType type;
    @DocumentField("DISABLED")
    private Boolean disabled;
    @DocumentCreateTimestamp
    private Date createDate;
    @DocumentUpdateTimestamp
    private Date updateDate;


    public static String genId(Long userId, String uniqueKey) {
        return userId + SEP + uniqueKey;
    }
}
