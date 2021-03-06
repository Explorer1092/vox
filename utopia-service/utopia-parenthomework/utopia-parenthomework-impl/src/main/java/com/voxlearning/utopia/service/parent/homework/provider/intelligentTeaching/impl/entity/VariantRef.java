package com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.impl.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 变式关系表
 *
 * @author Wenlong Meng
 * @since Feb 13, 2019
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-question")
@DocumentDatabase(database = "vox-question")
@DocumentCollection(collection = "variant_ref")
public class VariantRef implements Serializable {

    private static final long serialVersionUID = -2022210401815265192L;

    @DocumentId
    private String id;
    @DocumentField("subject_id")
    private Integer subjectId;//科目
    @DocumentField("book_id")
    private String bookId;//教材id
    @DocumentField("created_at")
    private Date createdAt;//创建时间
    @DocumentField("updated_at")
    private Date updatedAt;//更新时间
    @DocumentField("book_catalog_id")
    private String bookCatalogId;
    @DocumentField("variants")
    private List<Variant> variants;
    @DocumentField("book_catalog_ancestors")
    private List<BookCatalog> bookCatalogAncestors;

}
