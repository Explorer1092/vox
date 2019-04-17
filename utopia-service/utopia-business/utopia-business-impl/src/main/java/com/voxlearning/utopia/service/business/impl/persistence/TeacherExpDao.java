package com.voxlearning.utopia.service.business.impl.persistence;

import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.entity.level.TeacherExp;

import java.util.Collection;

public class TeacherExpDao extends AlpsStaticJdbcDao<TeacherExp,Long>{

    @Override
    protected void calculateCacheDimensions(TeacherExp document, Collection<String> dimensions) {

    }
}
