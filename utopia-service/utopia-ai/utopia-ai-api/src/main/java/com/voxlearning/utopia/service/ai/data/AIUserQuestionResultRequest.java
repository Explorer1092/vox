package com.voxlearning.utopia.service.ai.data;

import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.entity.QuestionWeekPoint;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Summer on 2018/3/28
 * 用户答题结果请求
 */
@Getter
@Setter
public class AIUserQuestionResultRequest implements Serializable {

    private static final long serialVersionUID = -5515005808766390487L;

    private String qid;
    private String unitId;
    private String lessonId;
    private List<String> userAudio;
    private String userVideo;
    private String bookId;          //教材ID add by zhuxuan

    private Integer independent;     // 独立性
    private Integer listening;       // 听力
    private Integer express;         // 表达
    private Integer fluency;         // 流利度
    private Integer pronunciation;   // 发音
    private Integer score;          // 总分
    private Integer completeScore;  // 完整性得分
    private LessonType lessonType;  // 题目类型

    private String answerLevel;     // 回答类型
    private List<QuestionWeekPoint> weekPoints; // 回答薄弱点

    private Integer deductScore;    // 扣分累计值

    private Boolean lessonLast;     // 课时最后一题
    private Boolean unitLast;       // 单元最后一题

    private String roleName;        //任务对话角色名称
    private String usercode;        //任务对话中所需要的设备号

}
