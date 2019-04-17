package com.voxlearning.utopia.agent.mockexam.middleschool.domain;

import com.voxlearning.utopia.agent.mockexam.middleschool.domain.model.MiddleSchoolExamPaper;
import com.voxlearning.utopia.agent.mockexam.middleschool.service.dto.input.MiddleSchoolExamPaperParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageInfo;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageResult;

/**
 * @description: 中学试卷领域接口
 * @author: kaibo.he
 * @create: 2019-03-18 15:52
 **/
public interface MiddleSchoolExamPaperDomain {

    PageResult<MiddleSchoolExamPaper> queryPage(MiddleSchoolExamPaperParams params, PageInfo pageInfo);
}
