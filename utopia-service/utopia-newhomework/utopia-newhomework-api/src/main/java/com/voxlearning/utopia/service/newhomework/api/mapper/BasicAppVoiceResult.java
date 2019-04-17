package com.voxlearning.utopia.service.newhomework.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author guoqiang.li
 * @since 2016/8/10
 */
@Getter
@Setter
public class BasicAppVoiceResult implements Serializable {
    private static final long serialVersionUID = -1747820437384152581L;

    private Long studentId;                 // 学生id
    private String studentName;             // 学生姓名
    private String lessonId;                // lessonId
    private Integer categoryId;             // 应用类型id
    private String categoryName;            // 应用类型
    private Integer macScore;               // 百分制得分
    private Integer score;                  // 得分
    private String scoreStr;
    private List<String> voiceList;         // 语音列表
    private Boolean hasLowScoreVoice;       // 是否有低于60分的语音
}
