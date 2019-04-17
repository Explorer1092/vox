package com.voxlearning.utopia.service.newhomework.api.context.selfstudy;

import com.voxlearning.utopia.service.newhomework.api.context.AbstractContext;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author xuesong.zhang
 * @since 2017/2/6
 */
@Getter
@Setter
public class FinishSelfStudyHomeworkContext extends AbstractContext<FinishSelfStudyHomeworkContext> {

    private static final long serialVersionUID = 7640105637316872572L;

    // in
    private Long userId;                            // 用户ID
    private String homeworkId;                      // 作业ID
    private SelfStudyHomework selfStudyHomework;    // 作业
    private ObjectiveConfigType objectiveConfigType;            // 作业类型

    // middle
    private SelfStudyHomeworkResult selfStudyHomeworkResult;    // 作业中间结果

    // FSS_CheckPracticeFinished 写入
    private Map<ObjectiveConfigType, List<String>> answerIdMap; // 作业里作业形式对应的新结构的id，不是processId
    private boolean practiceFinished = false;                   // 当前错题订正类型是否全部完成
    // FSS_CheckSelfStudyHomeworkFinished 写入
    private boolean homeworkFinished = false;                   // 当前作业是否全部完成
    // FSS_CalculateScoreAndDuration 写入
    private Double practiceScore;                               // 某个练习的分数
    private Long practiceDuration;                              // 某个练习的耗时

    private Set<String> processIds;                             //当前作业形式的processId

    private String appChameleonId;                              //app变色龙ID(courseId/...)
}
