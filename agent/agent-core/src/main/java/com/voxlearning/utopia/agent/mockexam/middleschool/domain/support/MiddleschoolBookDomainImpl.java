package com.voxlearning.utopia.agent.mockexam.middleschool.domain.support;

import com.voxlearning.utopia.agent.mockexam.integration.MiddleSchoolExamPaperClient;
import com.voxlearning.utopia.agent.mockexam.middleschool.domain.MiddleschoolBookDomain;
import com.voxlearning.utopia.agent.mockexam.middleschool.domain.model.MiddleSchoolBook;
import com.voxlearning.utopia.agent.mockexam.middleschool.service.dto.output.MiddleSchoolBookDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageResult;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @description: 中学教材领域接口实现
 * @author: kaibo.he
 * @create: 2019-03-19 18:49
 **/
@Named
public class MiddleschoolBookDomainImpl implements MiddleschoolBookDomain {

    @Inject
    private MiddleSchoolExamPaperClient middleSchoolExamPaperClient;
    @Override
    public PageResult<MiddleSchoolBook> queryBooks() {
        MiddleSchoolExamPaperClient.PaperSearchItemResponse searchItemResponse = middleSchoolExamPaperClient.querySearchItems();
        List<MiddleSchoolBook> books = MiddleSchoolBook.Builder.build(searchItemResponse);
        return PageResult.success((ArrayList) books, null, 0);
    }
}
