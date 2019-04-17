package com.voxlearning.utopia.agent.persist.entity.trainingcenter;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 *  文章用户关系实体
 *
 * @author deliang.che
 * @since  2018/7/6
 */

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_article_user")
public class AgentArticleUser implements CacheDimensionDocument {

    @DocumentId
    private String id;
    private String articleId;   //文章ID
    private Long userId;        //用户ID
    private Integer viewsNum;   //浏览次数
    private Date firstViewsTime;//首次浏览时间

    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("uid", this.userId),
                newCacheKey("aid", this.articleId)
        };
    }
}

