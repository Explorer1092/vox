package com.voxlearning.utopia.agent.persist.entity.taskmanage;

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
 * 主任务
 * @author deliang.che
 * @since  2018-11-13
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_main_task")
public class AgentMainTask implements CacheDimensionDocument {

    @DocumentId
    private String id;
    private String title;               //标题
    private Integer teacherNum;         //老师数量
    private Date endTime;               //截止时间
    private String comment;             //任务说明
    private Long publisherId;           //发布人ID
    private String publisherName;       //发布人姓名

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
