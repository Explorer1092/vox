package com.voxlearning.utopia.service.psr.entity.newhomework;

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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hotallen
 * Date: 2016/7/27
 * Time: 11:18
 * To change this template use File | Settings | File Templates.
 */

@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = "mongo-question")
@DocumentDatabase(database = "vox-question")
@DocumentCollection(collection = "bigdata_question_package")
@DocumentIndexes({
        @DocumentIndex(def = "{'book_id':1}", background = true),
        @DocumentIndex(def = "{'unit_id':1}", background = true),
        @DocumentIndex(def = "{'lesson_id':1}", background = true),
        @DocumentIndex(def = "{'section_id':1}", background = true),
        @DocumentIndex(def = "{'deleted_at':1}", background = true),
        @DocumentIndex(def = "{'subject_id':1}", background = true),
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160908")
public class QuestionPackage implements Serializable {
    private static final long serialVersionUID = 592141131030913621L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentField("subject_id")
    private Integer subjectId;
    @DocumentField("book_id")
    private String bookId;
    @DocumentField("unit_id")
    private String unitId;
    @DocumentField("lesson_id")
    private String lessonId;
    @DocumentField("section_id")
    private String sectionId;
    @DocumentField("created_at")
    private Date createdAt;
    @DocumentField("updated_at")
    private Date updateAt;
    @DocumentField("deleted_at")
    private Date deletedAt;
    @DocumentField("question_ids")
    private List<String> questionIds;
    @DocumentField("pak_type")
    private String pakType; //题包类型  base(基础题包)|solidify(巩固题包)

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(QuestionClusterProfile.class, id);
    }

}
