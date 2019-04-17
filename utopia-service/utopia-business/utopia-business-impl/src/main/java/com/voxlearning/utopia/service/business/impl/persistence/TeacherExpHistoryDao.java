package com.voxlearning.utopia.service.business.impl.persistence;

import com.voxlearning.alps.dao.jdbc.dao.AlpsDynamicJdbDao;
import com.voxlearning.utopia.entity.level.TeacherExpHistory;

import javax.inject.Named;
import java.util.Collection;

@Named
public class TeacherExpHistoryDao extends AlpsDynamicJdbDao<TeacherExpHistory,Long>{

    @Override
    protected void calculateCacheDimensions(TeacherExpHistory document, Collection<String> dimensions) {

    }

    @Override
    protected String calculateTableName(String template, TeacherExpHistory document) {
        return null;
    }
}
