package com.voxlearning.utopia.service.newhomework.api.mapper.report;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class JztHomeworkNotice extends JztHomeworkReport {

    private static final long serialVersionUID = -7987280554279753988L;

    private String bookCoverImageUrl;                   // 教材封面图
    private Integer questionCount;                      // 题目总数
    private String questionCountStr;                    // 题目总数字符串
    private List<PracticeContent> practices;            // 作业内容
    private OfflineHomework offlineHomework;        // 线下作业内容
    private List<FinishedStudent> finishedStudents;     // 完成学生
    private Boolean hasSentFlower;                      // 是否送过鲜花
    private String flowerDetailUrl;                     // 送花详情页url

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PracticeContent implements Serializable {
        private static final long serialVersionUID = -8207625706418202525L;

        private ObjectiveConfigType objectiveConfigType;    // 作业形式
        private String objectiveConfigTypeName;             // 作业形式名
        private String summary;                             // 简介
        private boolean finished;                           // 是否完成
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class FinishedStudent implements Serializable {
        private static final long serialVersionUID = -2624175566336752193L;

        private Long studentId;                 // 学生id
        private String studentName;             // 学生姓名
        private String avatarImgUrl;            // 学生头像
        private String finishAt;                // 完成时间
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class OfflineHomework implements Serializable {
        private static final long serialVersionUID = -8017933148080374666L;

        private String ohid;                            // 线下作业ID
        private List<String> offlineHomeworkContents;   // 线下作业内容
        private boolean needSign;                       // 是否需要家长签字
        private boolean hadSign;                        // 家长是否已签字
        private String signReadContent;                 // 签字确认内容

    }
}
