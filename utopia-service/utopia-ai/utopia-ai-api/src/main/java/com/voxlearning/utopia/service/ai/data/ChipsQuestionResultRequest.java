package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;


@Getter
@Setter
@ToString
public class ChipsQuestionResultRequest implements Serializable {

    private static final long serialVersionUID = -5515005808766390487L;

    private String qid;
    private String unitId;
    private String lessonId;
    private String bookId;
    private String questionType;


    private String userAnswer;       //用户回答
    private Boolean master;          //是否正确
    private Integer independent;     // 独立性
    private Integer listening;       // 听力
    private Integer express;         // 表达
    private Integer fluency;         // 流利度
    private Integer pronunciation;   // 发音
    private Integer score;          // 总分
    private Integer engineScore;    // 打分引擎分数
    private Integer keysIntegrity;  // 关键词完整度
    private Integer sentIntegrity;  // 句子完整度
    private Integer completeScore;  // 完整性得分
    private Integer deductScore;    // 扣分累计值
    private String answerLevel;     // 回答类型
    private List<String> userAudio;


    private Boolean lessonLast;     // 课时最后一题
    private Boolean unitLast;       // 单元最后一题

    private String roleName;        //任务对话角色名称
    private String usercode;        //任务对话中所需要的设备号


}
