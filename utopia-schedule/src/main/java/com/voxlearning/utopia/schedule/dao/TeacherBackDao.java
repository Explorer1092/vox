package com.voxlearning.utopia.schedule.dao;

import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.schedule.entity.TeacherBack;

import javax.inject.Named;

@Named
public class TeacherBackDao extends StaticCacheDimensionDocumentJdbcDao<TeacherBack, Long> {
}
