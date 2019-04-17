package com.voxlearning.utopia.service.ai.impl.persistence.task;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mysql.hql.MySQLInsertOperationHelper;
import com.voxlearning.alps.dao.mysql.hql.MySQLUpdateOperationHelper;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.ChipsUserDrawingTask;
import com.voxlearning.utopia.service.ai.entity.ChipsUserDrawingTaskJoin;

import javax.inject.Named;
import java.util.*;


@Named
@CacheBean(type = ChipsUserDrawingTaskJoin.class)
public class ChipsUserDrawingTaskJoinPersistence extends StaticMySQLPersistence<ChipsUserDrawingTaskJoin, Long> {

    @Override
    protected void calculateCacheDimensions(ChipsUserDrawingTaskJoin document, Collection<String> dimensions) {
        dimensions.add(ChipsUserDrawingTaskJoin.ck_task(document.getTaskId()));
        dimensions.add(ChipsUserDrawingTaskJoin.ck_joiner(document.getJoiner()));
    }

    @Override
    public ChipsUserDrawingTaskJoin load(Long id) {
        return $load(id);
    }

    @CacheMethod
    public List<ChipsUserDrawingTaskJoin> loadByTask(@CacheParameter(value = "T") Long taskId) {
        Criteria criteria = Criteria.where("TASK_ID").is(taskId).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<ChipsUserDrawingTaskJoin> loadByJoiner(@CacheParameter(value = "J") Long joiner) {
        Criteria criteria = Criteria.where("JOINER").is(joiner).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }


    public void insertOrUpdate(Long joinUser, Long taskId, String userAnwer, boolean master) {
        ChipsUserDrawingTaskJoin document = new ChipsUserDrawingTaskJoin(joinUser, taskId, userAnwer, master);
        MySQLInsertOperationHelper insertOperationHelper = new MySQLInsertOperationHelper(getDocumentMapping(), document, getTableName());
        StringBuilder sql = new StringBuilder(insertOperationHelper.generateSQL());
        sql.append(" ON DUPLICATE KEY UPDATE ").append("`UPDATETIME` = NOW(), `DISABLED` = 0");
        Object[] params = insertOperationHelper.toParams();
        if (master) {
            sql.append(",MASTER=b'1'")
                    .append(",ENERGY=1")
                    .append(",USER_ANSWER = ?");
            Object[] newParams = Arrays.copyOf(params, params.length + 1);
            newParams[params.length] = userAnwer;
            params = newParams;
        }

        int res = getJdbcTemplate().update(sql.toString(), params);
        if (res > 0) {
            ChipsUserDrawingTaskJoin join = loadByJoinerAndTask(joinUser, taskId);
            cleanCache(join);
        }
    }

    private ChipsUserDrawingTaskJoin loadByJoinerAndTask(Long joinUser, Long taskId) {
        Criteria criteria = Criteria.where("TASK_ID").is(taskId).and("JOINER").is(joinUser);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATETIME");
        Query query = Query.query(criteria).with(sort).limit(1);
        List<ChipsUserDrawingTaskJoin> list = query(query);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }


    private void cleanCache(ChipsUserDrawingTaskJoin task) {
        if (task == null) {
            return;
        }
        Set<String> cacheIds = new HashSet<>();
        calculateCacheDimensions(task, cacheIds);
        getCache().deletes(cacheIds);
    }
}
