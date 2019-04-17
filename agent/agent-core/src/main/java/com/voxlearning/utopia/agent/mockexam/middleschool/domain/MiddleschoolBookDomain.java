package com.voxlearning.utopia.agent.mockexam.middleschool.domain;

import com.voxlearning.utopia.agent.mockexam.middleschool.domain.model.MiddleSchoolBook;
import com.voxlearning.utopia.agent.mockexam.middleschool.service.dto.output.MiddleSchoolBookDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageResult;

/**
 * @description: 中学教材领域接口
 * @author: kaibo.he
 * @create: 2019-03-19 18:48
 **/
public interface MiddleschoolBookDomain {
    PageResult<MiddleSchoolBook> queryBooks();
}
