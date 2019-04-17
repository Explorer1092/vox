package com.voxlearning.utopia.service.psr.entity.newhomework;

/**
 * Created with IntelliJ IDEA.
 * User: hotallen
 * Date: 2016/8/1
 * Time: 19:40
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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 精品题包类题
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = "mongo-question")
@DocumentDatabase(database = "vox-question")
@DocumentCollection(collection = "bigdata_question_similarity_profile")
@DocumentIndexes({
        @DocumentIndex(def = "{'book_catalog_id':1}", background = true),
        @DocumentIndex(def = "{'pak_id':1}", background = true),
        @DocumentIndex(def = "{'question_id':1}", background = true),
        @DocumentIndex(def = "{'doc_id':1}", background = true),
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160908")
public class QuestionSimilarityProfile implements Serializable {
    private static final long serialVersionUID = -1415105516048105825L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentField("book_catalog_id")
    private String book_catalog_id;
    @DocumentField("pak_id")
    private String pak_id;
    @DocumentField("question_id")
    private String question_id;
    @DocumentField("doc_id")
    private String doc_id;
    @DocumentField("sim_qids")
    private List<String> sim_qids; //相似类题
    @DocumentField("created_at")
    private Date createdAt;
    @DocumentField("updated_at")
    private Date updatedAt;
    @DocumentField("deleted_at")
    private Date deletedAt;

}
