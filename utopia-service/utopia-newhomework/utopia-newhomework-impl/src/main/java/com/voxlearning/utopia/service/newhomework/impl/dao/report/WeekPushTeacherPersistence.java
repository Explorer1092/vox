package com.voxlearning.utopia.service.newhomework.impl.dao.report;


import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.newhomework.api.entity.report.WeekPushTeacher;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
public class WeekPushTeacherPersistence extends AlpsStaticJdbcDao<WeekPushTeacher, Long> {


    public Page<WeekPushTeacher> loadWeekPushTeacherByPage(Pageable pageable) {
        Pageable request = new PageRequest(pageable.getPageNumber(), pageable.getPageSize());
        Criteria criteria = new Criteria();
        List<WeekPushTeacher> content = query(Query.query(criteria).with(request));
        long total = count(Query.query(criteria));
        return new PageImpl<>(content, request, total);
    }

    @Override
    protected void calculateCacheDimensions(WeekPushTeacher document, Collection<String> dimensions) {
        dimensions.add(WeekPushTeacher.ck_id(document.getTeacherId()));
    }
}
