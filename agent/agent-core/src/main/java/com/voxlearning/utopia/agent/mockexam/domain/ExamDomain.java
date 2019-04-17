package com.voxlearning.utopia.agent.mockexam.domain;

import com.voxlearning.utopia.agent.mockexam.domain.model.ExamStudentScore;
import com.voxlearning.utopia.agent.mockexam.service.dto.Result;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamMakeupParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamReplenishParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamScoreQueryParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamUploadParams;

import java.util.Map;

/**
 * 考试领域层接口
 *
 * @author xiaolei.li
 * @version 2018/8/12
 */
public interface ExamDomain {

    /**
     * 补考
     *
     * @param params
     * @return
     */
    Map<Long, String> makeup(ExamMakeupParams params);

    /**
     * 重考
     *
     * @param params
     * @return
     */
    Map<Long, String> replenish(ExamReplenishParams params);

    /**
     * 成绩查询
     *
     * @param params 参数
     * @return 学生成绩
     */
    ExamStudentScore queryScore(ExamScoreQueryParams params);

    /**
     * 上传文件
     *
     * @param params 参数
     * @return 结果
     */
    Result<Boolean> uploadFile(ExamUploadParams params);

}
