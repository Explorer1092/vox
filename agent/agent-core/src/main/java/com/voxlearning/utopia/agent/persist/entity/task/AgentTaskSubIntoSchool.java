package com.voxlearning.utopia.agent.persist.entity.task;

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
 * 子任务-进校维护
 * @author deliang.che
 * @date 2018-05-24
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_task_sub_intoschool")
public class AgentTaskSubIntoSchool implements CacheDimensionDocument {


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

    private Boolean isIntoSchool;   //是否进校
    private Boolean isVisitTeacher; //是否进校拜访老师
    private Boolean isHomework;     //是否布置作业
    private Date intoSchoolTime;    //进校时间
    private Date visitTeacherTime;  //拜访老师时间
    private Date homeworkTime;      //布置作业时间

    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("mid", this.mainTaskId)
        };
    }
}
