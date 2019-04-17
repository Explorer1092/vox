package com.voxlearning.utopia.service.mizar.api.entity.cjlschool;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLDataEntity;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLEntityType;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLField;
import lombok.Getter;
import lombok.Setter;

import java.util.stream.Stream;

/**
 * Created by Yuechen.Wang on 2017/7/19.
 */
@Getter
@Setter
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "vox_cjl_teacher")
@UtopiaCacheRevision("20170715")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class CJLTeacher extends CJLDataEntity implements CacheDimensionDocument {

    private static final Long serialVersionUID = 3406459661360543742L;

    @DocumentId
    @CJLField(field = "id") private String id;                           // 主键

    @CJLField(field = "name") private String name;                       // 老师姓名
    @CJLField(field = "loginName") private String loginName;             // 老师登录名
    @CJLField(field = "sex") private String gender;                      // 老师性别
    @CJLField(field = "phoneNumber") private String mobile;              // 老师手机号
    @CJLField(field = "courseListNames") private String subjectNames;    // 老师任教科目
    @CJLField(field = "status") private Integer status;                  // 老师状态, 0--在职 1--离职 2--离退
    @CJLField(field = "schoolId") private String schoolId;               // 老师所在学校ID

    @Override
    public CJLEntityType entity() {
        return CJLEntityType.TEACHER;
    }

    @JsonIgnore
    public boolean isMathTeacher() {
        return subjectNames != null && Stream.of(subjectNames.split(",")).anyMatch(subject -> StringUtils.equals(subject, Subject.MATH.getValue()));
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id), newCacheKey("L", loginName)
        };
    }

    @JsonIgnore
    public boolean isValid() {
        return status != null && status == 0;
    }

}
