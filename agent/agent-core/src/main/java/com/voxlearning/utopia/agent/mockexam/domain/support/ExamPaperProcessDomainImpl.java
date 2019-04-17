package com.voxlearning.utopia.agent.mockexam.domain.support;

import com.voxlearning.utopia.agent.mockexam.dao.ExamPaperProcessDao;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPaperProcessEntity;
import com.voxlearning.utopia.agent.mockexam.domain.ExamPaperProcessDomain;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPaperProcess;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.BooleanEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 试卷流程领域层实现
 *
 * @author xiaolei.li
 * @version 2018/8/10
 */
@Service
public class ExamPaperProcessDomainImpl implements ExamPaperProcessDomain {

    @Resource
    ExamPaperProcessDao paperProcessDao;

    @Override
    public void create(ExamPaperProcess process) {
        ExamPaperProcessEntity entity = ExamPaperProcess.Builder.build(process);
        entity.setDisable(BooleanEnum.N.name());
        paperProcessDao.insert(entity);
    }
}
