package com.voxlearning.utopia.agent.persist.entity.trainingcenter;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 *  文章实体
 *
 * @author deliang.che
 * @since  2018/7/6
 */

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_article")
public class AgentArticle implements CacheDimensionDocument {

    public static final Integer ONLINE = 1; //发布
    public static final Integer OFFLINE = 2;//下线

    @DocumentId
    private String id;

    private String title;//标题
    private String oneLevelColumnId;//一级栏目ID
    private String twoLevelColumnId;//二级栏目ID

    private String coverImgUrl;//封面

    private List<AgentRoleType> roleTypeList;  // 发布角色列表
    private List<Long> groupIdList;            // 发布部门列表

    private Boolean openInAPP;//是否跳至APP内打开

    private String content;//正文

    private Integer viewsNumAll;//总浏览次数
    private Integer viewsNumTj;//天玑内浏览次数
    private Integer servicePersonNum;//天玑送达人数

    private Long publisherId;//发布人ID
    private Date publishTime;//发布时间
    private Boolean isPublish;//是否发布
    private String jumpUrl;   //跳转URL

    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
            newCacheKey("tcid",this.twoLevelColumnId)
        };
    }
}

