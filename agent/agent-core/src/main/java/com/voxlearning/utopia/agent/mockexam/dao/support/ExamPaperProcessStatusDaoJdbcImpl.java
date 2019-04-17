package com.voxlearning.utopia.agent.mockexam.dao.support;

import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.agent.mockexam.dao.ExamPaperProcessStateDao;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPaperProcessStatusEntity;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * 试卷流程通知持久层mysql实现
 *
 * @author xiaolei.li
 * @version 2018/8/7
 */
@Repository
public class ExamPaperProcessStatusDaoJdbcImpl extends AlpsStaticJdbcDao<ExamPaperProcessStatusEntity, Long>
        implements ExamPaperProcessStateDao {

    @Override
    public void insert(ExamPaperProcessStatusEntity process) {
        super.insert(process);
    }

    @Override
    protected void calculateCacheDimensions(ExamPaperProcessStatusEntity document, Collection<String> dimensions) {

    }
}
