package com.voxlearning.utopia.service.ai.impl.persistence.task;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mysql.hql.MySQLInsertOperationHelper;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.constant.ChipsUserDrawingTaskStatus;
import com.voxlearning.utopia.service.ai.entity.ChipsUserDrawingTask;

import javax.inject.Named;
import java.util.*;


@Named
@CacheBean(type = ChipsUserDrawingTask.class)
public class ChipsUserDrawingTaskPersistence extends StaticMySQLPersistence<ChipsUserDrawingTask, Long> {

    @Override
    protected void calculateCacheDimensions(ChipsUserDrawingTask document, Collection<String> dimensions) {
        dimensions.add(ChipsUserDrawingTask.ck_user(document.getUserId()));
        dimensions.add(ChipsUserDrawingTask.ck_id(document.getId()));
    }


    public void insertOrUpdate(Long userId, String bookId, String unitId, String drawingId) {
        ChipsUserDrawingTask document = new ChipsUserDrawingTask(userId, bookId, unitId, drawingId);
        MySQLInsertOperationHelper insertOperationHelper = new MySQLInsertOperationHelper(getDocumentMapping(), document, getTableName());
        StringBuilder sql = new StringBuilder(insertOperationHelper.generateSQL());
        sql.append(" ON DUPLICATE KEY UPDATE ").append("`UPDATETIME`=NOW(),`DISABLED`=0,STATUS='underway',DRAWING_ID='" + drawingId + "'");
        int res = getJdbcTemplate().update(sql.toString(), insertOperationHelper.toParams());
        if (res > 0) {
            ChipsUserDrawingTask task = loadByUserAndUnitId(userId, unitId);
            cleanCache(task);
        }
    }


    public void updateStatus(ChipsUserDrawingTask document, ChipsUserDrawingTaskStatus taskStatus) {
        Criteria criteria = Criteria.where("ID").is(document.getId()).and("DISABLED").is(false);
        Update update = new Update();
        update.set("STATUS", taskStatus.name()).set("UPDATETIME", new Date());
        long res = $update(update, criteria);
        if (res > 0) {
            cleanCache(document);
        }
    }

    public void updateShare(ChipsUserDrawingTask document) {
        Criteria criteria = Criteria.where("ID").is(document.getId()).and("DISABLED").is(false).and("SHARE").is(false);
        Update update = new Update();
        update.set("SHARE", true).set("UPDATETIME", new Date());
        long res = $update(update, criteria);
        if (res > 0) {
            cleanCache(document);
        }

    }

    @CacheMethod
    public List<ChipsUserDrawingTask> loadByUser(@CacheParameter(value = "U") Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    private ChipsUserDrawingTask loadByUserAndUnitId(Long userId, String unitId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId).and("UNIT_ID").is(unitId);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATETIME");
        Query query = Query.query(criteria).with(sort).limit(1);
        List<ChipsUserDrawingTask> list = query(query);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }


    private void cleanCache(ChipsUserDrawingTask task) {
        if (task == null) {
            return;
        }
        Set<String> cacheIds = new HashSet<>();
        calculateCacheDimensions(task, cacheIds);
        getCache().deletes(cacheIds);
    }
}
