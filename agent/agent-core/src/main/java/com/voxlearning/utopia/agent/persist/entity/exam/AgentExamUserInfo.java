package com.voxlearning.utopia.agent.persist.entity.exam;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
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
 * 大考分配到人员详情
 * Date:     2018/9/17 21:18
 * Description: 大考分配情况
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_exam_user_info")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20180926")
public class AgentExamUserInfo implements CacheDimensionDocument {
    @DocumentId
    private String id;
    private String examId;   //考试id
    private Long userId;      // 分配专员id
    private String realName;  // 姓名
    private String level;      // 评价等级
    private Boolean evaluateState;      // 是否评价
    private String desc;//评价描述
    private String name;
    private Integer grade;
    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("examId", this.examId),
                newCacheKey(new String[]{"examId", "userId"}, new Object[]{examId, userId}),
        };
    }
}
