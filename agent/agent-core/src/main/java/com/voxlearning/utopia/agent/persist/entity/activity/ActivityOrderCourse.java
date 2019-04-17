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

/**
 * 保存平台订单与课程的对应关系
 */

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_activity_order_course")
public class ActivityOrderCourse implements CacheDimensionDocument {

    @DocumentId
    private String id;

    private String orderId;                  // 订单ID

    private Long studentId;

    private String courseId;                 // 课程ID
    private String courseName;                 // 课程名称

    private Boolean firstActivation;         // 课程是否是首次激活

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"sid", "cid"}, new Object[]{this.studentId, this.courseId}),
                newCacheKey("oid", this.orderId)
        };
    }
}
