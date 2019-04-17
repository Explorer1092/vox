package com.voxlearning.utopia.agent.persist.entity.activity;

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

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_live_enrollment_user_positive_statistics")
public class LiveEnrollmentUserPositiveStatistics implements CacheDimensionDocument {

    @DocumentId
    private String id;

    private Long userId;
    private String userName;
    private Integer day;
    //小学正价课数据
    private Double orderNum;
    private Long payPrice;
    private Double refundNum;
    private Long refundPrice;

    //中学正价课数据
    private Double middleOrderNum;
    private Long middlePayPrice;
    private Double middleRefundNum;
    private Long middleRefundPrice;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"uid", "d"}, new Object[]{this.userId, day})
        };
    }
}
