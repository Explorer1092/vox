package com.voxlearning.utopia.service.crm.api.entities.agent.work;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.crm.api.constants.agent.AccompanyBusinessType;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 陪同
 *
 * @author song.wang
 * @date 2018/12/5
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_work_record_accompany")
@UtopiaCacheRevision("20181214")
public class WorkRecordAccompany implements CacheDimensionDocument {
    private static final long serialVersionUID = 4326946941588413316L;
    @DocumentId
    private String id;

    private AccompanyBusinessType businessType;         // 陪同业务类型
    private String businessRecordId;                             // 关联的业务ID (进校ID, 组会ID 等)

    private String signInRecordId;               // 签到记录ID

    private List<String> photoUrls;     // 现场照片

    private String purpose;    //陪同目的

    private String content;
    private String result;

    private List<String> evaluationRecordList;       // 评价记录ID列表

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
                newCacheKey("bid", businessRecordId)
        };
    }
}
