package com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.impl.entity;

import com.voxlearning.alps.annotation.dao.DocumentField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 教材节点
 *
 * @author Wenlong Meng
 * @since Feb 13, 2019
 */
@Getter
@Setter
public class BookCatalog implements Serializable {

    private static final long serialVersionUID = -2022210401815265192L;

    private String id;
    @DocumentField("node_level")
    private String nodeLevel;
    @DocumentField("node_type")
    private String nodeType;

}
