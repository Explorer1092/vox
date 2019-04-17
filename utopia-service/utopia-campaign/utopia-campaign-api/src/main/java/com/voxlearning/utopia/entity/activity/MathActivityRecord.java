package com.voxlearning.utopia.entity.activity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 数据趣味周活动记录 - 实体
 * Created by ganhaitian on 2018/4/1.
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_MATH_ACTIVITY_RECORD")
@UtopiaCacheRevision("20180503")
public class MathActivityRecord extends AbstractDatabaseEntity implements Cloneable{

    private static final long serialVersionUID = 1337409950080693806L;

    @UtopiaSqlColumn private Long teacherId;
    @UtopiaSqlColumn private Integer rank;
    @UtopiaSqlColumn private Integer phase;
    @UtopiaSqlColumn private Integer clazz;
    @UtopiaSqlColumn private Long groupId;
    @UtopiaSqlColumn private Integer stuNum;
    @UtopiaSqlColumn private Integer finishNum;
    @UtopiaSqlColumn private String clazzName;
    @UtopiaSqlColumn private String schoolName;
    @UtopiaSqlColumn private String teacherName;
    @UtopiaSqlColumn private BigDecimal avgScore;

    public static String ck_teacherId(Long teacherId){
        return CacheKeyGenerator.generateCacheKey(MathActivityRecord.class,"teacherId",teacherId);
    }

    public MathActivityRecord clone(){
        try {
            return (MathActivityRecord) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
