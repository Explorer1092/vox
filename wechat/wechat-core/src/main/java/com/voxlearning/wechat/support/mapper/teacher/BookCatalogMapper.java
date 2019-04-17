package com.voxlearning.wechat.support.mapper.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Created by xinxin on 26/1/2016.
 */
@Getter
@Setter
public class BookCatalogMapper implements Serializable {
    private static final long serialVersionUID = 8585829185888890919L;

    private String id;
    private String name;
    private Integer rank;
    private String type;
    private String parentId;

    private Boolean isDefault;

    private List<BookCatalogMapper> children;

    public BookCatalogMapper(NewBookCatalog catalog) {
        this.id = catalog.getId();
        this.name = Objects.equals(Subject.ENGLISH.getId(), catalog.getSubjectId()) ? catalog.getAlias() : catalog.getName();
        this.rank = catalog.getRank();
        this.type = catalog.getNodeType();
        this.parentId = catalog.getParentId();
        this.isDefault = false;

        this.children = new LinkedList<>();
    }
}
