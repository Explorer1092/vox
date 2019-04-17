package com.voxlearning.utopia.agent.persist.entity.activity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
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
 * 保存学生上课数据
 */

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_activity_attend_course")
@UtopiaCacheRevision("20190311")
public class ActivityAttendCourse implements CacheDimensionDocument {

    @DocumentId
    private String id;

    private String activityId;
    private String relatedId;           // 关联的订单ID, 礼品卡卡号等

    private Long studentId;
    private String courseId;
    private Date attendTime;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"aid", "rid", "sid", "cid"}, new Object[]{this.activityId, this.relatedId, this.studentId, this.courseId})
        };
    }
}
