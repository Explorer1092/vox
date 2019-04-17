package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.util.List;

@Data
public class PsrLessonPersistence {

    /** Lesson ID */
    private Long lessonId;
    /** 中文名称 */
    private String cname;
    /** 英文名称 */
    private String ename;
    /** sentence分两种，type = 0是句子，type = 1是单词。目前小学英语应用，只推单词，所以这里只存了单词 */
    private List<String> sentences;
}
