package com.voxlearning.utopia.agent.mockexam.service.dto.output;

import com.voxlearning.utopia.agent.mockexam.service.dto.enums.BooleanEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 开放或者关闭结果
 *
 * @author xiaolei.li
 * @version 2018/9/4
 */
@Data
public class ExamPaperOpenOptionResult implements Serializable {
    private Long id;
    private String paperId;
    private BooleanEnum isPublic;
}
