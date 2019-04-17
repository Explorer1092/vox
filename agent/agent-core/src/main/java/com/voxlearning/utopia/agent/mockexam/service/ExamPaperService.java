package com.voxlearning.utopia.agent.mockexam.service;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageInfo;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageResult;
import com.voxlearning.utopia.agent.mockexam.service.dto.Result;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperOpenOptionParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperProcessStateNotify;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperQueryParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPaperDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPaperOpenOptionResult;

import java.util.concurrent.TimeUnit;

/**
 * 试卷服务
 *
 * @author xiaolei.li
 * @version 2018/8/6
 */
@ServiceVersion(version = "2018.10.01")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
public interface ExamPaperService {

    /**
     * 处理流程状态通知
     *
     * @param notify 消息
     */
    Result<Boolean> handleProcessNotify(ExamPaperProcessStateNotify notify);

    /**
     * 分页查找
     *
     * @return
     */
    PageResult<ExamPaperDto> queryPage(ExamPaperQueryParams paperQueryParams, PageInfo pageInfo);

    /**
     * 查找所有
     * @return
     */
    void initPlanForm();

    /**
     * 开放或者关闭
     *
     * @param params 参数
     * @return
     */
    Result<ExamPaperOpenOptionResult> openOrClose(ExamPaperOpenOptionParams params);
}
