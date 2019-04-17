package com.voxlearning.utopia.service.mizar.api.entity.cjlschool;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLConstants;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLDataEntity;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLEntityType;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLField;
import com.voxlearning.utopia.service.user.api.entities.ArtScienceType;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Yuechen.Wang on 2017/7/19.
 */
@Getter
@Setter
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "vox_cjl_class")
@UtopiaCacheRevision("20170715")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class CJLClass extends CJLDataEntity implements CacheDimensionDocument {

    private static final Long serialVersionUID = 1406459661360543742L;

    @DocumentId
    @CJLField(field = "id") private String id;                            // 主键
    @CJLField(field = "name") private String name;                        // 班级名
    @CJLField(field = "schoolId") private String schoolId;                // 学校ID
    @CJLField(field = "schoolName") private String schoolName;            // 学校名称
    @CJLField(field = "masterTeacherId") private String masterTeacherId;  // 班主任ID
    @CJLField(field = "type") private String type;                        // 班级类型  , 普通班、重点班、实验班、美术班、特长班
    @CJLField(field = "type1") private String type1;                      // 班级类型1 , 标准班、文科班、理科班
    @CJLField(field = "type2") private String type2;                      // 班级类型2 , 层级班、行政班
    @CJLField(field = "entranceYear") private String entranceYear;        // 入学年份
    @CJLField(field = "gradeId") private String gradeId;                  // 年级Id
    @CJLField(field = "gradeName") private String gradeName;              // 年级name
    @CJLField(field = "isNormalEclass") private Boolean isPublic;         // 是否行政班
    @CJLField(field = "isCurrent") private Boolean isCurrent;             // 是否当前学期
    @CJLField(field = "periodId") private String periodId;                // 所属届ID
    @CJLField(field = "periodName") private String periodName;            // 所属届名称
    @CJLField(field = "status") private Integer status;                   // 状态, 1--启用, 0--禁用
    @CJLField(field = "graduatedFlag") private Boolean graduated;         // 毕业标志

    @Override
    public CJLEntityType entity() {
        return CJLEntityType.CLASS;
    }

    @JsonIgnore
    public ClazzLevel getClazzLevel() {
        if (StringUtils.isBlank(gradeName)) {
            return null;
        }
        return CJLConstants.parseGrade(gradeName);
    }

    @JsonIgnore
    public ArtScienceType getArtScienceType() {
        return CJLConstants.parseArtScienceType(type1);
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id)
        };
    }

    public String formalizeClazzName() {
        ClazzLevel clazzLevel = getClazzLevel();
        if (clazzLevel == null) {
            clazzLevel = ClazzLevel.getDefaultClazzLevel();
        }
        return clazzLevel.getDescription() + (name == null ? "" : name);
    }

    @JsonIgnore
    public boolean isNotGraduated() {
        return !Boolean.TRUE.equals(graduated);
    }

}
