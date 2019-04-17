package com.voxlearning.utopia.service.parent.homework.impl.model;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.List;

/**
 * 教材口算题的缓存结构
 */
@Getter
@Setter
public class BookQuestionNode implements Serializable {
    private String id;
    private BookCatalogType bookCatalogType;
    private List<BookQuestionNode> childNodes; // 子节点
    private Boolean keyPoint; // 是否主要知识点
    private Boolean supportForAi; // 是否支持纸质打印场景
}