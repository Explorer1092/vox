package com.voxlearning.utopia.service.mizar.api.entity.cjlschool;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLDataEntity;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLEntityType;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLField;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

/**
 * Created by Yuechen.Wang on 2017/7/19.
 */
@Getter
@Setter
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "vox_cjl_teacher_course")
@UtopiaCacheRevision("20170715")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class CJLTeacherCourse extends CJLDataEntity implements CacheDimensionDocument {

    private static final Long serialVersionUID = 4406459661360543742L;

    @DocumentId
    @CJLField(field = "ID") private String id;                       // 主键

    @CJLField(field = "schoolId") private String schoolId;           // 所属学校ID
    @CJLField(field = "eclassId") private String classId;            // 所属班级ID
    @CJLField(field = "teacherId") private String teacherId;         // 任教教师ID
    @CJLField(field = "teacherName") private String teacherName;     // 任教教师名
    @CJLField(field = "eclassUId") private String classUId;          // 班级UID
    @CJLField(field = "gradeName") private String gradeName;         // 所属年级名称
    @CJLField(field = "gradeNum") private String gradeNum;           // 所属年级
    @CJLField(field = "studentlist") private Collection<String> studentList;   // 班级学生List
    @CJLField(field = "PeriodId") private String periodId;           // 所属届ID
    @CJLField(field = "periodName") private String periodName;       // 所属届名称
    @CJLField(field = "isCurrent") private Boolean isCurrent;        // 是否是当前学期
    @CJLField(field = "schooltermId") private String schoolTermId;   // 学期ID

    @Override
    public CJLEntityType entity() {
        return CJLEntityType.TEACHER_COURSE;
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id)
        };
    }

    @JsonIgnore
    public boolean isValid() {
        return teacherId != null && classId != null;
    }

}
