package com.voxlearning.utopia.agent.mockexam.service.dto.input;

import com.voxlearning.utopia.agent.mockexam.service.dto.OperateRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * 开放与关闭参数
 *
 * @Author: peng.zhang
 * @Date: 2018/8/10 10:30
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ExamPaperOpenOptionParams extends OperateRequest implements Serializable {

    /**
     * 试卷
     */
    private String paperId;
}
