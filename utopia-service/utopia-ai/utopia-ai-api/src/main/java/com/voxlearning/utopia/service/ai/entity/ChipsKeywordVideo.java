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
@DocumentCollection(collection = "vox_chips_keyword_video")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190326")
public class ChipsKeywordVideo implements Serializable {
    private static final long serialVersionUID = 4324541431007914546L;
    @DocumentId
    private String id;//关键词
    private String video;//视频讲解地址
    @DocumentField("DISABLED")
    private Boolean disabled;
    @DocumentCreateTimestamp
    private Date createDate;
    @DocumentUpdateTimestamp
    private Date updateDate;
}
