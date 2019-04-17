package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.voxlearning.utopia.service.newhomework.api.constant.QuestionBoxType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author zhangbin
 * @since 2018/1/19
 */

@Getter
@Setter
public class ReadReciteVoiceResult implements Serializable {
    private static final long serialVersionUID = 1345341568212725536L;

    private Long studentId;                 // 学生id
    private String studentName;             // 学生姓名
    private String lessonId;                // lessonId
    private String lessonName;              // 课文名称
    private Double macScore;               // 百分制得分
    private Integer score;                  // 7分制得分
    private Boolean keyPointParagraph;      // 重点段落
    private String paragraph;               // 第n段
    private QuestionBoxType type;           // 朗读或者背诵
    private String voice;                   // 推荐语音
}
