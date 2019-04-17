package com.voxlearning.utopia.agent.mockexam.dao.support;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.mockexam.dao.ExamPlanOperateLogDao;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPlanOperateLogEntity;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * @Author: peng.zhang
 * @Date: 2018/8/9 15:40
 */
@Repository
public class ExamPlanOperateLogDaoImpl extends AlpsStaticJdbcDao<ExamPlanOperateLogEntity, Long>
        implements ExamPlanOperateLogDao {

    @Override
    protected void calculateCacheDimensions(ExamPlanOperateLogEntity planProcessStateEntity, Collection<String> collection) {
        collection.add(ExamPlanOperateLogEntity.ck_eid(planProcessStateEntity.getId()));
    }

    @Override
    public void insert(ExamPlanOperateLogEntity entity) {
        super.insert(entity);
    }

    @Override
    public List<ExamPlanOperateLogEntity> findByPlanId(Long planId) {
        Criteria criteria = Criteria.where("PLAN_ID").is(planId);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return query(Query.query(criteria).with(sort));
    }
}
