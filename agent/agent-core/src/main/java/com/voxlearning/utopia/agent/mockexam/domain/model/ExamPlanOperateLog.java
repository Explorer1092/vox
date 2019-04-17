package com.voxlearning.utopia.agent.mockexam.domain.model;

import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPlanOperateLogEntity;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums.Status;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 测评计划审核记录
 *
 * @Author: peng.zhang
 * @Date: 2018/8/9 15:48
 */
@Data
public class ExamPlanOperateLog implements Serializable {
    private Long id;

    /**
     * 计划id
     */
    private Long planId;

    /**
     * 上一个状态
     */
    private Status prevStatus;

    /**
     * 当前状态
     */
    private Status currentStatus;

    /**
     * 备注
     */
    private String note;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 创建时间
     */
    private Date createDatetime;

    public static class Builder {
        public static ExamPlanOperateLogEntity build(ExamPlanOperateLog model) {
            ExamPlanOperateLogEntity entity = new ExamPlanOperateLogEntity();
            BeanUtils.copyProperties(model, entity);
            entity.setPrevStatus(model.getPrevStatus().name());
            entity.setCurrentStatus(model.getCurrentStatus().name());
            return entity;
        }

        public static ExamPlanOperateLog build(ExamPlanOperateLogEntity entity) {
            ExamPlanOperateLog model = new ExamPlanOperateLog();
            BeanUtils.copyProperties(entity, model);
            model.setPrevStatus(Status.valueOf(entity.getPrevStatus()));
            model.setCurrentStatus(Status.valueOf(entity.getCurrentStatus()));
            return model;
        }
    }
}
