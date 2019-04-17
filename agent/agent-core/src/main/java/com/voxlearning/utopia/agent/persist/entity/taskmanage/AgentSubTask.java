package com.voxlearning.utopia.agent.persist.entity.taskmanage;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 子任务
 * @author deliang.che
 * @since  2018-11-13
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_sub_task")
@DocumentIndexes({
        @DocumentIndex(def = "{'mainTaskId':1,'regionCode':1,}", background = true),
        @DocumentIndex(def = "{'mainTaskId':1,'schoolId':1,}", background = true)
})
public class AgentSubTask implements CacheDimensionDocument {


    @DocumentId
    private String id;
    private String mainTaskId;          //主任务ID
    private Long schoolId;              //学校ID
    private String schoolName;          //学校名称
    private Long teacherId;             //老师ID
    private String teacherName;         //老师姓名

    private Boolean ifFollowUp; //是否已跟进
    private Boolean ifHomework; //是否布置作业
    private Date homeworkTime;  //布置作业时间

    private Integer regionCode; //学校区域编码

    private String schoolLevel;//学校阶段

    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("id", this.id),
                newCacheKey("mid", this.mainTaskId),
                newCacheKey(new String[]{"mid", "schoolId"}, new Object[]{this.mainTaskId, this.schoolId}),
                newCacheKey(new String[]{"mid", "regionCode"}, new Object[]{this.mainTaskId, this.regionCode})
        };
    }
}
