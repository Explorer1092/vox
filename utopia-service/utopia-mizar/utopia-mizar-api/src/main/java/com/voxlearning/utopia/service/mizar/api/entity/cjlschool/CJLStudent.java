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
@DocumentCollection(collection = "vox_cjl_student")
@UtopiaCacheRevision("20170715")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class CJLStudent extends CJLDataEntity implements CacheDimensionDocument {

    private static final Long serialVersionUID = 2406459661360543742L;

    @DocumentId
    @CJLField(field = "id") private String id;                              // 主键

    @CJLField(field = "name") private String name;                          // 学生姓名
    @CJLField(field = "sex") private String gender;                         // 学生性别
    @CJLField(field = "studentCode") private String studentNumber;          // 学生学号
    @CJLField(field = "schoolId") private String schoolId;                  // 所在学校ID
    @CJLField(field = "eclassList") private Collection<String> classList;   // 所属班级ID
    @CJLField(field = "eclassId") private String classId;                   // 所在行政班ID
    @CJLField(field = "status") private String status;                      // 学生状态

    private String klxStudentId;                // 创建的时候对应的KlxStudent的ID

    @Override
    public CJLEntityType entity() {
        return CJLEntityType.STUDENT;
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id)
        };
    }

    @JsonIgnore
    public boolean isValid() {
        return status != null && "在校".equals(status);
    }
}
