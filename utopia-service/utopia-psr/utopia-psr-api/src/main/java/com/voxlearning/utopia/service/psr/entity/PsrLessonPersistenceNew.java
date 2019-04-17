package com.voxlearning.utopia.service.psr.entity;

import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePoint;
import lombok.Data;

import java.util.Map;

@Data
/**
 * Created by Administrator on 2016/3/29.
 */
public class PsrLessonPersistenceNew {

    /** Lesson ID */
    private String lessonId;
    /** 名称 */
    private String name;

    /** sentence分两种，type = 0是句子，type = 1是单词。目前小学英语应用，只推单词，所以这里只存了单词 */
    //private List<String> sentences;
    private Map<String/*kpid*/,NewKnowledgePoint/**/> sentences;
}


