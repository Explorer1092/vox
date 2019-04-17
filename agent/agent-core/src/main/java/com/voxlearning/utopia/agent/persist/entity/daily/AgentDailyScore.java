package com.voxlearning.utopia.agent.persist.entity.daily;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.agent.constants.AgentDailyScoreIndex;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 *  日报得分实体
 *
 * @author deliang.che
 * @since  2018/11/2
 */

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_daily_score")
public class AgentDailyScore implements CacheDimensionDocument {

    private static final long serialVersionUID = 4700896090764762564L;
    @DocumentId
    private String id;

    private String dailyId;     //日报ID
    private Integer dailyTime;  //日报日期,格式：20180920

    private Long userId;        //人员ID

    private AgentDailyScoreIndex index; //指标
    private Double weight;              //权重
    private Double ratio;               //系数
    private Double score;               //得分

    private List<AgentDailySubScore> subScoreList;//子指标得分信息

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
              newCacheKey(new String[]{"uid","time"},new Object[]{this.userId,this.dailyTime}),
              newCacheKey(new String[]{"uid","time","index"},new Object[]{this.userId,this.dailyTime,this.index})
        };
    }
}

