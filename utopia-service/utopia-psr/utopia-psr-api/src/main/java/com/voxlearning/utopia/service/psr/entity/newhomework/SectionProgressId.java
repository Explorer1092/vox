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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by huicheng on 2016/8/4.
 */

@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = "mongo-question")
@DocumentDatabase(database = "vox-question")
@DocumentCollection(collection = "bigdata_section_progressid")
@DocumentIndexes({
        @DocumentIndex(def = "{'series_id':1}", background = true),
        @DocumentIndex(def = "{'book_id':1}", background = true),
        @DocumentIndex(def = "{'unit_id':1}", background = true),
        @DocumentIndex(def = "{'lesson_id':1}", background = true),
        @DocumentIndex(def = "{'section_id':1}", background = true),
        @DocumentIndex(def = "{'deleted_at':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160908")
public class SectionProgressId implements Serializable {

    private static final long serialVersionUID = -8773610669033457258L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentField("created_at")
    private Date createdAt;
    @DocumentField("updated_at")
    private Date updateAt;
    @DocumentField("deleted_at")
    private Date deletedAt;
    @DocumentField("series_id")
    private String seriesId;
    @DocumentField("book_id")
    private String bookId;
    @DocumentField("unit_id")
    private String unitId;
    @DocumentField("lesson_id")
    private String lessonId;
    @DocumentField("section_id")
    private String sectionId;
    @DocumentField("progress_id")
    private Long progressId;
    @DocumentField("last_progress_id")
    private Long lastProgressId;
}
