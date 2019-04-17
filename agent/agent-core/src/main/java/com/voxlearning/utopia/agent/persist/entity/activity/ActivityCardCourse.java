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
@DocumentCollection(collection = "agent_activity_card_course")
public class ActivityCardCourse implements CacheDimensionDocument {

    @DocumentId
    private String id;

    private String cardNo;                  // 礼品卡卡号

    private Long studentId;

    private String courseId;                 // 课程ID
    private String courseName;                 // 课程名称


    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;


    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"sid", "cid"}, new Object[]{this.studentId, this.courseId}),
                newCacheKey("cn", this.cardNo)
        };
    }
}
