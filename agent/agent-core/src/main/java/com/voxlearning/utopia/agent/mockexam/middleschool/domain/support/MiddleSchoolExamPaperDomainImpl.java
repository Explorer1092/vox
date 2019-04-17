package com.voxlearning.utopia.agent.mockexam.middleschool.domain.support;

import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.integration.MiddleSchoolExamPaperClient;
import com.voxlearning.utopia.agent.mockexam.middleschool.domain.MiddleSchoolExamPaperDomain;
import com.voxlearning.utopia.agent.mockexam.middleschool.domain.model.MiddleSchoolExamPaper;
import com.voxlearning.utopia.agent.mockexam.middleschool.service.dto.input.MiddleSchoolExamPaperParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.ErrorCode;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageInfo;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageResult;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @description: 中学试卷领域接口实现
 * @author: kaibo.he
 * @create: 2019-03-18 17:02
 **/
@Named
public class MiddleSchoolExamPaperDomainImpl implements MiddleSchoolExamPaperDomain {
    @Inject
    private MiddleSchoolExamPaperClient middleSchoolExamPaperClient;
    @Override
    public PageResult<MiddleSchoolExamPaper> queryPage(MiddleSchoolExamPaperParams params, PageInfo pageInfo) {
        MiddleSchoolExamPaperClient.PaperRequest request = MiddleSchoolExamPaperClient.PaperRequest.Builder.build(params, pageInfo);
        MiddleSchoolExamPaperClient.PaperPageResponse response = middleSchoolExamPaperClient.queryPage(request);
        if (!response.isSuccess()) {
            throw new BusinessException(ErrorCode.PAPER_QUERY_ERROR, "查询试卷报错");
        }
        List<MiddleSchoolExamPaper> papers = Optional.ofNullable(response.getItems()).orElse(new ArrayList<>())
                .stream()
                .map(paper -> MiddleSchoolExamPaper.Builder.build(paper))
                .collect(Collectors.toList());
        return PageResult.success((ArrayList) papers, pageInfo, response.getTotal());
    }
}
