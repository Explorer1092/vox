package com.voxlearning.utopia.agent.persist.entity.task;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.agent.constants.AgentTaskType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 主任务
 * @author deliang.che
 * @date 2018-05-24
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_task_main")
public class AgentTaskMain implements CacheDimensionDocument {

    public static final Integer PERSONAL = 1;   //个人
    public static final Integer TEAM = 2;       //团队

    @DocumentId
    private String id;
    private String title;               //标题
    private Long userId;                //发布人ID
    private String userName;            //发布人姓名
    private Date endTime;               //截止时间
    private AgentTaskType taskType;     //类别
    private Integer rowNum;             //表格行数
    private String comment;             //任务说明

    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("id",this.id)
        };
    }
}
