package com.voxlearning.utopia.agent.mockexam.service.dto.input;

import com.voxlearning.utopia.agent.mockexam.service.dto.OperateRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2019-01-14 20:48
 **/
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BatchExamPlanAuditParams extends OperateRequest {
    private List<Long> ids;
    /**
     * 审核选项
     */
    private ExamPlanAuditParams.Option option;
}
