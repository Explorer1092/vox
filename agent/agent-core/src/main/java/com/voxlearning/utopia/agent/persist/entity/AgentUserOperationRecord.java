package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.agent.constants.AgentUserOperationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 *  用户的操作记录（天玑天权内部使用）
 *
 * @author song.wang
 * @date 2018/1/11
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_user_operation_record")
public class AgentUserOperationRecord implements CacheDimensionDocument {

    private static final long serialVersionUID = 433126859183569310L;

    @DocumentId
    private String id;
    private String dataId ;    //通用ID
    private Long schoolId;
    private String schoolName;
    private Long teacherId;
    private String teacherName;
    private Long operatorId;
    private String operatorName;
    private AgentUserOperationType operationType;
    private String note;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("type", operationType),
                newCacheKey(new String[]{"type", "tid"}, new Object[]{operationType, teacherId}),
                newCacheKey("did", dataId)
        };
    }
}
