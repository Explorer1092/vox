package com.voxlearning.utopia.agent.persist.entity.publish;

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
import java.util.List;

/**
 *
 *
 * @author song.wang
 * @date 2018/4/23
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_publish")
public class AgentPublish implements CacheDimensionDocument {

    public static final Integer STATUS_OFFLINE = 2;
    public static final Integer STATUS_ONLINE = 1;

    @DocumentId
    private String id;

    private String title;
    private Long userId;                // 创建者ID
    private String userName;            // 创建者名称
    private Long operatorId;           // 操作人ID
    private String operatorName;       // 操作人名称
    private Integer status;             //状态（2：已下线  1：已发布）
    private List<String> dataTitleList;

    private String comment;

    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(this.id)
        };
    }
}
