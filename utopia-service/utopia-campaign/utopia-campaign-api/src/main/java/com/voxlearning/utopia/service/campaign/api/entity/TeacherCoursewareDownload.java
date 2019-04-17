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
@DocumentTable(table = "VOX_TEACHER_COURSEWARE_DOWNLOAD")
public class TeacherCoursewareDownload implements Serializable {

    private static final long serialVersionUID = -8591946292761668177L;
    @UtopiaSqlColumn(
            name = "ID",
            primaryKey = true,
            primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC
    )
    private Long id;
    @UtopiaSqlColumn private String courseware_id;
    @UtopiaSqlColumn private Long teacher_id;
    @UtopiaSqlColumn private Integer allow_download_times;
    @UtopiaSqlColumn private Integer already_download_times;
    @UtopiaSqlColumn private Integer allow_lottery_times;
    @UtopiaSqlColumn private Integer already_lottery_times;
    @UtopiaSqlColumn private String	disabled;
    @DocumentUpdateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    public static String ck_teacher(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(TeacherCoursewareDownload.class,
                new String[]{"TID"},
                new Object[]{teacherId});
    }

}
