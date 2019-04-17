package com.voxlearning.utopia.agent.persist.exam;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamContract;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamPaper;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;

import java.io.Serializable;
import java.util.List;

/**
 * @author chunlin.yu
 * @create 2018-03-16 16:59
 **/
public abstract class AgentExamBasePersistence<E extends AbstractDatabaseEntityWithDisabledField, ID extends Serializable> extends AlpsStaticJdbcDao<E, ID> {

    @Override
    public E load(ID id){
        if (null == id){
            return null;
        }
        return super.load(id);
    }

    @Override
    public void insert(E document){
        if (null != document && null == document.getDisabled()){
            document.setDisabled(false);
        }
        super.insert(document);
    }

    @Override
    public E replace(E model){
        if (null != model && null == model.getDisabled()){
            model.setDisabled(false);
        }
        return super.replace(model);
    }

    @Override
    public E upsert(E model) {
        if (null != model && null == model.getDisabled()) {
            model.setDisabled(false);
        }
        return super.upsert(model);
    }

    public List<E> loadAll(){
        Criteria criteria = generateCriteriaWithDisabledField(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    protected Criteria generateCriteriaWithDisabledField(Boolean disabled){
        Criteria criteria = new Criteria();
        if (disabled != null){
            criteria.and("DISABLED").is(disabled);
        }
        return criteria;
    }

}
