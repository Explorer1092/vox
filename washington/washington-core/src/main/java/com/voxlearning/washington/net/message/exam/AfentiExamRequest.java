package com.voxlearning.washington.net.message.exam;

import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.utopia.api.constant.AfentiState;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Ruib
 * @since 2016/7/20
 */
@Data
public class AfentiExamRequest implements Serializable {
    private static final long serialVersionUID = -1866431906476985644L;

    private String subject; // 科目
    private Boolean finished; // 是否是最后一题
    private String questionId; // 题ID
    private List<List<String>> answer;  // 答案
    private Long finishTime; // 完成时长
    private String clientType;  // 客户端类型:pc,mobile
    private String clientName;  // 客户端名称:***app

    // castle
    private String bookId;           //课本ID
    private String unitId;           //单元ID
    private Integer rank;            //关卡
    private String scoreCoefficient; // 算法权重，用于数据上报
    private AfentiLearningType learningType;  // 区分预习还是城堡

    // elf
    private AfentiState afentiState; // 题目状态
    private String originalQuestionId; // 类题的原题

    public static AfentiExamRequest transform(SaveAfentiResultRequest sarr) {
        AfentiExamRequest request = new AfentiExamRequest();

        AfentiExamResultRequest aerr = sarr.result;
        Validate.notNull(aerr);
        request.setSubject(aerr.getSubject());
        request.setFinished(aerr.getFinished());
        request.setClientType(aerr.getClientType());
        request.setClientName(aerr.getClientName());

        Validate.isTrue(aerr.getHomeworkExamResults().size() > 0);
        QuestionResultMapper qrm = aerr.getHomeworkExamResults().get(0);
        request.setQuestionId(qrm.getExamId());
        request.setAnswer(qrm.getAnswer());
        request.setFinishTime(qrm.getFinishTime());

        AfentiExtraRequest qer = aerr.getExtra();
        Validate.notNull(qer);
        request.setBookId(qer.getBookId());
        request.setUnitId(qer.getUnitId());
        request.setRank(qer.getRank());
        request.setScoreCoefficient(qer.getScoreCoefficient());
        request.setAfentiState(qer.getAfentiState());
        request.setOriginalQuestionId(qer.getOriginQuestionId());
        request.setLearningType(qer.getLearningType());

        return request;
    }
}
