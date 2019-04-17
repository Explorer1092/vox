package com.voxlearning.utopia.agent.persist.entity.schoollastworkrecord;

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
 * 学校与最近一次拜访时间关系表
 *
 * @author deliang.che
 * @since  2019/3/4
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_school_last_work_record")
public class AgentSchoolLastWorkRecord implements CacheDimensionDocument {

    private static final long serialVersionUID = -8078491685874952278L;
    @DocumentId
    private String id;

    private String workRecordId;

    private Long schoolId;

    private Date lastVisitTime;     // 最近一次拜访日期

    private Long userId;
    private String userName;


    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(this.id)
        };
    }
}
