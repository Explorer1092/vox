package com.voxlearning.utopia.agent.mockexam.middleschool.service;

import com.voxlearning.utopia.agent.mockexam.middleschool.service.dto.input.MiddleSchoolExamPaperParams;
import com.voxlearning.utopia.agent.mockexam.middleschool.service.dto.output.MiddleSchoolBookDto;
import com.voxlearning.utopia.agent.mockexam.middleschool.service.dto.output.MiddleSchoolExamPaperDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageInfo;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageResult;

/**
 * @description: 中学试卷服务
 * @author: kaibo.he
 * @create: 2019-03-18 15:44
 **/
public interface MiddleSchoolExamPaperService {
    /**
     * 分页查询中学试卷
     * @param params
     * @param pageInfo
     * @return
     */
    PageResult<MiddleSchoolExamPaperDto> queryPage(MiddleSchoolExamPaperParams params, PageInfo pageInfo);

    /**
     * 获取教材信息
     * @return
     */
    PageResult<MiddleSchoolBookDto> queryBooks();
}
