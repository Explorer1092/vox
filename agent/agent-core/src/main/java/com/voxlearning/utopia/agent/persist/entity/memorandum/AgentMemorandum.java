package com.voxlearning.utopia.agent.persist.entity.memorandum;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import com.voxlearning.utopia.agent.constants.AgentMemorandumGenre;
import com.voxlearning.utopia.agent.constants.MemorandumType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yaguang.wang
 * on 2017/5/10.
 */

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "vox_memorandum")
public class AgentMemorandum implements Serializable, TimestampTouchable {
    private static final long serialVersionUID = -719004793784342203L;

    @DocumentId private String id;
    private Boolean disabled;                                   //是否禁用
    @DocumentCreateTimestamp private Date createTime;           //创建时间
    @DocumentUpdateTimestamp private Date updateTime;           //更新时间
    private Long schoolId;
    private Long teacherId;
    private Long createUserId;
    private String content;
    private String url;
    private MemorandumType type;
    private Date writeTime;                                     // 记录时间

    private String intoSchoolRecordId;                          // 进校记录id
    private AgentMemorandumGenre genre;                         // 老师或者学校


    public static String ck_sid(Long schoolId) {
        return CacheKeyGenerator.generateCacheKey(AgentMemorandum.class, "sid", schoolId);
    }

    public static String ck_uid(Long createUserId) {
        return CacheKeyGenerator.generateCacheKey(AgentMemorandum.class, "uid", createUserId);
    }

    public static String ck_tid(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(AgentMemorandum.class, "tid", teacherId);
    }

    @JsonIgnore
    public boolean isIntoSchool() {                   // 是否是进校填写的记录
        return StringUtils.isNoneBlank(intoSchoolRecordId);
    }
}
