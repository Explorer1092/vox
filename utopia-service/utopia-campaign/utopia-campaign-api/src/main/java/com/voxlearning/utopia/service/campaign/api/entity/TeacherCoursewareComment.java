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
@DocumentTable(table = "VOX_TEACHER_COURSEWARE_COMMENT")
public class TeacherCoursewareComment implements Serializable {

    private static final long serialVersionUID = -8591946292761668177L;
    @UtopiaSqlColumn(
            name = "ID",
            primaryKey = true,
            primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC
    )
    private Long id;
    @UtopiaSqlColumn private String courseware_id;
    @UtopiaSqlColumn private Long teacher_id;
    @UtopiaSqlColumn private Integer star;
    @UtopiaSqlColumn private String comment_one;
    @UtopiaSqlColumn private String comment_two;
    @UtopiaSqlColumn private String comment_three;
    @UtopiaSqlColumn private String comment_four;
    @UtopiaSqlColumn private String comment_five;
    @UtopiaSqlColumn private String	disabled;
    @UtopiaSqlColumn private String	key_word;
    @UtopiaSqlColumn private String	authentication;
    @DocumentUpdateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    public static String ck_tid_cid(Long teacherId, String coursewareId) {
        return CacheKeyGenerator.generateCacheKey(TeacherCoursewareComment.class,
                new String[]{"TID", "CID"},
                new Object[]{teacherId, coursewareId});
    }

//    public static String ck_courseware(String coursewareId) {
//        return CacheKeyGenerator.generateCacheKey(TeacherCoursewareComment.class,
//                new String[]{"COID"},
//                new Object[]{coursewareId});
//    }
//
//    public static String ck_Id(String id) {
//        return CacheKeyGenerator.generateCacheKey(TeacherCoursewareComment.class, id);
//    }
}
