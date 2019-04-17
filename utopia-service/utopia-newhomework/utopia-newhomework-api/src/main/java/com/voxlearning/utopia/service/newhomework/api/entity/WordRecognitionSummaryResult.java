package com.voxlearning.utopia.service.newhomework.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class WordRecognitionSummaryResult implements Serializable {

    private static final long serialVersionUID = 8959351581969543469L;
    private String lessonId;                // 课文id
    private String lessonName;              // 课文名称
    private String questionBoxId;           // 题包id

    private Integer questionCount;          // 题数
    private Integer finishQuestionCount;    // 完成题数
    private Boolean finished;               // 是否已做完
    private String processResultUrl;        // 上传答题结果url
    private String questionUrl;             // 获取试题url
    private String completedUrl;            // 获取答案url
    private String paragraphInfo;           // 生字信息
}
