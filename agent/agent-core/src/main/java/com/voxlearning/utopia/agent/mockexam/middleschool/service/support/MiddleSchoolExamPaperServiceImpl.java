package com.voxlearning.utopia.agent.mockexam.middleschool.service.support;

import com.voxlearning.utopia.agent.mockexam.middleschool.domain.MiddleSchoolExamPaperDomain;
import com.voxlearning.utopia.agent.mockexam.middleschool.domain.MiddleschoolBookDomain;
import com.voxlearning.utopia.agent.mockexam.middleschool.domain.model.MiddleSchoolExamPaper;
import com.voxlearning.utopia.agent.mockexam.middleschool.service.MiddleSchoolExamPaperService;
import com.voxlearning.utopia.agent.mockexam.middleschool.service.dto.input.MiddleSchoolExamPaperParams;
import com.voxlearning.utopia.agent.mockexam.middleschool.service.dto.output.MiddleSchoolBookDto;
import com.voxlearning.utopia.agent.mockexam.middleschool.service.dto.output.MiddleSchoolExamPaperDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageInfo;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageResult;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @description: 中学纸卷服务实现类
 * @author: kaibo.he
 * @create: 2019-03-18 15:46
 **/
@Named
public class MiddleSchoolExamPaperServiceImpl implements MiddleSchoolExamPaperService{

    @Inject
    private MiddleSchoolExamPaperDomain middleSchoolExamPaperDomain;
    @Inject
    private MiddleschoolBookDomain middleschoolBookDomain;
    @Override
    public PageResult<MiddleSchoolExamPaperDto> queryPage(MiddleSchoolExamPaperParams params, PageInfo pageInfo) {
        PageResult<MiddleSchoolExamPaper> paperResult = middleSchoolExamPaperDomain.queryPage(params, pageInfo);

        List<MiddleSchoolExamPaperDto> data = Optional.ofNullable(paperResult.getData()).orElse(new ArrayList<>())
                .stream()
                .map(paper -> MiddleSchoolExamPaperDto.Builder.build(paper))
                .collect(Collectors.toList());

        return PageResult.success((ArrayList) data, pageInfo, paperResult.getTotalSize());
    }

    @Override
    public PageResult<MiddleSchoolBookDto> queryBooks() {
        List<MiddleSchoolBookDto> dtos = Optional.ofNullable(middleschoolBookDomain.queryBooks().getData()).orElse(new ArrayList<>())
                .stream()
                .map(book -> MiddleSchoolBookDto.builder().name(book.getName()).id(book.getId()).build())
                .collect(Collectors.toList());
        return PageResult.success((ArrayList<MiddleSchoolBookDto>) dtos, null, 0);
    }
}
