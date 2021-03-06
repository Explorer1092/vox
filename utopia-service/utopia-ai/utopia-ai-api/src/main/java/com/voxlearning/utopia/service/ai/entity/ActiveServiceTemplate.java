package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author guangqing
 * @since 2019/1/4
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-chips")
@DocumentCollection(collection = "vox_active_service_template")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190107")
public class ActiveServiceTemplate implements Serializable {

    private static final long serialVersionUID = 1434545556790651243L;
    @DocumentId
    private String id;//qid
    private String json;
    //{userName},{question},{keyword}
    private String learnSummary;
    private String defaultSummary;
    private List<ActiveServicePronunciation> pronunciationList;
    private List<ActiveServiceGrammar> grammarList;
    private List<ActiveServiceKnowledge> knowledgeList;
    @DocumentCreateTimestamp
    private Date createDate; // 做题时间
    @DocumentUpdateTimestamp
    private Date updateDate; // 更新时间

    @Override
    public String toString() {
        return "ActiveServiceTemplate{" +
                "id='" + id + '\'' +
                ", json='" + json + '\'' +
                ", learnSummary='" + learnSummary + '\'' +
                ", pronunciationList=" + pronunciationList +
                ", grammarList=" + grammarList +
                ", knowledgeList=" + knowledgeList +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                '}';
    }
}
