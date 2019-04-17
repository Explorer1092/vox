package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * \* Created: liuhuichao
 * \* Date: 2019/1/3
 * \* Time: 7:53 PM
 * \* Description:口语交际首页推介记录
 * \
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newhomework")
@DocumentCollection(collection = "oral_communication_recommend_record")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190103")
public class OralCommunicationRecommendRecord implements Serializable {

    private static final long serialVersionUID = 7893130699590894080L;

    @DocumentId
    private String id;

    private Long teacherId;                                     // 教师id

    private Subject subject;                                    // 学科

    private Integer year;                                       // 学年

    private Term term;                                          // 学期

    private Map<String, String> oralCommunicationRecommendInfo;      // key:口语交际stone_id; v:推介day

    @DocumentCreateTimestamp
    private Long createTimestamp;                               // 创建时间
    @DocumentUpdateTimestamp
    private Long updateTimestamp;                               // 修改时间

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(OralCommunicationRecommendRecord.class, id);
    }
}
