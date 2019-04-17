package com.voxlearning.utopia.service.newexam.api.mapper;


import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Setter
@Getter
public class NewExamReportForStudent implements Serializable {
    private static final long serialVersionUID = -4637680620961069764L;
    private boolean success; //crm查询是否成功
    private String description;//失败描述
    private int totalQuestionNum;//一共的题数量
    private long userId;
    private int finishQuestionNum;//完成题数
    private List<CrmNewExamProcessDetail> crmNewExamProcessDetails = new LinkedList<>();
    private int processNum;

    @Setter
    @Getter
    public static class CrmNewExamProcessDetail implements Serializable {
        private static final long serialVersionUID = 3887356049491023473L;
        private String newExamProcessId; //答题记录ID
        private String createAt;   //创建时间
        private String updateAt;//修改时间
        private String questionDocId;//题ID
        private Double standardScore;//标准分保留四位小数
        private Double score; // 用户实际得分,学生口语分数 =　calculateStudentOralScore()
        private Boolean grasp;// 是否掌握(全对/部分对)
        private String subGrasp; // 作答区域的掌握情况
        private String userAnswers;// 用户答案
        private Long durationSeconds; // 完成时长（单位：秒）
        private String clientType; // 客户端类型
        private String clientName;  // 客户端名称
        private Double correctScore; // 批改分数
        private String correctAt; // 批改时间
        private List<List<NewExamProcessResult.OralDetail>> oralDetails; // 口语题详情
        private boolean inPaper; //判断记录是否在试卷题目中
        private String detail;   // 明细
        private List<Audio> audios = new LinkedList<>();
    }

    @Setter
    @Getter
    public static class Audio implements Serializable {

        private static final long serialVersionUID = 8787561647614869484L;
        private String audioUrl;
        private String audioInfo;
    }
}
