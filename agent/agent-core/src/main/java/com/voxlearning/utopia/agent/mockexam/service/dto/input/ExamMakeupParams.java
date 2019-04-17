package com.voxlearning.utopia.agent.mockexam.service.dto.input;

import com.voxlearning.utopia.agent.mockexam.service.dto.OperateRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 补考参数
 *
 * @Author: peng.zhang
 * @Date: 2018/8/10 16:54
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ExamMakeupParams extends OperateRequest {

    /**
     * 学生id，逗号分隔
     */
    private String studentIds;
}
