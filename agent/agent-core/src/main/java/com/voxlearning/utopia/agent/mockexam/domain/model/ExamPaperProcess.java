package com.voxlearning.utopia.agent.mockexam.domain.model;

import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPaperProcessEntity;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperProcessStateNotify.Status;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 试卷流程
 *
 * @author xiaolei.li
 * @version 2018/8/10
 */
@Data
public class ExamPaperProcess implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 试卷流程id
     */
    private String processId;

    /**
     * 计划id
     */
    private Long planId;

    /**
     * 流程状态
     */
    private Status status;

    /**
     * 拒绝理由
     */
    private String rejectReason;

    /**
     * 创建时间
     */
    private Date createDatetime;

    /**
     * 最后一次修改时间
     */
    private Date updateDatetime;

    public static class Builder {
        public static ExamPaperProcessEntity build(ExamPaperProcess model) {
            ExamPaperProcessEntity entity = new ExamPaperProcessEntity();
            entity.setId(model.getId());
            entity.setPlanId(model.getPlanId());
            entity.setProcessId(model.getProcessId());
            entity.setStatus(model.getStatus().name());
            entity.setRejectReason(model.getRejectReason());
            entity.setCreateDatetime(model.getCreateDatetime());
            entity.setUpdateDatetime(model.getUpdateDatetime());
            return entity;
        }

        public static ExamPaperProcess build(ExamPaperProcessEntity entity) {
            ExamPaperProcess model = new ExamPaperProcess();
            model.setId(model.getId());
            model.setPlanId(model.getPlanId());
            model.setProcessId(model.getProcessId());
            model.setStatus(model.getStatus());
            model.setRejectReason(model.getRejectReason());
            model.setCreateDatetime(model.getCreateDatetime());
            model.setUpdateDatetime(model.getUpdateDatetime());
            return model;
        }
    }
}
