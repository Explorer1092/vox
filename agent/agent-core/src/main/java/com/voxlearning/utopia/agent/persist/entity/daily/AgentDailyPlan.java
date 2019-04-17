package com.voxlearning.utopia.agent.persist.entity.daily;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.crm.api.constants.agent.CrmWorkRecordType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 *  日报计划实体
 *
 * @author deliang.che
 * @since  2018/9/19
 */

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_daily_plan")
public class AgentDailyPlan implements CacheDimensionDocument {

    private static final long serialVersionUID = -3004128416546727507L;
    @DocumentId
    private String id;

    private String dailyId;     //日报ID

    private Long userId;        //人员ID
    private Integer dailyTime;  //日报日期,格式：20180920

    private List<Long> schoolIdList;        //进校ID列表
    private List<String> meetingNameList;   //组会名称列表
    private List<Long> researcherIdList;    //教研员ID列表
    private List<Long> partnerIdList;       //陪同对象ID列表

    private String otherWork;               //其他工作

    private String content;                 //工作计划内容（用于列表展示）

    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
              newCacheKey("id", this.id),
              newCacheKey("did",this.dailyId),
              newCacheKey(new String[]{"uid","time"}, new Object[]{this.userId,this.dailyTime})
        };
    }
}

