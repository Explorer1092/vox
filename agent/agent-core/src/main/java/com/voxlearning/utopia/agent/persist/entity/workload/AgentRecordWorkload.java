package com.voxlearning.utopia.agent.persist.entity.workload;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentWorkRecordType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 保存市场每条工作记录的工作量T
 *
 * @author song.wang
 * @date 2018/6/13
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_record_workload")
public class AgentRecordWorkload implements CacheDimensionDocument {
    private static final long serialVersionUID = 2688375191337256960L;

    @DocumentId
    private String id;               // 对应crmWorkRecord的id（历史数据）

    private String workRecordId;
    private AgentWorkRecordType workRecordType;

    private Double workload;          // 工作量T


    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(this.id),
                newCacheKey(new String[]{"wrId","wrType"},new Object[]{this.workRecordId,this.workRecordType})
        };
    }
}
