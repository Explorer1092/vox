package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
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
import java.util.List;

/**
 * AgentNeedFollowUp
 *
 * @author song.wang
 * @date 2016/7/28
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_need_follow_up")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160902")
public class AgentNeedFollowUp implements Serializable {

    private static final long serialVersionUID = -2835030438816211503L;
    @DocumentId
    private String id;
    private Integer day;
    private Integer type; // 1: 新注册老师  2： 不活跃老师  3: 满足条件未认证老师
    private Long schoolId;
    private String schoolName;
    private List<AgentNeedFollowTeacher> teacherList;
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;


    public static String ck_sid_day(Long schoolId, Integer day) {
        return CacheKeyGenerator.generateCacheKey(AgentNeedFollowUp.class,
                new String[]{"schoolId", "day"},
                new Object[]{schoolId, day});
    }



}
