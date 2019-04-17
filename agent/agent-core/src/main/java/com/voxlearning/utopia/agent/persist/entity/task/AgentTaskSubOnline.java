package com.voxlearning.utopia.agent.persist.entity.task;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.agent.constants.AgentTaskFeedbackType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 子任务-线上维护
 * @author deliang.che
 * @date 2018-05-24
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_task_sub_online")
public class AgentTaskSubOnline implements CacheDimensionDocument {


    @DocumentId
    private String id;
    private String mainTaskId;          //主任务ID
    private Long operatorId;            //执行人ID
    private String operatorName;        //执行人姓名
    private Long schoolId;              //学校ID
    private String schoolName;          //学校名称
    private Long teacherId;             //老师ID
    private String teacherName;         //老师姓名
    private String comment;             //备注

    private Date feedbackTime;          //维护时间
    private AgentTaskFeedbackType feedbackType; //跟进方式
    private String feedbackResult;              //跟进结果
    private Boolean isFeedback;//是否已维护
    private Boolean isHomework;//是否布置作业
    private Date homeworkTime;//布置作业时间

    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("id", this.id),
                newCacheKey("mid", this.mainTaskId)
        };
    }
}
