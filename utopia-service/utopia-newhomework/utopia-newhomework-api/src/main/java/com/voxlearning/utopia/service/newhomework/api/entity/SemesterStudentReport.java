package com.voxlearning.utopia.service.newhomework.api.entity;


import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 学期报告信息
 */
@Setter
@Getter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newhomework")
@DocumentCollection(collection = "semester_student_report")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
@UtopiaCacheRevision("20161216")
public class SemesterStudentReport implements Serializable {

    private static final long serialVersionUID = -4119612722313642368L;
    @DocumentId
    private String id;
    private Long teacher_id;            //老师id
    private Long group_id;              //学组id
    private Long student_id;            //学生id
    private Integer assign_hw_num;      //布置作业数
    private Integer finish_hw_num;      //完成作业数
    private Integer avg_score;          //平均分
    private Integer grp_avg_score;      //组平均分
    private Integer grp_max_score;      //组最高分
    private String book_id;             //bookid
    private String content;             //知识点内容
    private String dt;                  //数据日期
    private String subject;             //科目
    private List<String> formdata; //作业形式信息

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(SemesterStudentReport.class, id);
    }

}
