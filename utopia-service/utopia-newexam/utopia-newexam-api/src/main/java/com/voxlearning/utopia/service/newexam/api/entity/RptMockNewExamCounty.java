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
//区域表格
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newexam")
@DocumentCollection(collection = "rpt_mock_exam_county_day_v1")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20171019")
public class RptMockNewExamCounty implements Serializable{
    private static final long serialVersionUID = 9192326878145187564L;

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

    @DocumentField("act_stu_num")
    private Integer actStuNum;//应考人数
    @DocumentField("real_stu_num")
    private Integer realStuNum;//参与学生
    @DocumentField("max_score")//最大分数
    private Double maxScore;
    @DocumentField("min_score")//最小分
    private Double minScore;
    /**
     * 总分平均分
     */
    @DocumentField("avg_score")
    private Double avgScore;
    /**
     * 平均答题时长(秒)
     */
    @DocumentField("avg_duration")
    private Double avgDuration;

    /**
     *  [{ranks_name:'',ranks_rat:''}]即[{等级名称:'',等级占比（等级人数占全班人数的比率）:''}]
     */
    @DocumentField("rank_json")
    private String rankJson;// 等級率

    /**
     * 模块成绩
     *
     * [{part_id:模块ID,part_avg_score:模块平均得分,part_avg_correctscore:模块平均得分（批改）}]
     */
    @DocumentField("part_json")
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
        return CacheKeyGenerator.generateCacheKey(RptMockNewExamCounty.class, id);
    }
}
