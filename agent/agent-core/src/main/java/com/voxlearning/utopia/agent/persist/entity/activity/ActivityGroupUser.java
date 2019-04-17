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
@DocumentCollection(collection = "agent_activity_group_user")
public class ActivityGroupUser implements CacheDimensionDocument {

    @DocumentId
    private String id;

    private String groupId;                 // 组团ID

    private Long joinUserId;               // 参与组团的用户ID
    private Boolean isLeader;              // 是否是组团发起者
    private Date joinTime;                 // 参与组团的时间

    private Long schoolId;                 // 对应学生所在学校， 多个学生的话，随机
    private String schoolName;

    private Date joinUserRegTime;          // 参与组团用户的注册时间

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("gid", this.groupId)
        };
    }
}
