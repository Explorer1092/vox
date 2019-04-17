package com.voxlearning.washington.net.message.exam;

import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.utopia.api.constant.StudyType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author songtao
 * @since 2017/8/17
 */
@Data
public class WonderlandWrongQuestionExamRequest implements Serializable {
    private static final long serialVersionUID = -1866431906476985644L;

    private String subject; // 科目
    private String questionId; // 题ID
    private List<List<String>> answer;  // 答案
    private Long finishTime; // 完成时长
    private String clientType;  // 客户端类型:pc,mobile
    private String clientName;  // 客户端名称:***app

    private StudyType source;

    public static WonderlandWrongQuestionExamRequest transform(SaveWonderlandWrongQuestionExamRequest serr) {
        WonderlandWrongQuestionExamRequest request = new WonderlandWrongQuestionExamRequest();

        WonderlandWrongQuestionExamResultRequest aerr = serr.result;
        Validate.notNull(aerr);
        request.setSubject(aerr.getSubject());
        request.setClientType(aerr.getClientType());
        request.setClientName(aerr.getClientName());

        Validate.isTrue(aerr.getHomeworkExamResults().size() > 0);
        QuestionResultMapper qrm = aerr.getHomeworkExamResults().get(0);
        request.setQuestionId(qrm.getExamId());
        request.setAnswer(qrm.getAnswer());
        request.setFinishTime(qrm.getFinishTime());

        request.setSource(StudyType.of(aerr.getExtra().getSource()));

        return request;
    }
}
