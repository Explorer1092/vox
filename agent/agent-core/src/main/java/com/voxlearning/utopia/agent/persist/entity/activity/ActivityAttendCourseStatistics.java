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
@DocumentCollection(collection = "agent_activity_attend_course_statistics")
public class ActivityAttendCourseStatistics implements CacheDimensionDocument {

    @DocumentId
    private String id;

    private String activityId;               // 活动ID

    private Long userId;
    private String userName;
    private Integer day;                     // 日期

    private Integer firstAttendStuCount;     // 首次上该活动课的学生数
    private Integer attendStuCount;          // 当日上课学生数(学生参加多次课，不重复计数)
    private Integer meetConditionStuCount;   // 上课并且满足市场指定条件的学生数


    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"aid", "uid", "d"}, new Object[]{this.activityId, this.userId, this.day}),
        };
    }
}
