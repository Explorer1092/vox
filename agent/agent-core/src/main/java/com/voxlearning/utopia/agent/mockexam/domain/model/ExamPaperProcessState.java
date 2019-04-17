package com.voxlearning.utopia.agent.mockexam.domain.model;

import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperProcessStateNotify;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperProcessStateNotify.Status;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPaperProcessStatusEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 试卷流程状态
 *
 * @author xiaolei.li
 * @version 2018/8/7
 */
@Data
public class ExamPaperProcessState implements Serializable {

    /**
     * 主键 id
     */
    private Long id;

    /**
     * 考试计划id
     */
    private String businessId;

    /**
     * 试卷流程id
     */
    private String processId;

    /**
     * 流程状态
     */
    private Status status;

    /**
     * 当前处理人姓名
     */
    private String operatorName;

    /**
     * 驳回理由
     */
    private String rejectReason;

    /**
     * 试卷列表
     */
    private List<ExamPaperProcessStateNotify.Paper> papers;

    public static final class Builder {
        public static ExamPaperProcessStatusEntity build(ExamPaperProcessState model) {
            ExamPaperProcessStatusEntity entity = new ExamPaperProcessStatusEntity();
            entity.setStatus(model.getStatus().name());
            entity.setProcessId(model.getProcessId());
            entity.setRejectReason(model.getRejectReason());
            return entity;
        }

        public static ExamPaperProcessState build(ExamPaperProcessStateNotify notify) {
            ExamPaperProcessState model = new ExamPaperProcessState();
            model.setBusinessId(notify.getBusinessId());
            model.setProcessId(notify.getProcessId());
            model.setStatus(notify.getStatus());
            model.setOperatorName(notify.getOperatorName());
            model.setRejectReason(notify.getRejectReason());
            model.setPapers(notify.getPapers());
            return model;
        }
    }
}
