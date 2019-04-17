package com.voxlearning.utopia.agent.mockexam.service.dto.input;

import com.voxlearning.utopia.agent.mockexam.service.dto.OperateRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 考试计划审核拒绝参数
 *
 * @author xiaolei.li
 * @version 2018/8/7
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ExamPlanAuditParams extends OperateRequest {

    /**
     * 审核选项
     */
    private Option option;

    /**
     * 备注
     */
    private String note;

    /**
     * 审核选项
     */
    @AllArgsConstructor
    public enum Option {
        APPROVE("批准"),
        REJECT("拒绝");
        public final String desc;
    }
}
