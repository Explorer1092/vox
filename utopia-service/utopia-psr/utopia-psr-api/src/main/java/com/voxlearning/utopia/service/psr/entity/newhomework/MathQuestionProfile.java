package com.voxlearning.utopia.service.psr.entity.newhomework;

/**
 * Created with IntelliJ IDEA.
 * User: hotallen
 * Date: 2016/7/18
 * Time: 20:28
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
 * 类题相似推荐
 */

@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = "mongo-question")
@DocumentDatabase(database = "vox-question")
@DocumentCollection(collection = "bigdata_question_profile")
@DocumentIndexes({
        @DocumentIndex(def = "{'doc_id':1}", background = true),
        @DocumentIndex(def = "{'question_id':1}", background = true),
        @DocumentIndex(def = "{'deleted_at':1}", background = true),
        @DocumentIndex(def = "{'subject_id':1}", background = true),
        @DocumentIndex(def = "{'knowledge_points_new.kpf_id':1}", background = true),
        @DocumentIndex(def = "{'test_methods.id':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160908")
public class MathQuestionProfile implements Serializable {

    private static final long serialVersionUID = -8773610669033457258L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentField("subject_id")
    private Integer subjectId;
    @DocumentField("knowledge_points_new")
    private List<KnowledgePointNew> knowledgePointNews;
    @DocumentField("test_methods")
    private List<EmbedTestMethod> testMethods;
    @DocumentField("solution_methods")
    private List<EmbedSolutionMethodContent> solutionMethods;
    @DocumentField("created_at")
    private Date createdAt;
    @DocumentField("updated_at")
    private Date updateAt;
    @DocumentField("deleted_at")
    private Date deletedAt;
    @DocumentField("series_progress")
    private List<SeriesProgress> seriesProgresses;
    @DocumentField("doc_id")
    private String doc_id;
    @DocumentField("question_id")
    private String question_id;
    @DocumentField("content_type_id")
    private Integer content_type_id;
    @DocumentField("difficulty_int")
    private Integer difficulty_int;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(MathQuestionProfile.class, id);
    }
}
