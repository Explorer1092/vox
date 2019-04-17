package com.voxlearning.utopia.service.psr.entity.midtermreview;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/10/10.
 */

@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = "mongo-question")
@DocumentDatabase(database = "vox-question")
@DocumentCollection(collection = "bigdata_midenglish_midtermreview")
@DocumentIndexes({
        @DocumentIndex(def = "{'group_id':1}", background = true),
        @DocumentIndex(def = "{'book_id':1}", background = true),
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161010")
public class EnglishStatisticsResult implements Serializable {
    private static final long serialVersionUID = -3880487925983560645L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentField("question_type")
    private String questionType; // 错题 or 类题
    @DocumentField("kp_type")
    private String kpType;
    @DocumentField("catalog_id")
    private String catalogId;
    @DocumentField("book_id")
    private String bookId;
    @DocumentField("group_id")
    private String groupId;
    @DocumentField("kp_infos")
    private List<KnowledgePointInfo> kpInfos;
    @DocumentField("updated_at")
    private Date updatedAt;
    @DocumentField("created_at")
    private Date createdAt;
}
