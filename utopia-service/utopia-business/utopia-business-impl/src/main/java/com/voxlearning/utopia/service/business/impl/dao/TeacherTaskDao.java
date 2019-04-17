package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.entity.task.TeacherTask;

import javax.inject.Named;
import java.util.*;

@Named
@CacheBean(type = TeacherTask.class)
public class TeacherTaskDao extends AlpsStaticJdbcDao<TeacherTask, Long> {

    @Override
    protected void calculateCacheDimensions(TeacherTask document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    @CacheMethod
    public List<TeacherTask> loadByTeacherId(@CacheParameter("TEACHER_ID") Long teacherId) {
        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public List<Long> getTeacherIdByInfo(List<Long> tplIds, String status) {
        String tplIdsString = "";
        for (Long id : tplIds) {
            if (StringUtils.isNotEmpty(tplIdsString)) {
                tplIdsString = tplIdsString + ",";
            }
            tplIdsString = tplIdsString + id;
        }
        String sql = "SELECT DISTINCT `TEACHER_ID` FROM `VOX_TEACHER_TASK` WHERE TPL_ID in (" + tplIdsString + ") AND STATUS = ? AND DISABLED = 0";
        return getDataSourceConnection().getJdbcTemplate().queryForList(sql, Long.class, status);
    }

    public List<Long> getTeacherIdByLimit(Long teacherId) {
        String sql = "SELECT DISTINCT `TEACHER_ID` FROM `VOX_TEACHER_TASK` WHERE TEACHER_ID > ? AND DISABLED = 0  ORDER BY TEACHER_ID ASC limit 1000";
        return getDataSourceConnection().getJdbcTemplate().queryForList(sql, Long.class, teacherId);
    }

    public TeacherTask getTeacherTaskByTeacherIdAndTplId(Long teacherId, Long tplId) {
        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId).and("TPL_ID").is(tplId).and("DISABLED").is(false);
        List<TeacherTask> teacherTasks = query(Query.query(criteria));
        return teacherTasks.stream().findAny().orElse(null);
    }

    public void updateTeacherTask(TeacherTask teacherTask) {
        Update update = Update.update("TYPE", teacherTask.getType())
                .set("NAME", teacherTask.getName())
                .set("TEACHER_ID", teacherTask.getTeacherId())
                .set("TPL_ID", teacherTask.getTplId())
                .set("STATUS", teacherTask.getStatus())
                .set("EXPIRE_DATE", teacherTask.getExpireDate())
                .set("RECEIVE_DATE", teacherTask.getReceiveDate())
                .set("CANCEL_DATE", teacherTask.getCancelDate())
                .set("FINISHED_DATE", teacherTask.getFinishedDate());
        Criteria criteria = Criteria.where("ID").is(teacherTask.getId());
        if ($update(update, criteria) > 0) {
            Set<String> cacheKeys = new HashSet<>();
            calculateCacheDimensions(teacherTask, cacheKeys);
            getCache().delete(cacheKeys);
        }
    }

}
