package com.voxlearning.utopia.service.newexam.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

//班级表格
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newexam")
@DocumentCollection(collection = "rpt_mock_exam_class_day_v1")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20171019")
public class RptMockNewExamClazz implements Serializable {
    private static final long serialVersionUID = -924986168889103818L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentField("paper_id")
    private String paperDocId;  //paperDocId
    @DocumentField("paper_name")
    private String paperName;
    @DocumentField("exam_id")
    private String examId;

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

    @DocumentField("group_id")
    private Integer groupId;
    @DocumentField("class_id")
    private Integer classId;
    @DocumentField("class_name")
    private String className;//班级名字
    @DocumentField("teacher_id")
    private Integer teacherId;
    @DocumentField("teacher_name")
    private String teacherName;//老师名字
    @DocumentField("school_id")
    private Integer schoolId;
    @DocumentField("school_name")
    private String schoolName;//学校名字
    @DocumentField("act_stu_num")
    private Integer actStuNum;//应考人数
    @DocumentField("real_stu_num")
    private Integer realStuNum;//参与学生
    @DocumentField("avg_score")
    private Double avgScore;//平均分
    @DocumentField("avg_duration")
    private Double avgDuration;
    @DocumentField("max_score")//最大分数
    private Double maxScore;
    @DocumentField("min_score")//最小分
    private Double minScore;

    /**
     *  [{ranks_name:'',ranks_rat:''}]即[{等级名称:'',等级占比（等级人数占全班人数的比率）:''}]
     */
    @DocumentField("rank_json")
    private String rankJson;// 等級率

    /**
     * [{part_id:'',title:'',part_score:'',part_avg_score:''}]即[{模块ID:'',模块名称:'',模块标准分:'',模块平均得分:''}]
     * }]
     */
    @DocumentField("part_json")//模块成绩
    private String partJson;
    @DocumentField("subject")
    private Subject subject;
    @DocumentField("school_level")
    private Integer schoolLevel;
    /**
     * （批改）总分最高分
     */
    @DocumentField("max_correctscore")
    private Double maxCorrectScore;
    /**
     * （批改）总分最低分
     */
    @DocumentField("min_correctscore")
    private Double minCorrectScore;
    /**
     * （批改）总分平均分
     */
    @DocumentField("avg_correctscore")
    private Double avgCorrectScore;
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
        return CacheKeyGenerator.generateCacheKey(RptMockNewExamClazz.class, id);
    }
}
