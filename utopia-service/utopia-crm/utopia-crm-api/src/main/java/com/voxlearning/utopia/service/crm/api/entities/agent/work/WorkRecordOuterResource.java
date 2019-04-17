package com.voxlearning.utopia.service.crm.api.entities.agent.work;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
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
 * 上层资源人员记录
 *
 * @author song.wang
 * @date 2018/12/5
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_work_record_outer_resource")
@UtopiaCacheRevision("20181214")
public class WorkRecordOuterResource implements CacheDimensionDocument {

    private static final long serialVersionUID = 2497284231702608804L;
    @DocumentId
    private String id;
    private Long outerResourceId;    //上层资源ID
    private String outerResourceName;//上层资源名称

    private String workRecordId;
    private AgentWorkRecordType workRecordType;

    private String content;
    private String result;

    private Long userId;
    private String userName;
    private Date workTime;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;
    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("orid",outerResourceId)
        };
    }
}
