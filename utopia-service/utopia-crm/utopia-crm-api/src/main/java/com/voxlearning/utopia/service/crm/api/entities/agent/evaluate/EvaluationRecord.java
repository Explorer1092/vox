package com.voxlearning.utopia.service.crm.api.entities.agent.evaluate;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.crm.api.constants.agent.EvaluationBusinessType;
import com.voxlearning.utopia.service.crm.api.constants.agent.EvaluationIndicator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 *
 *
 * @author song.wang
 * @date 2018/12/14
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_evaluation_record")
@UtopiaCacheRevision("20181214")
public class EvaluationRecord implements CacheDimensionDocument {
    private static final long serialVersionUID = -4174312657484627557L;
    @DocumentId
    private String id;

    private EvaluationBusinessType businessType;            // 评价的业务类型
    private String businessRecordId;                             // 关联的业务ID (进校ID, 组会ID 等)

    private EvaluationIndicator indicator;                  // 评价指标
    private Integer result;                              // 评价结果

    private Long targetUserId;                           // 关联的用户ID
    private String targetUserName;                       // 关联的用户名

    private Long userId;
    private String userName;

    private Date evaluateTime;

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
