package com.voxlearning.utopia.service.psr.entity.newhomework;

/**
 * Created with IntelliJ IDEA.
 * User: hotallen
 * Date: 2016/7/18
 * Time: 20:46
 * To change this template use File | Settings | File Templates.
 */

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
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.question.api.entity.EmbedSolutionMethodContent;
import com.voxlearning.utopia.service.question.api.entity.EmbedTestMethod;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 小学数学更多题目推荐
 */

@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = "mongo-question")
@DocumentDatabase(database = "vox-question")
@DocumentCollection(collection = "bigdata_question_cluster_profile")
@DocumentIndexes({
        @DocumentIndex(def = "{'book_catalog_id':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160908")
public class QuestionClusterProfile implements Serializable {
    private static final long serialVersionUID = 5044777004618356409L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentField("subject_id")
    private Integer subjectId;
    @DocumentField("book_catalog_id")
    private String bookCatalogId; //section Id
    @DocumentField("knowledge_points_new")
    private List<KnowledgePointNew> knowledgePointNews;
    @DocumentField("test_methods")
    private List<EmbedTestMethod> testMethods;
    @DocumentField("solution_methods")
    private List<EmbedSolutionMethodContent> solutionMethods;
    @DocumentField("created_at")
    private Date createdAt;
    @DocumentField("updated_at")
    private Date updatedAt;
    @DocumentField("deleted_at")
    private Date deletedAt;
    @DocumentField("clusters")
    private List<List<String>> cluster_qids;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(QuestionClusterProfile.class, id);
    }

}
