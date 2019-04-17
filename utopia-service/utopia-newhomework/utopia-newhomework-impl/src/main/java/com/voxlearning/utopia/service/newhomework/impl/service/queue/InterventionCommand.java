package com.voxlearning.utopia.service.newhomework.impl.service.queue;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Auther: majianxin
 * @Date: 2018/5/2
 * @Description: 即时干预, 提交答案上报
 */
@Data
public class InterventionCommand implements Serializable {

    private static final long serialVersionUID = -5876993981629001188L;

    private Long actor; // 用户ID
    /**
     * 1. 行为, string "answer"--> 题目ID
     * 2. 行为, string "viewHint"-->提示ID，拼接方法为questionId#hint（hint ENUM取值1-4），string
     */
    private String object;
    private String verb; // 行为, string "answer"/"viewHint"
    private Boolean result;// //回答判别，=grasp, boolean
    private Context context;

    private Attachments attachments;// 附件

    private String timestamp;//时间戳
    private Long duration;//持续时间

    @Data
    public static class Context {
        private String homeworkId;//作业ID
        private String objectiveConfigType;//作业类型
        private String tag;// 行为, string "answer"--> 看到提示后二次尝试的标志，'re-attempt'
        private String hintTag;//提示来源标签：string：举例，qc-label-note；qc-label-vote

        public Context(String homeworkId, String objectiveConfigType, String tag) {
            this.homeworkId = homeworkId;
            this.objectiveConfigType = objectiveConfigType;
            this.tag = tag;
        }

        public Context(String homeworkId, String objectiveConfigType) {
            this.homeworkId = homeworkId;
            this.objectiveConfigType = objectiveConfigType;
        }
    }

    @Data
    public static class Attachments {
        private List<List<String>> userAnswers;//答案详情

        public Attachments(List<List<String>> userAnswers) {
            this.userAnswers = userAnswers;
        }
    }
}
