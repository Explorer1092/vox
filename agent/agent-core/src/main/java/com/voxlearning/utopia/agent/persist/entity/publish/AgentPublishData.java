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
@DocumentCollection(collection = "agent_publish_data")
public class AgentPublishData implements CacheDimensionDocument {

    @DocumentId
    private String id;
    private String publishId;
    private Long groupId;
    private String groupName;
    private Long userId;
    private String userName;
    private String category;

    private List<Object> dataList;

    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;


    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
        };
    }
}
