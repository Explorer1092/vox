package com.voxlearning.utopia.service.campaign.api.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_TEACHER_COURSEWARE_STATISTICS")
public class TeacherCoursewareStatistics implements Serializable {

    private static final long serialVersionUID = -8591946292761668177L;
    @UtopiaSqlColumn(
            name = "ID",
            primaryKey = true,
            primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC
    )
    private Long id;
    @UtopiaSqlColumn private String courseware_id;
    @UtopiaSqlColumn private Long create_teacher_id;
    @UtopiaSqlColumn private Long operate_teacher_id;
    @UtopiaSqlColumn private String type;
    @UtopiaSqlColumn private String	disabled;
    @UtopiaSqlColumn private String	authentication;
    @UtopiaSqlColumn private String	wechatOpenId;
    @DocumentUpdateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    public static String ck_opuser(String op, Long operateTid) {
        return CacheKeyGenerator.generateCacheKey(TeacherCoursewareStatistics.class,
                new String[]{"OP", "TID"},
                new Object[]{op, operateTid});
    }

    public static String ck_openuser(String op, String openId) {
        return CacheKeyGenerator.generateCacheKey(TeacherCoursewareStatistics.class,
                new String[]{"OP", "OID"},
                new Object[]{op, openId});
    }

}
