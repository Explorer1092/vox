package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * Agent用户的扩展信息
 * Created by yaguang.wang on 2016/9/27.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "agent_user_ext_info")
public class AgentUserExtInfo implements Serializable {
    private static final long serialVersionUID = 7139485226960874360L;
    @DocumentId
    private Long id;
    private Set<Long> hideTeachers;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    public static String ck_uid(Long id) {
        return CacheKeyGenerator.generateCacheKey(AgentUserExtInfo.class,  id);
    }
}
