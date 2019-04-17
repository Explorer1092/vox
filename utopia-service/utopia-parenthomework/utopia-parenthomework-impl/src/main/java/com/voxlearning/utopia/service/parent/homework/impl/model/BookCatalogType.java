package com.voxlearning.utopia.service.parent.homework.impl.model;

import lombok.Getter;

/**
 * 新版教材目录节点类型
 * BOOK, UNIT, LESSON, SECTION
 *
 * @author Junjie Zhang
 * @since 2016-01-26
 */
public enum BookCatalogType {
  SERIES(1, "教材版本"),
  BOOK(2, "教材"),
  MODULE(3, "章"),
  UNIT(4, "单元"),
  LESSON(5, "课"),
  SECTION(6, "节"),
  KnowledgePoint(7, "知识点");

  @Getter
  private final int key;
  @Getter
  private final String description;

  BookCatalogType(int key, String description) {
    this.key = key;
    this.description = description;
  }

}
