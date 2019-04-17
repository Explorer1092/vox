package com.voxlearning.utopia.agent.mockexam.dao.support;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.agent.mockexam.dao.ExamPaperProcessDao;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPaperProcessEntity;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * 试卷流程通知持久层mysql实现
 *
 * @author xiaolei.li
 * @version 2018/8/7
 */
@Repository
public class ExamPaperProcessDaoJdbcImpl extends AlpsStaticJdbcDao<ExamPaperProcessEntity, Long> implements ExamPaperProcessDao {

    @Override
    public void insert(ExamPaperProcessEntity process) {
        super.insert(process);
    }

    @Override
    public void update(ExamPaperProcessEntity entity) {
        Criteria criteria = Criteria.where("ID").is(entity.getId());
        Update update = new Update();
        if (StringUtils.isNotBlank(entity.getDisable()))
            update.set("DISABLE", entity.getDisable());
        if (StringUtils.isNotBlank(entity.getStatus()))
            update.set("STATUS", entity.getStatus());
        if (null != entity.getUpdateDatetime())
            update.set("UPDATE_DATETIME", entity.getUpdateDatetime());
        super.$update(update, criteria);
    }

    @Override
    public ExamPaperProcessEntity findByProcessId(String processId) {
        Criteria criteria = Criteria.where("PROCESS_ID").is(processId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @Override
    protected void calculateCacheDimensions(ExamPaperProcessEntity document, Collection<String> dimensions) {

    }
}
