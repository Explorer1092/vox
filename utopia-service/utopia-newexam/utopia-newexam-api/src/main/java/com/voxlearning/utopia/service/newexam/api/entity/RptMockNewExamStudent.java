package com.voxlearning.utopia.service.newexam.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
//个人表格
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newexam")
@DocumentCollection(collection = "rpt_mock_exam_student_day_v1")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181206")
public class RptMockNewExamStudent implements Serializable {
    private static final long serialVersionUID = 4418189745211761727L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;

    /***** begin 维度信息****/
    @DocumentField("paper_id")
    private String paperDocId;  //paperDocId
    @DocumentField("paper_name")
    private String paperName;
    @DocumentField("exam_id")
    private String examId;
    /***** end 维度信息****/

    /**** begin 教研员区域限制 ： 用于下载相应的数据********/
    @DocumentField("county_id")
    private Integer countyId;
    @DocumentField("county_name")
    private String countyName;
    @DocumentField("city_id")
    private Integer cityId;
    @DocumentField("city_name")
    private String cityName;
    @DocumentField("province_id")
    private Integer provinceId;
    @DocumentField("province_name")
    private String provinceName;
    /**** end 教研员区域限制 ： 用于下载相应的数据********/

    @DocumentField("student_id")
    private Long studentId;
    @DocumentField("student_name")
    private String studentName;//学生Name
    @DocumentField("class_id")
    private Integer classId;
    @DocumentField("class_name")
    private String className;
    @DocumentField("group_id")
    private Long groupId;
    @DocumentField("school_id")
    private Integer schoolId;
    @DocumentField("school_name")
    private String schoolName;//学校名字
    @DocumentField("teacher_id")
    private Long teacherId;
    @DocumentField("teacher_name")
    private String teacherName;
    @DocumentField("total_score")
    private Double totalScore;//总分
    @DocumentField("total_duration")
    private Double totalDuration;//总时间 暂时不需要，先留着

    /**
     * [{
     *     partId：1，
     *     score:1.0,
     *     duration:10,
     *     questions:[{
     *         qid:"qidA"
     *         score:1.0,
     *         correctScore：1.0,
     *         duration:10,
     *         subquestions:[{小题
     *            score:1.0,
     *            correctScore：1.0,
     *            duration:10
     *         }]
     *     }]
     *
     * }]
     */
    @DocumentField("paper_json")
    private String paperJson;
    @DocumentField("submitat")
    private Boolean submitAt;   // 是否提交
    @DocumentField("subject")
    private Subject subject;
    @DocumentField("school_level")
    private Integer schoolLevel;
    /**
     * 批改总成绩
     */
    @DocumentField("total_correctscore")
    private Double totalCorrectScore;
    /**
     * 创建时间
     */
    @DocumentCreateTimestamp
    private Date createAt;
    /**
     * 更新时间
     */
    @DocumentUpdateTimestamp
    private Date updateAt;

    public static String ckId(String id) {
        return CacheKeyGenerator.generateCacheKey(RptMockNewExamStudent.class, id);
    }
}
